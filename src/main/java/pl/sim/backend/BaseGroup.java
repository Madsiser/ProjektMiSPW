package pl.sim.backend;

import pl.simNG.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BaseGroup extends SimGroup {

    Random random = new Random();
    private SimPosition originalDestination;
    private SimPosition lastAttackerPosition = null;

    public BaseGroup(String name, SimPosition position, SimForceType forceType) {
        super(name, position, forceType);

        SimUnit unit = new UnitManager.Abrams(5);
        SimUnit unit2 = new UnitManager.BWP(5);
        this.addUnit(unit);
        this.addUnit(unit2);
    }

    //Inicjalizacja ruchu oraz strzału
    @Override
    public void init(){
        originalDestination = new SimPosition(2, 2);
        this.route = calculateRouteTo(new SimPosition(2,2));
        addTask(this::move,1);
        for (SimUnit unit : units) {
            double fireIntensity = unit.getFireIntensity();
            if (fireIntensity > 0) {
                int maxInterval = 3;
                int minInterval = 1;
                int nextShotInterval = (int) Math.ceil(maxInterval - (fireIntensity / 10.0) * (maxInterval - minInterval));
                addTask(() -> unitShot(unit), nextShotInterval);
                Logger.log(this, "Konfiguracja dla jednostki " + unit.getName() +
                        ": fireIntensity = " + fireIntensity + ", pierwszy strzał za: " + nextShotInterval, parent.getSimulationTime());
            }
        }
    }

    //=====================================
    //Sekcja odpowiedzialna za ruch grupy
    //=====================================

    private boolean isCloseToDestination(SimPosition current, SimPosition destination, double tolerance) {
        return Math.abs(current.getX() - destination.getX()) <= tolerance &&
                Math.abs(current.getY() - destination.getY()) <= tolerance;
    }

    //Główna metoda odpowiedzialna za ruch
    @Override
    public void move() {
        int groupSpeed = getSpeed();
        double stepSize = 0.1 * groupSpeed;

        int minShotRange = units.stream()
                .mapToInt(SimUnit::getShotRange)
                .min()
                .orElse(0);

        //Przeciwnik znajduje się w zasięgu widoczności
        if (!visibleGroups.isEmpty() && !(units.stream().allMatch(unit -> unit.getCurrentAmmunition() == 0))) {
            SimGroup target = visibleGroups.get(0);
            double distanceToTarget = position.distanceTo(target.getPosition());
            if (distanceToTarget > minShotRange) {
                // Zbliżanie się do celu
                Logger.log(this, "Zbliżanie się do celu, odległość: " + distanceToTarget +
                        ", minimalny zasięg: " + minShotRange, parent.getSimulationTime());
                attackTarget(target.getPosition(), stepSize);
            }
        }
        //Jeżeli został zaatakowany i brak widocznych przeciwników
        else if (lastAttackerPosition != null) {
            Logger.log(this, "Ruch w stronę ostatniego atakującego. Pozycja: " + lastAttackerPosition, parent.getSimulationTime());
            attackTarget(lastAttackerPosition, stepSize);
            lastAttackerPosition = null;
        }
        //Brak amunicji, powrót do pierwotnego celu
        else if (units.stream().allMatch(unit -> unit.getCurrentAmmunition() == 0)) {
            Logger.log(this, "Powrót do pierwotnej trasy z powodu braku amunicji.", parent.getSimulationTime());
            moveToOriginalDestination(stepSize);
        }
        //Domyślne poruszanie się po zadanej trasie
        else if (!isCloseToDestination(position, originalDestination, 0.5)){
            Logger.log(this, "Kontynuowanie ruchu po pierwotnej trasie.", parent.getSimulationTime());
            moveToOriginalDestination(stepSize);
            if(isCloseToDestination(position, originalDestination, 0.5)){
                Logger.log(this, "Dotarł w pobliże celu. Pozycja celu: " + originalDestination +
                        ", aktualna pozycja: " + position, parent.getSimulationTime());
            }
        }
        addTask(this::move, 1);
    }

    //Ruch po domyślnie zadanej trasie
    private void moveToOriginalDestination(double stepSize) {
        if (route.isEmpty() && !position.equals(originalDestination)) {
            Logger.log(this, "Oblicza trasę do pierwotnego celu. Pozycja celu: " + originalDestination +
                    ", aktualna pozycja: " + position, parent.getSimulationTime());
            route = calculateRouteTo(originalDestination);
        }
        if (!route.isEmpty()) {
            SimVector2i direction = route.poll();
            if (direction != null) {
                SimVector2d smoothDirection = new SimVector2d(direction.getX(), direction.getY()).scale(stepSize);
                position.add(smoothDirection.getDx(), smoothDirection.getDy());
                Logger.log(this, "Kontynuuje ruch po trasie. Następny krok: " + position, parent.getSimulationTime());
            }
        }
    }

    //Ruch w kierunku przeciwnika
    private void attackTarget(SimPosition targetPosition, double speed) {
        if (route.isEmpty() || !targetPosition.equals(route.getLast())) {
            Logger.log(this, "Oblicza trasę do celu. Cel: " + targetPosition, parent.getSimulationTime());
            route = calculateRouteTo(targetPosition);
        }

        if (!route.isEmpty()) {
            SimVector2i direction = route.poll();
            if (direction != null) {
                SimVector2d smoothDirection = new SimVector2d(direction.getX(), direction.getY()).scale(speed);
                position.add(smoothDirection.getDx(), smoothDirection.getDy());
                Logger.log(this, "Porusza się w kierunku celu. Pozycja: " + position, parent.getSimulationTime());
            }
        }
    }

    //==================================================================
    //Sekcja odpowiedzialna za przyjmowanie obrażeń i stratę jednostek
    //==================================================================

    public void applyDamage(SimGroup attacker, SimUnit targetUnit) {
        if (units.contains(targetUnit) && targetUnit.getActiveUnits() > 0) {
            targetUnit.setActiveUnits(targetUnit.getActiveUnits() - 1);

            //Utracona amunicja
            int ammunitionPerUnit = targetUnit.getInitialAmmunition() / targetUnit.getInitialUnits();
            int lostAmmunition = Math.min(ammunitionPerUnit, targetUnit.getCurrentAmmunition());
            targetUnit.setCurrentAmmunition(targetUnit.getCurrentAmmunition() - lostAmmunition);

            Logger.log(this, "Jednostka " + targetUnit.getName() + " została uszkodzona. Pozostało aktywnych: " +
                    targetUnit.getActiveUnits() + "/" + targetUnit.getInitialUnits() +
                    ". Stracono amunicję: " + lostAmmunition, parent.getSimulationTime());

            this.cleanDestroyedUnits();

            if (isDestroyed()) {
                Logger.log(this, "Grupa " + this.getName() + " została rozbita przez " + attacker.getName() + "!", parent.getSimulationTime());
            } else {
                lastAttackerPosition = attacker.getPosition();
                Logger.log(this, "Została zaatakowana przez " + attacker.getName() +
                        " na pozycji " + lastAttackerPosition, parent.getSimulationTime());
            }
        }
    }

    //=============================================
    //Sekcja odpowiedzialna za zadawanie obrażeń
    //============================================

    //Główna funkcja odpowiedzialna za strzelanie jednostek
    private void unitShot(SimUnit unit) {
        if (unit.getCurrentAmmunition() > 0) {
            if (visibleGroups.isEmpty()) {
                Logger.log(this, "Jednostka " + unit.getName() + " nie strzela, ponieważ brak widocznych przeciwników.", parent.getSimulationTime());
                return;
            }
            Logger.log(this, "Grupa " + this.getName() + " rozpoczyna ostrzał. Jednostka: " +
                    unit.getName() + ", Obecna amunicja: " + unit.getCurrentAmmunition(), parent.getSimulationTime());

            visibleGroups.removeIf(SimGroup::isDestroyed);

            //Obliczanie wagi celu na podstawie liczby aktywnych jednostek
            Map<SimUnit, Integer> targetWeights = new HashMap<>();
            int totalWeight = 0;
            for (SimGroup targetGroup : visibleGroups) {
                for (SimUnit targetUnit : targetGroup.getUnits()) {
                    int weight = targetUnit.getActiveUnits();
                    targetWeights.put(targetUnit, weight);
                    totalWeight += weight;
                }
            }

            if (totalWeight == 0) {
                Logger.log(this, "Brak ważnych celów dla jednostki " + unit.getName(), parent.getSimulationTime());
                return;
            }

            double cumulativeProbability = 0.0;
            SimUnit selectedUnit = null;
            SimGroup selectedGroup = null;

            //Losowanie celu według wagi
            for (Map.Entry<SimUnit, Integer> entry : targetWeights.entrySet()) {
                SimUnit potentialTarget = entry.getKey();
                cumulativeProbability += (double) entry.getValue() / totalWeight;
                if (random.nextDouble() <= cumulativeProbability) {
                    selectedUnit = potentialTarget;
                    break;
                }
            }

            //Znalezienie grupy odpowiadającej wybranemu celowi
            if (selectedUnit != null) {
                SimUnit finalSelectedUnit = selectedUnit;
                selectedGroup = visibleGroups.stream()
                        .filter(group -> group.getUnits().contains(finalSelectedUnit))
                        .findFirst()
                        .orElse(null);
            }

            if (selectedGroup != null && selectedUnit != null) {
                double distance = position.distanceTo(selectedGroup.getPosition());
                double hitProbability = unit.calculateHitProbability(selectedUnit.getType(), distance);

                Logger.log(this, "Jednostka " + unit.getName() + " celuje w: " + selectedUnit.getName() + " [" +
                        selectedGroup.getName() + "]. Odległość: " + distance + ", Prawdopodobieństwo trafienia: " +
                        hitProbability, parent.getSimulationTime());

                //Jednostka trafia w cel
                if (random.nextDouble() <= hitProbability) {
                    Logger.log(this, "Jednostka " + unit.getName() + " trafia w cel: " +
                            selectedUnit.getName() + " [" + selectedGroup.getName() + "]", parent.getSimulationTime());

                    double destructionProbability = unit.calculateDestructionProbability(selectedUnit.getType());

                    //Jednostka niszczy cel
                    if (random.nextDouble() <= destructionProbability) {
                        Logger.log(this, "Jednostka " + unit.getName() + " zniszczyła cel: " +
                                selectedUnit.getName() + " [" + selectedGroup.getName() + "]", parent.getSimulationTime());
                        selectedGroup.applyDamage(this, selectedUnit);
                    }
                    //Jednostka nie niszczy celu
                    else {
                        Logger.log(this, "Jednostka " + unit.getName() + " trafiła, ale nie zniszczyła celu: " +
                                selectedUnit.getName() + " [" + selectedGroup.getName() + "]", parent.getSimulationTime());
                    }
                }
                //Jednostka nie trafia w cel
                else {
                    Logger.log(this, "Jednostka " + unit.getName() + " nie trafia w cel: " +
                            selectedUnit.getName() + " [" + selectedGroup.getName() + "]", parent.getSimulationTime());
                }
            }
            unit.setCurrentAmmunition(unit.getCurrentAmmunition() - 1);
        }

        double fireIntensity = unit.getFireIntensity();
        if (fireIntensity > 0 && unit.getCurrentAmmunition() > 0) {
            int maxInterval = 3;
            int minInterval = 1;
            int nextShotInterval = (int) Math.ceil(maxInterval - (fireIntensity / 10.0) * (maxInterval - minInterval));
            addTask(() -> unitShot(unit), nextShotInterval);
        }
    }
}
