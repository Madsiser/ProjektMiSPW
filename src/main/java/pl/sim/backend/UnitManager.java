package pl.sim.backend;

import pl.simNG.SimUnit;

public class UnitManager {

    public static class Abrams extends SimUnit {
        public Abrams(int initialUnits) {
            super("Abrams", "tank", 10, 5, 3, initialUnits, 50, 0.7, 0.8, 2.0, 3.0, 3);
        }
    }

    public static class BWP extends SimUnit {
        public BWP(int initialUnits) {
            super("BWP", "combat vehicle", 8, 4, 4, initialUnits, 60, 0.6, 0.8, 1.0, 2.0, 4);
        }
    }

    public static class Soldier extends SimUnit {
        public Soldier(int initialUnits) {
            super("Soldier", "soldier", 5, 2, 1, initialUnits, 100, 0.5, 0.7, 0.1, 0.2, 5);
        }
    }

    public static class Krab extends SimUnit {
        public Krab(int initialUnits) {
            super("Krab", "artillery", 15, 10, 2, initialUnits, 40, 0.6, 0.9, 1.0, 2.5, 2);
        }
    }
}