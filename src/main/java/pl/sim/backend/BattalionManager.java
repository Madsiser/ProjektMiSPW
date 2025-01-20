package pl.sim.backend;

import pl.simNG.*;

public class BattalionManager {

    //Batalion Czołgów (Tank Battalion)
    public static class TankBattalion extends BaseGroup {
        public TankBattalion(String name, SimPosition position, SimForceType forceType, int initialUnits) {
            super(name, position, forceType);
            SimUnit unit1 = new UnitManager.Abrams(initialUnits);
            this.addUnit(unit1);
            totalInitialUnits = units.stream().mapToInt(SimUnit::getInitialUnits).sum();
        }

        @Override
        public void init() {
            super.init();
        }
    }

    //Batalion Zmechanizowany (Mechanized Battalion)
    public static class MechanizedBattalion extends BaseGroup {
        public MechanizedBattalion(String name, SimPosition position, SimForceType forceType, int initialUnits) {
            super(name, position, forceType);
            SimUnit unit1 = new UnitManager.Abrams(initialUnits);
            SimUnit unit2 = new UnitManager.BWP(initialUnits);
            this.addUnit(unit1);
            this.addUnit(unit2);
            totalInitialUnits = units.stream().mapToInt(SimUnit::getInitialUnits).sum();
        }

        @Override
        public void init() {
            super.init();
        }
    }

    //Batalion Piechoty (Infantry Battalion)
    public static class InfantryBattalion extends BaseGroup {
        public InfantryBattalion(String name, SimPosition position, SimForceType forceType, int initialUnits) {
            super(name, position, forceType);
            SimUnit unit1 = new UnitManager.Soldier(initialUnits);
            this.addUnit(unit1);
            totalInitialUnits = units.stream().mapToInt(SimUnit::getInitialUnits).sum();
        }

        @Override
        public void init() {
            super.init();
        }
    }

    //Batalion Artylerii (Artillery Battalion)
    public static class ArtilleryBattalion extends BaseGroup {
        public ArtilleryBattalion(String name, SimPosition position, SimForceType forceType, int initialUnits) {
            super(name, position, forceType);
            SimUnit unit1 = new UnitManager.Krab(initialUnits);
            this.addUnit(unit1);
            totalInitialUnits = units.stream().mapToInt(SimUnit::getInitialUnits).sum();
        }

        @Override
        public void init() {
            super.init();
        }
    }
}
