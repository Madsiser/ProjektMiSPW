package pl.sim.backend;

import pl.simNG.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Przykładowa konkretna implementacja grupy bazowej (np. batalion),
 * dziedzicząca po SimGroup, implementująca logikę ruchu, strzelania i otrzymywania obrażeń.
 * Na jej podstawie tworzone są jednostki (grupy) poruszające się po mapie.
 */
public class BaseGroup extends SimGroup {
    Random random = new Random();

    /** Punkt docelowy, do którego chcemy dotrzeć. */
    protected SimPosition originalDestination;
    /** Miejsce, skąd ostatnio nas zaatakowano (pozycja wroga atakującego). */
    private SimPosition lastAttackerPosition = null;
    /** Suma początkowych środków bojowych w momencie tworzenia jednostki (grupy). */
    protected int totalInitialUnits;

    /**
     * Konstruktor grupy bazowej.
     * @param name nazwa jednostki (grupy)
     * @param position pozycja startowa
     * @param forceType typ sił (BLUFOR, REDFOR)
     */
    public BaseGroup(String name, SimPosition position, SimForceType forceType) {
        super(name, position, forceType);
        totalInitialUnits = units.stream().mapToInt(SimUnit::getInitialUnits).sum();
    }

    /**
     * Metoda inicjalizująca ruch i konfigurację strzelania (wywoływana raz na początku).
     */
    @Override
    public void init(){
        addProcess("commandLogic", this::commandLogic, 4);
        addTask(this::move,1);
        //Strzał środków bojowych w ramach tej jednostki
        for (SimUnit unit : units) {
            double fireIntensity = unit.getFireIntensity();
            if (fireIntensity > 0) {
                int maxInterval = 10;
                int minInterval = 1;
                int nextShotInterval = Math.max(minInterval, (int) Math.ceil(maxInterval - (fireIntensity * (maxInterval - minInterval) / 10.0)));
                addTask(() -> unitShot(unit), nextShotInterval);
                Logger.log(this, "Konfiguracja dla jednostki " + unit.getName() + ": fireIntensity = " + fireIntensity + ", pierwszy strzał za: " + nextShotInterval, parent.getSimulationTime());
            }
        }
    }

    //=====================================
    //Sekcja odpowiedzialna za ruch grupy - DO POPRAWY
    //=====================================

    /**
     * Sprawdza, czy aktualna pozycja jest blisko docelowej w promieniu tolerance.
     * @param current bieżąca pozycja
     * @param destination docelowa pozycja
     * @param tolerance dopuszczalny promień różnicy
     * @return true, jeśli dotarliśmy wystarczająco blisko
     */
    private boolean isCloseToDestination(SimPosition current, SimPosition destination, double tolerance) {
        return Math.abs(current.getX() - destination.getX()) <= tolerance &&
                Math.abs(current.getY() - destination.getY()) <= tolerance;
    }

