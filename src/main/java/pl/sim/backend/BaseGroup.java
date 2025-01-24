package pl.sim.backend;

import pl.simNG.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BaseGroup extends SimGroup {

    Random random = new Random();
    protected SimPosition originalDestination;
    private SimPosition lastAttackerPosition = null;
    protected int totalInitialUnits;

    public BaseGroup(String name, SimPosition position, SimForceType forceType) {
        super(name, position, forceType);
        totalInitialUnits = units.stream().mapToInt(SimUnit::getInitialUnits).sum();
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
                int maxInterval = 10;
                int minInterval = 1;

                int nextShotInterval = Math.max(minInterval, (int) Math.ceil(maxInterval - (fireIntensity * (maxInterval - minInterval) / 10.0)));

                addTask(() -> unitShot(unit), nextShotInterval);
                Logger.log(this, "Konfiguracja dla jednostki " + unit.getName() +
                        ": fireIntensity = " + fireIntensity + ", pierwszy strzał za: " + nextShotInterval, parent.getSimulationTime());
            }
        }
    }

    //=====================================
    //Sekcja odpowiedzialna za ruch grupy
    //=====================================

    //Czy jest w pobliżu celu
    private boolean isCloseToDestination(SimPosition current, SimPosition destination, double tolerance) {
        return Math.abs(current.getX() - destination.getX()) <= tolerance &&
                Math.abs(current.getY() - destination.getY()) <= tolerance;
    }

    //Główna metoda odpowiedzialna za ruch
    @Override
    public void move() {
        int groupSpeed = getSpeed();
        double stepSize = 0.2 * groupSpeed;

        int minShotRange = units.stream()
                .mapToInt(SimUnit::getShotRange)
                .min()
                .orElse(0);

        //Przeciwnik znajduje się w zasięgu widoczności
        if (!visibleGroups.isEmpty() && !(units.stream().allMatch(unit -> unit.getTotalCurrentAmmunition() == 0))) {
            SimGroup target = visibleGroups.get(0);
            double distanceToTarget = position.distanceTo(target.getPosition());
            if (distanceToTarget > (minShotRange-0.5)) {
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
        else if (units.stream().allMatch(unit -> unit.getTotalCurrentAmmunition() == 0)) {
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
                SimVector2d smoothDirection = new SimVector2d(direction.getX(), direction.getY());
                position.add(smoothDirection.getDx(), smoothDirection.getDy());
                Logger.log(this, "Kontynuuje ruch po trasie. Następny krok: " + position, parent.getSimulationTime());
            }
        }
    }

    //Ruch w kierunku przeciwnika
    private void attackTarget(SimPosition targetPosition, double speed) {
        if (route.isEmpty() || !targetPosition.equals(new SimPosition(route.getLast().getX(), route.getLast().getY()))) {
            Logger.log(this, "Oblicza trasę do celu. Cel: " + targetPosition, parent.getSimulationTime());
            route = calculateRouteTo(targetPosition);
        }

        if (!route.isEmpty()) {
            SimVector2i direction = route.poll();
            if (direction != null) {
                SimVector2d smoothDirection = new SimVector2d(direction.getX(), direction.getY());
                position.add(smoothDirection.getDx(), smoothDirection.getDy());
                Logger.log(this, "Porusza się w kierunku celu. Pozycja: " + position, parent.getSimulationTime());
            }
        }
    }

    //==================================================================
    //Sekcja odpowiedzialna za przyjmowanie obrażeń i stratę jednostek
    //==================================================================

    //Główna metoda odpowiedzialna za otrzymywanie obrażeń przez jednostkę
    public void applyDamage(SimGroup attacker, SimUnit targetUnit) {
        if (units.contains(targetUnit) && targetUnit.getActiveUnits() > 0) {
            int lostAmmo = targetUnit.killOneSubunit(random);

            Logger.log(this,
                    "Jednostka " + targetUnit.getName() + " została uszkodzona. " +
                            "Pozostało aktywnych: " + targetUnit.getActiveUnits() + "/" + targetUnit.getInitialUnits() +
                            ". Stracono amunicję: " + lostAmmo,
                    parent.getSimulationTime()
            );

            int totalActiveUnits = units.stream().mapToInt(SimUnit::getActiveUnits).sum();
            Logger.log(this,
                    "applyDamage: Początkowe jednostki: " + totalInitialUnits +
                            ", Aktywne: " + totalActiveUnits,
                    parent.getSimulationTime()
            );

            this.cleanDestroyedUnits();

            if ((double) totalActiveUnits / totalInitialUnits < 0.30) {
                destroyGroup();
                Logger.log(this, "Grupa " + this.getName() + " została rozbita przez " + attacker.getName() + "!", parent.getSimulationTime());
            } else {
                lastAttackerPosition = attacker.getPosition();
                Logger.log(this,
                        "Została zaatakowana przez " + attacker.getName() +
                                " na pozycji " + lastAttackerPosition,
                        parent.getSimulationTime()
                );
            }
        }
    }

    //Usunięcie grupy
    public void destroyGroup() {
        Logger.log(this, "Grupa " + this.getName() + " została całkowicie usunięta.", parent.getSimulationTime());
        units.clear();
        visibleGroups.clear();
        route.clear();
    }

    //=============================================
    //Sekcja odpowiedzialna za zadawanie obrażeń
    //============================================

    //Główna funkcja odpowiedzialna za strzelanie jednostek
    protected void unitShot(SimUnit unit) {
        //Wykonuje się tylko jeśli mamy amunicję i jednostkę przeciwnika, która jest widoczna
        if (unit.getTotalCurrentAmmunition() > 0 && !visibleGroups.isEmpty()) {

            //I tylko jeśli jednostka jest w zasięgu strzału
            boolean anyEnemyInShotRange = false;
            for (SimGroup grp : visibleGroups) {
                double distToGroup = position.distanceTo(grp.getPosition());
                if (distToGroup <= unit.getShotRange()) {
                    anyEnemyInShotRange = true;
                }
            }

            if (anyEnemyInShotRange) {
                Logger.log(this,
                        "Grupa " + this.getName() + " rozpoczyna ostrzał. Jednostka: " +
                                unit.getName() + ", Obecna amunicja: " + unit.getTotalCurrentAmmunition(),
                        parent.getSimulationTime());

                visibleGroups.removeIf(SimGroup::isDestroyed);

                //Obliczamy wagę potencjalnych celów
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
                    Logger.log(this, "Brak ważnych celów dla jednostki " + unit.getName(),
                            parent.getSimulationTime());
                    return;
                }

                //Losowanie celu na podstawie wagi
                double cumulativeProbability = 0.0;
                SimUnit selectedUnit = null;
                SimGroup selectedGroup = null;

                for (Map.Entry<SimUnit, Integer> entry : targetWeights.entrySet()) {
                    SimUnit potentialTarget = entry.getKey();
                    cumulativeProbability += (double) entry.getValue() / totalWeight;
                    if (random.nextDouble() <= cumulativeProbability) {
                        selectedUnit = potentialTarget;
                        break;
                    }
                }

                if (selectedUnit != null) {
                    SimUnit finalSelectedUnit = selectedUnit;
                    selectedGroup = visibleGroups.stream()
                            .filter(group -> group.getUnits().contains(finalSelectedUnit))
                            .findFirst()
                            .orElse(null);
                }

                //Wykonanie strzałów
                if (selectedGroup != null) {
                    double distance = position.distanceTo(selectedGroup.getPosition());

                    int attackerActiveUnits = unit.getActiveUnits();
                    int targetActiveUnits = selectedUnit.getActiveUnits();
                    int currentAmmo = unit.getTotalCurrentAmmunition();

                    int randomShots = random.nextInt(attackerActiveUnits) + 1;
                    int limitedByTarget = Math.min(randomShots, targetActiveUnits);
                    int finalShots = Math.min(limitedByTarget, currentAmmo);

                    int kills = 0;
                    int ammoBefore = unit.getTotalCurrentAmmunition();

                    Logger.log(this,
                            String.format(
                                    "Z jednostki %s może strzelać maks %d pod-jednostek. Wylosowano %d do strzału, ale ograniczono do %d (cel ma %d, ammo=%d).",
                                    unit.getName(),
                                    attackerActiveUnits,
                                    randomShots,
                                    finalShots,
                                    targetActiveUnits,
                                    currentAmmo
                            ),
                            parent.getSimulationTime()
                    );

                    java.util.List<Integer> availableIndexes = new java.util.ArrayList<>();
                    for (int i = 0; i < unit.getInitialUnits(); i++) {
                        if (i < unit.getActiveUnits()) {
                            if (unit.hasAmmo(i)) {
                                availableIndexes.add(i);
                            }
                        }
                    }

                    java.util.Collections.shuffle(availableIndexes);

                    if (finalShots > availableIndexes.size()) {
                        finalShots = availableIndexes.size();
                    }

                    for (int s = 0; s < finalShots; s++) {
                        int subunitIndex = availableIndexes.get(s);
                        unit.useOneAmmo(subunitIndex);

                        double hitProbability = unit.calculateHitProbability(selectedUnit.getType(), distance);
                        if (random.nextDouble() <= hitProbability) {
                            double destructionProbability =
                                    unit.calculateDestructionProbability(unit, selectedUnit.getType());

                            if (random.nextDouble() <= destructionProbability) {
                                selectedGroup.applyDamage(this, selectedUnit);
                                kills++;
                                if (selectedUnit.getActiveUnits() <= 0) {
                                    break;
                                }
                            }
                        }
                    }

                    int ammoAfter = unit.getTotalCurrentAmmunition();

                    Logger.log(this,
                            String.format(
                                    "Seria strzałów zakończona: wystrzelono %d pocisków. Zniszczono %d jednostek przeciwnika [%s]. Amunicja spadła z %d do %d.",
                                    (ammoBefore - ammoAfter),
                                    kills,
                                    selectedGroup.getName(),
                                    ammoBefore,
                                    ammoAfter
                            ),
                            parent.getSimulationTime()
                    );
                }
            }
            scheduleNextShot(unit);
        }else {
            scheduleNextShot(unit);
        }
    }

    //Planowanie kolejnego zadania
    private void scheduleNextShot(SimUnit unit) {
        double fireIntensity = unit.getFireIntensity();
        if (fireIntensity > 0 && unit.getTotalCurrentAmmunition() > 0) {
            int maxInterval = 10;
            int minInterval = 1;
            int nextShotInterval = (int) Math.ceil(
                    maxInterval - (fireIntensity / 10.0) * (maxInterval - minInterval)
            );
            addTask(() -> unitShot(unit), nextShotInterval);
        }
    }


}
