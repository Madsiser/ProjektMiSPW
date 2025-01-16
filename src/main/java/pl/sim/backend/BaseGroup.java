package pl.sim.backend;

import pl.simNG.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BaseGroup extends SimGroup {

    private SimPosition originalDestination;
    private SimPosition lastAttackerPosition = null;

    public BaseGroup(String name, SimPosition position, SimForceType forceType) {
        super(name, position, forceType);

        SimUnit unit = new UnitManager.Abrams(50);
        this.addUnit(unit);
    }

    @Override
    public void init(){
        originalDestination = new SimPosition(2, 2);
        this.route = calculateRouteTo(new SimPosition(2,2));
        addTask(this::move,1);
        addProcess("shot", this::shot, 5);
    }

    //=====================================
    //Sekcja odpowiedzialna za ruch grupy
    //=====================================

    //Główna metoda odpowiedzialna za ruch
    @Override
    public void move() {
        int groupSpeed = getSpeed();
        double stepSize = 0.1 * groupSpeed;

        //Przeciwnik znajduje się w zasięgu widoczności
        if (!visibleGroups.isEmpty()) {
            SimGroup target = visibleGroups.get(0);
            if (units.stream().anyMatch(unit -> unit.inShotRange(target.getPosition()))) {
                Logger.log(this, "Przeciwnik w zasięgu strzału, oddawanie strzału. Cel: " + target.getName() +
                        " na pozycji " + target.getPosition(), parent.getSimulationTime());
                shot();
            } else {
                //Ruch w kierunku widocznego przeciwnika
                Logger.log(this, "Ruch w kierunku widocznego przeciwnika. Cel: " + target.getName() +
                        " na pozycji " + target.getPosition(), parent.getSimulationTime());
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
        else {
            Logger.log(this, "Kontynuowanie ruchu po pierwotnej trasie.", parent.getSimulationTime());
            moveToOriginalDestination(stepSize);
        }
        addTask(this::move, 1);
    }

    //Ruch po domyślnie zadanej trasie
    private void moveToOriginalDestination(double stepSize) {
        if (route.isEmpty() && !position.equals(originalDestination)) {
            Logger.log(this, "Oblicza trasę do pierwotnego celu. Pozycja celu: " + originalDestination, parent.getSimulationTime());
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

    public void apply_damage(SimGroup attacker) {
        if (!units.isEmpty()) {
            SimUnit unit = units.get(0);
            unit.setActiveUnits(unit.getActiveUnits() - 1);
            Logger.log(this, "Jednostka " + unit.getName() + " została uszkodzona. Pozostało aktywnych: " +
                    unit.getActiveUnits() + "/" + unit.getInitialUnits(), parent.getSimulationTime());

            this.cleanDestroyedUnits();

            //Jeśli grupa została rozbita
            if (isDestroyed()) {
                Logger.log(this, "Grupa została rozbita przez " + attacker.getName() + "!", parent.getSimulationTime());
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

    public void shot() {
        if (!visibleGroups.isEmpty()) {
            // Oblicz proporcje celu dla każdego typu jednostek
            Map<String, Integer> targetCounts = new HashMap<>();
            int totalUnits = 0;

            for (SimGroup target : visibleGroups) {
                for (SimUnit unit : target.getUnits()) {
                    targetCounts.put(unit.getType(), targetCounts.getOrDefault(unit.getType(), 0) + unit.getActiveUnits());
                    totalUnits += unit.getActiveUnits();
                }
            }

            Random random = new Random();
            for (SimUnit unit : units) {
                if (unit.getCurrentAmmunition() > 0) {
                    int shotsToFire = (int) Math.ceil(unit.getFireIntensity()); // Liczba strzałów na turę
                    for (int i = 0; i < shotsToFire && unit.getCurrentAmmunition() > 0; i++) {
                        double rand = random.nextDouble();
                        double cumulativeProbability = 0.0;

                        for (Map.Entry<String, Integer> entry : targetCounts.entrySet()) {
                            cumulativeProbability += (double) entry.getValue() / totalUnits;
                            if (rand <= cumulativeProbability) {
                                SimGroup target = visibleGroups.stream()
                                        .filter(g -> g.getUnits().stream()
                                                .anyMatch(u -> u.getType().equalsIgnoreCase(entry.getKey())))
                                        .findFirst()
                                        .orElse(null);

                                if (target != null) {
                                    double distance = position.distanceTo(target.getPosition());
                                    double hitProbability = unit.calculateHitProbability(entry.getKey(), distance);

                                    if (random.nextDouble() <= hitProbability) {
                                        target.apply_damage(this);
                                        Logger.log(this, "Trafia w jednostkę: " + entry.getKey() + ". Pozycja celu: " + target.getPosition(), parent.getSimulationTime());
                                    } else {
                                        Logger.log(this, "Nie trafia w jednostkę: " + entry.getKey() + ". Pozycja celu: " + target.getPosition(), parent.getSimulationTime());
                                    }
                                }
                                break;
                            }
                        }
                        unit.setCurrentAmmunition(unit.getCurrentAmmunition() - 1);
                    }
                }
            }
        }
    }
}
