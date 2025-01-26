package pl.sim.backend;

import pl.simNG.SimGroup;
import pl.simNG.SimUnit;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {

    private static final String LOG_FILE_PATH = "simulation_log.txt";

    static {
        try (PrintWriter writer = new PrintWriter(LOG_FILE_PATH)) {
            writer.print("");
        } catch (IOException e) {
            System.err.println("Błąd podczas czyszczenia pliku logów: " + e.getMessage());
        }
    }

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
                        unit.getTotalCurrentAmmunition(),
                        unit.getInitialAmmunition()*unit.getInitialUnits(),
                        unit.getShootingRange()
                ));
            }
        }
        System.out.println(logEntry);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE_PATH, true))) {
            writer.write(logEntry.toString());
        } catch (IOException e) {
            System.err.println("Błąd podczas zapisywania logów do pliku: " + e.getMessage());
        }
    }
}
