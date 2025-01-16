package pl.sim.backend;

import pl.simNG.SimGroup;
import pl.simNG.SimUnit;

public class Logger {

    public static void log(SimGroup group, String action, int simulationTime) {
        StringBuilder logEntry = new StringBuilder();

        logEntry.append(String.format("[%04d]:[%s] %s\n", simulationTime, group.getName(), action));

        if (!group.getUnits().isEmpty()) {
            logEntry.append("STAN GRUPY:");
            for (SimUnit unit : group.getUnits()) {
                logEntry.append(String.format(
                        " Jednostka: %s | Aktywne: %d/%d | Amunicja: %d/%d | Zasięg: %d |",
                        unit.getName(),
                        unit.getActiveUnits(),
                        unit.getInitialUnits(),
                        unit.getCurrentAmmunition(),
                        unit.getInitialAmmunition(),
                        unit.getShotRange()
                ));
            }
        } else {
            logEntry.append("Grupa nie posiada żadnych jednostek.");
        }
        System.out.println(logEntry);
    }
}