    /**
     * Główna metoda odpowiedzialna za ruch grupy.
     * Sprawdza różne warunki:
     * - jeżeli widzimy przeciwnika ruszamy w jego kierunku i atakujemy go
     * - jeżeli zostaliśmy przez kogoś zaatakowani ruszamy w jego kierunku i atakujemy go
     * - jeżeli nie mamy amunicji kierujemy się do pierwotnego celu
     * - domyślnie poruszamy się po zadanej trasie
     */
    @Override
    public void move() {
//        int groupSpeed = getSpeed();
//        double stepSize = 0.1 * groupSpeed;
//
//        int minShotRange = units.stream()
//                .mapToInt(SimUnit::getShootingRange)
//                .min()
//                .orElse(0);
//
//        visibleGroups.removeIf(SimGroup::isDestroyed);
//
//        //Przeciwnik znajduje się w zasięgu widoczności
//        if (!visibleGroups.isEmpty() && !(units.stream().allMatch(unit -> unit.getTotalCurrentAmmunition() == 0))) {
//            SimGroup target = visibleGroups.get(0);
//            if (target.isDestroyed()) {
//                Logger.log(this, "Cel został zniszczony, przerywam ruch w jego kierunku.", parent.getSimulationTime());
//                visibleGroups.remove(target);
//            } else {
//                double distanceToTarget = position.distanceTo(target.getPosition());
//                if (distanceToTarget > (minShotRange - 0.5)) {
//                    Logger.log(this, "Zbliżanie się do celu, odległość: " + distanceToTarget + ", minimalny zasięg: " + minShotRange, parent.getSimulationTime());
//                    attackTarget(target.getPosition(), stepSize);
//                }
//            }
//        }
//        //Jeżeli został zaatakowany i brak widocznych przeciwników
//        else if (lastAttackerPosition != null) {
//            boolean attackerStillExists = parent.getGroups().stream()
//                    .anyMatch(group -> group.getPosition().equals(lastAttackerPosition));
//
//            if (attackerStillExists) {
//                Logger.log(this, "Ruch w stronę ostatniego atakującego. Pozycja: " + lastAttackerPosition, parent.getSimulationTime());
//                attackTarget(lastAttackerPosition, stepSize);
//            } else {
//                Logger.log(this, "Przeciwnik, który nas atakował, został zniszczony. Rezygnuję z ruchu w jego kierunku.", parent.getSimulationTime());
//            }
//            lastAttackerPosition = null;
//        }
//        //Brak amunicji, powrót do pierwotnego celu
//        else if (units.stream().allMatch(unit -> unit.getTotalCurrentAmmunition() == 0)) {
//            Logger.log(this, "Powrót do pierwotnej trasy z powodu braku amunicji.", parent.getSimulationTime());
//            moveToOriginalDestination(stepSize);
//        }
//        //Domyślne poruszanie się po zadanej trasie
//        else if (!isCloseToDestination(position, originalDestination, 0.5)){
//            Logger.log(this, "Kontynuowanie ruchu po pierwotnej trasie.", parent.getSimulationTime());
//            moveToOriginalDestination(stepSize);
//            if(isCloseToDestination(position, originalDestination, 0.5)){
//                Logger.log(this, "Dotarł w pobliże celu. Pozycja celu: " + originalDestination + ", aktualna pozycja: " + position, parent.getSimulationTime());
//            }
//        }
        super.move();
        addTask(this::move, 1);
    }

