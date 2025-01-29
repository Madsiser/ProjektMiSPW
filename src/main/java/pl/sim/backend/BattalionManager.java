package pl.sim.backend;

import pl.simNG.*;

/**
 * Klasa `BattalionManager` zawiera definicje różnych typów batalionów.
 * Każdy typ batalionu dziedziczy po klasie `BaseGroup`.
 */
public class BattalionManager {

    /** Klasa reprezentująca batalion czołgów Abrams. */
    public static class AbramsTankBattalion extends BaseGroup {
        public AbramsTankBattalion(String name, SimPosition position, SimForceType forceType, int initialUnits) {
            super(name, position, forceType);
            this.addUnit(UnitManager.createUnit("W1", initialUnits)); // Abrams
            totalInitialUnits = units.stream().mapToInt(SimUnit::getInitialUnits).sum();
        }

        @Override
        public void init() {
            super.init();
        }
    }

    /** Klasa reprezentująca batalion czołgów Leopard. */
    public static class LeopardTankBattalion extends BaseGroup {
        public LeopardTankBattalion(String name, SimPosition position, SimForceType forceType, int initialUnits) {
            super(name, position, forceType);
            this.addUnit(UnitManager.createUnit("W5", initialUnits)); // Leopard 2A5
            totalInitialUnits = units.stream().mapToInt(SimUnit::getInitialUnits).sum();
        }

        @Override
        public void init() {
            super.init();
        }
    }

    /** Klasa reprezentująca batalion czołgów T-90M. */
    public static class T90TankBattalion extends BaseGroup {
        public T90TankBattalion(String name, SimPosition position, SimForceType forceType, int initialUnits) {
            super(name, position, forceType);
            this.addUnit(UnitManager.createUnit("W7", initialUnits)); // T-90M
            totalInitialUnits = units.stream().mapToInt(SimUnit::getInitialUnits).sum();
        }

        @Override
        public void init() {
            super.init();
        }
    }

    /** Klasa reprezentująca batalion zmechanizowany. */
    public static class MechanizedBattalion extends BaseGroup {
        public MechanizedBattalion(String name, SimPosition position, SimForceType forceType, int initialUnits) {
            super(name, position, forceType);
            this.addUnit(UnitManager.createUnit("W3", initialUnits)); // Soldier
            this.addUnit(UnitManager.createUnit("W2", initialUnits)); // BWP-1
            totalInitialUnits = units.stream().mapToInt(SimUnit::getInitialUnits).sum();
        }

        @Override
        public void init() {
            super.init();
        }
    }

    /** Klasa reprezentująca batalion piechoty. */
    public static class InfantryBattalion extends BaseGroup {
        public InfantryBattalion(String name, SimPosition position, SimForceType forceType, int initialUnits) {
            super(name, position, forceType);
            this.addUnit(UnitManager.createUnit("W3", initialUnits)); // Soldier
            totalInitialUnits = units.stream().mapToInt(SimUnit::getInitialUnits).sum();
        }

        @Override
        public void init() {
            super.init();
        }
    }

    /** Klasa reprezentująca batalion artylerii lufowej. */
    public static class ArtilleryBattalion extends BaseGroup {
        public ArtilleryBattalion(String name, SimPosition position, SimForceType forceType, int initialUnits) {
            super(name, position, forceType);
            this.addUnit(UnitManager.createUnit("W4", initialUnits)); // Krab
            totalInitialUnits = units.stream().mapToInt(SimUnit::getInitialUnits).sum();
        }

        @Override
        public void init() {
            super.init();
        }
    }

    /** Klasa reprezentująca batalion artylerii rakietowej HIMARS. */
    public static class RocketArtilleryBattalion extends BaseGroup {
        public RocketArtilleryBattalion(String name, SimPosition position, SimForceType forceType, int initialUnits) {
            super(name, position, forceType);
            this.addUnit(UnitManager.createUnit("W6", initialUnits)); // HIMARS
            totalInitialUnits = units.stream().mapToInt(SimUnit::getInitialUnits).sum();
        }

        @Override
        public void init() {
            super.init();
        }
    }

    /** Klasa reprezentująca batalion mieszany czołgów Abrams i Leopard. */
    public static class MixedTankBattalion extends BaseGroup {
        public MixedTankBattalion(String name, SimPosition position, SimForceType forceType, int initialUnits) {
            super(name, position, forceType);
            this.addUnit(UnitManager.createUnit("W1", initialUnits / 2)); // Abrams
            this.addUnit(UnitManager.createUnit("W5", initialUnits / 2)); // Leopard 2A5
            totalInitialUnits = units.stream().mapToInt(SimUnit::getInitialUnits).sum();
        }

        @Override
        public void init() {
            super.init();
        }
    }

    /** Klasa reprezentująca batalion wsparcia ogniowego (BWP + artyleria). */
    public static class FireSupportBattalion extends BaseGroup {
        public FireSupportBattalion(String name, SimPosition position, SimForceType forceType, int initialUnits) {
            super(name, position, forceType);
            this.addUnit(UnitManager.createUnit("W2", initialUnits / 2)); // BWP-1
            this.addUnit(UnitManager.createUnit("W4", initialUnits / 2)); // Krab
            totalInitialUnits = units.stream().mapToInt(SimUnit::getInitialUnits).sum();
        }

        @Override
        public void init() {
            super.init();
        }
    }

    /** Klasa reprezentująca batalion ciężki (Abrams + HIMARS). */
    public static class HeavyBattalion extends BaseGroup {
        public HeavyBattalion(String name, SimPosition position, SimForceType forceType, int initialUnits) {
            super(name, position, forceType);
            this.addUnit(UnitManager.createUnit("W1", initialUnits / 2)); // Abrams
            this.addUnit(UnitManager.createUnit("W6", initialUnits / 2)); // HIMARS
            totalInitialUnits = units.stream().mapToInt(SimUnit::getInitialUnits).sum();
        }

        @Override
        public void init() {
            super.init();
        }
    }
}
