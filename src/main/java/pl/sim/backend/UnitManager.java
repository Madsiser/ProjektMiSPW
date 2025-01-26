package pl.sim.backend;

import pl.simNG.SimUnit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class UnitManager {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = UnitManager.class.getClassLoader().getResourceAsStream("unit_attributes.properties")) {
            if (input == null) {
                throw new IllegalStateException("Nie znaleziono pliku unit_attributes.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Błąd podczas wczytywania pliku unit_attributes.properties", e);
        }
    }

    public static SimUnit createUnit(String unitId, int initialUnits) {
        String prefix = unitId + ".";
        return new SimUnit(
                properties.getProperty(prefix + "name"),
                properties.getProperty(prefix + "type"),
                Integer.parseInt(properties.getProperty(prefix + "visibilityRange")),
                Integer.parseInt(properties.getProperty(prefix + "shootingRange")),
                Integer.parseInt(properties.getProperty(prefix + "speed")),
                initialUnits,
                Integer.parseInt(properties.getProperty(prefix + "initialAmmunition")),
                Double.parseDouble(properties.getProperty(prefix + "horizontalDeviation")),
                Double.parseDouble(properties.getProperty(prefix + "verticalDeviation")),
                Double.parseDouble(properties.getProperty(prefix + "width")),
                Double.parseDouble(properties.getProperty(prefix + "height")),
                Double.parseDouble(properties.getProperty(prefix + "armorThickness")),
                Double.parseDouble(properties.getProperty(prefix + "armorPenetration")),
                Double.parseDouble(properties.getProperty(prefix + "fireIntensity"))
        ) {};
    }
}