    /**
     * Ruch po oryginalnie zaplanowanej trasie.
     * @param stepSize wielkość kroku zależna od prędkości grupy.
     */
    private void moveToOriginalDestination(double stepSize) {
        if (route.isEmpty() && !position.equals(originalDestination)) {
            Logger.log(this, "Oblicza trasę do pierwotnego celu. Pozycja celu: " + originalDestination + ", aktualna pozycja: " + position, parent.getSimulationTime());
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

    /**
     * Ruch w kierunku wybranej pozycji wroga.
     * @param targetPosition punkt docelowy
     * @param speed wielkość kroku
     */
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

    /**
     * Główna metoda odpowiedzialna za stratę środków bojowych przez jednostkę.
     * Jeśli środek bojowy zostaje zniszczony to tracimy również jej amunicję
     * Jeśli ilość aktywnych środków bojowych w stosunku do początkowych spadnie poniżej 30%, to jednostka zostaje rozbita
     * @param attacker jednostka (grupa) atakująca
     * @param targetUnit środek bojowy z tej grupy, który zostaje uszkodzona/zabita
     */
    public void applyDamage(SimGroup attacker, SimUnit targetUnit) {
        if (units.contains(targetUnit) && targetUnit.getActiveUnits() > 0) {
            //obsługa straty amunicji zniszczonego środka bojowego
            int lostAmmo = targetUnit.killOneSubunit(random);

            Logger.log(this,
                    "Jednostka " + targetUnit.getName() + " została uszkodzona. " + "Pozostało aktywnych: " + targetUnit.getActiveUnits() + "/" + targetUnit.getInitialUnits() + ". Stracono amunicję: " + lostAmmo, parent.getSimulationTime()
            );

            int totalActiveUnits = units.stream().mapToInt(SimUnit::getActiveUnits).sum();
            Logger.log(this,
                    "applyDamage: Początkowe jednostki: " + totalInitialUnits + ", Aktywne: " + totalActiveUnits, parent.getSimulationTime()
            );

            this.cleanDestroyedUnits();

            //Obsługa rozbicia jednostki przy spełnieniu określonych warunków
            if ((double) totalActiveUnits / totalInitialUnits < 0.30) {
                destroyGroup();
                Logger.log(this, "Grupa " + this.getName() + " została rozbita przez " + attacker.getName() + "!", parent.getSimulationTime());
            } else {
                lastAttackerPosition = new SimPosition(attacker.getPosition());
                Logger.log(this,
                        "Została zaatakowana przez " + attacker.getName() + " na pozycji " + lastAttackerPosition, parent.getSimulationTime()
                );
            }
        }
    }

    /**
     * Wywoływane, gdy cała jednostka (grupa) zostaje uznana za zniszczoną (rozbicie jednostki).
     */
    public void destroyGroup() {
        Logger.log(this, "Grupa " + this.getName() + " została całkowicie usunięta.", parent.getSimulationTime());
        units.clear();
        visibleGroups.clear();
        route.clear();
    }

    //=============================================
    //Sekcja odpowiedzialna za zadawanie obrażeń
    //============================================

    /**
     * Główna funkcja odpowiedzialna za strzelanie środków bojowych.
     * @param unit - środek bojowy, która strzela
     */
    protected void unitShot(SimUnit unit) {
        //Wykonuje się tylko jeśli mamy amunicję i wrogie grupy w polu widzenia
        if (unit.getTotalCurrentAmmunition() > 0 && !visibleGroups.isEmpty()) {

            visibleGroups.removeIf(SimGroup::isDestroyed);

            //Sprawdzamy, czy przynajmniej jeden przeciwnik jest w zasięgu strzału (targetsInRange).
            Map<SimUnit, SimGroup> targetsInRange = new HashMap<>();
            for (SimGroup group : visibleGroups) {
                double distanceToGroup = position.distanceTo(group.getPosition());
                if (distanceToGroup <= unit.getShootingRange()) {
                    for (SimUnit targetUnit : group.getUnits()) {
                        if (targetUnit.getActiveUnits() > 0) {
                            targetsInRange.put(targetUnit, group);
                        }
                    }
                }
            }

            //Jeżeli brak celów w zasięgu – kończymy metodę i planujemy następny strzał
            if (targetsInRange.isEmpty()) {
//                Logger.log(this,
//                        "Brak celów w zasięgu strzału dla jednostki: " + unit.getName(),
//                        parent.getSimulationTime()
//                );
                scheduleNextShot(unit);
                return;
            }

            Logger.log(this,
                    "Grupa " + this.getName() + " rozpoczyna ostrzał. Jednostka: " + unit.getName()
                            + ", Obecna amunicja: " + unit.getTotalCurrentAmmunition(),
                    parent.getSimulationTime()
            );

            // Budujemy mapę wag (dla losowania celu)
            Map<SimUnit, Integer> targetWeights = new HashMap<>();
            int totalWeight = 0;
            for (Map.Entry<SimUnit, SimGroup> entry : targetsInRange.entrySet()) {
                SimUnit tUnit = entry.getKey();
                int weight = tUnit.getActiveUnits();
                if (weight > 0) {
                    targetWeights.put(tUnit, weight);
                    totalWeight += weight;
                }
            }

            if (totalWeight == 0) {
//                Logger.log(this,
//                        "Brak ważnych celów (aktywne jednostki=0) dla " + unit.getName(),
//                        parent.getSimulationTime()
//                );
                scheduleNextShot(unit);
                return;
            }

            //Wybór (wylosowanie) docelowego SimUnit, do którego strzelamy
            SimUnit selectedUnit = null;
            SimGroup selectedGroup = null;

            if (targetWeights.size() == 1) {
                selectedUnit = targetWeights.keySet().iterator().next();
                selectedGroup = targetsInRange.get(selectedUnit);
            } else {
                // Losowanie celu na podstawie wagi
                double rnd = random.nextDouble();
                double cumulativeProbability = 0.0;
                for (Map.Entry<SimUnit, Integer> entry : targetWeights.entrySet()) {
                    cumulativeProbability += (double) entry.getValue() / totalWeight;
                    if (rnd <= cumulativeProbability) {
                        selectedUnit = entry.getKey();
                        selectedGroup = targetsInRange.get(selectedUnit);
                        break;
                    }
                }
            }

            //Jeśli udało się poprawnie wybrać cel - dokonujemy serii strzałów
            if (selectedUnit != null && selectedGroup != null) {
                double distance = position.distanceTo(selectedGroup.getPosition());

                int attackerActiveUnits = unit.getActiveUnits();
                int targetActiveUnits = selectedUnit.getActiveUnits();
                int currentAmmo = unit.getTotalCurrentAmmunition();

                //Losowanie ilości oddających strzał podjednostek
                int randomShots = random.nextInt(attackerActiveUnits) + 1;
                int limitedByTarget = Math.min(randomShots, targetActiveUnits);
                int finalShots = Math.min(limitedByTarget, currentAmmo);

                int kills = 0;
                int ammoBefore = unit.getTotalCurrentAmmunition();

                Logger.log(this, String.format(
                        "Z jednostki %s może strzelać maks %d pod-jednostek. Wylosowano %d do strzału, "
                                + "ale ograniczono do %d (cel ma %d, ammo=%d).",
                        unit.getName(),
                        attackerActiveUnits,
                        randomShots,
                        finalShots,
                        targetActiveUnits,
                        currentAmmo
                ), parent.getSimulationTime());

                //Lista indeksów aktywnych sub-unitów, które mają amunicję
                java.util.List<Integer> availableIndexes = new java.util.ArrayList<>();
                for (int i = 0; i < unit.getInitialUnits(); i++) {
                    if (i < unit.getActiveUnits() && unit.hasAmmo(i)) {
                        availableIndexes.add(i);
                    }
                }

                java.util.Collections.shuffle(availableIndexes);

                if (finalShots > availableIndexes.size()) {
                    finalShots = availableIndexes.size();
                }

                //Oddajemy serię strzałów
                for (int s = 0; s < finalShots; s++) {
                    int subunitIndex = availableIndexes.get(s);
                    unit.useOneAmmo(subunitIndex);

                    //Sprawdzenie trafienia
                    if (unit.calculateHitProbability(unit, selectedUnit, distance)) {
                        //Sprawdzenie zniszczenia
                        if (unit.calculateDestructionProbability(unit, selectedUnit, 0.25)) {
                            selectedGroup.applyDamage(this, selectedUnit);
                            kills++;
                            if (selectedUnit.getActiveUnits() <= 0) {
                                break;
                            }
                        }
                    }
                }

                int ammoAfter = unit.getTotalCurrentAmmunition();

                Logger.log(this, String.format(
                        "Seria strzałów zakończona: wystrzelono %d pocisków. Zniszczono %d jednostek [%s]. "
                                + "Amunicja spadła z %d do %d.",
                        (ammoBefore - ammoAfter),
                        kills,
                        selectedGroup.getName(),
                        ammoBefore,
                        ammoAfter
                ), parent.getSimulationTime());
            }
        }

        //Niezależnie od tego, czy strzelaliśmy, planujemy kolejną salwę
        scheduleNextShot(unit);
    }

    /**
     * Planowanie kolejnego ostrzału.
     * @param unit strzelający środek bojowy
     */
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
