package pl.sim.backend;

import pl.simNG.*;

public class BattalionManager {

    public static class TankBattalion extends BaseGroup {
        public TankBattalion(String name, SimPosition position, SimForceType forceType, int initialUnits) {
            super(name, position, forceType);
            this.addUnit(UnitManager.createUnit("W1", initialUnits)); // Abrams
            totalInitialUnits = units.stream().mapToInt(SimUnit::getInitialUnits).sum();
        }

        @Override
        public void init() {
            super.init();
        }
    }

    public static class MechanizedBattalion extends BaseGroup {
        public MechanizedBattalion(String name, SimPosition position, SimForceType forceType, int initialUnits) {
            super(name, position, forceType);
            this.addUnit(UnitManager.createUnit("W1", initialUnits)); // Abrams
            this.addUnit(UnitManager.createUnit("W2", initialUnits)); // BWP
            totalInitialUnits = units.stream().mapToInt(SimUnit::getInitialUnits).sum();
        }

        @Override
        public void init() {
            super.init();
        }
    }

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
}
