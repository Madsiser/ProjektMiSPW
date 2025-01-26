package pl.sim.backend;

import pl.simNG.SimUnit;

public class UnitManager {

    public static class Abrams extends SimUnit {
        public Abrams(int initialUnits) {
            super("Abrams", "tank", 10, 5, 3, initialUnits, 50, 2.0, 2.0, 3.0, 2.0, 350.0, 300.0,2.0);
        }
    }

    public static class BWP extends SimUnit {
        public BWP(int initialUnits) {
            super("BWP", "combat vehicle", 8, 4, 4, initialUnits, 60, 3.0, 3.0, 2.5, 1.8, 150.0,200.0, 3.0);
        }
    }

    public static class Soldier extends SimUnit {
        public Soldier(int initialUnits) {
            super("Soldier", "soldier", 5, 2, 1, initialUnits, 100, 1.0, 1.0, 0.5, 1.7, 10.0,20.0,5);
        }
    }

    public static class Krab extends SimUnit {
        public Krab(int initialUnits) {
            super("Krab", "artillery", 15, 10, 2, initialUnits, 40, 4.0, 4.0, 3.5, 2.5, 200.0,300.0,2.0);
        }
    }
}