package pl.sim.backend;

import pl.simNG.SimUnit;

public class UnitManager {

    public static class Abrams extends SimUnit {
        public Abrams(int initialUnits) {
            super("Abrams", "tank", 10, 5, 3, initialUnits, 50, 0.9, 0.2, 1.0);
        }
    }

    public static class BWP extends SimUnit {
        public BWP(int initialUnits) {
            super("BWP", "combat vehicle", 8, 4, 4, initialUnits, 30, 0.8, 0.4, 0.9);
        }
    }

    public static class Soldier extends SimUnit {
        public Soldier(int initialUnits) {
            super("Soldier", "soldier", 5, 2, 6, initialUnits, 10, 0.6, 0.8, 0.7);
        }
    }

    public static class Krab extends SimUnit {
        public Krab(int initialUnits) {
            super("Krab", "artillery", 12, 8, 2, initialUnits, 40, 0.7, 0.5, 1.2);
        }
    }
}