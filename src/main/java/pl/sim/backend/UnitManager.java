package pl.sim.backend;

import pl.simNG.SimUnit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Klasa zarządzająca tworzeniem jednostek na podstawie parametrów z pliku konfiguracyjnego.
 * Plik konfiguracyjny (`unit_attributes.properties`) zawiera szczegółowe atrybuty każdej jednostki.
 */
public class UnitManager {

    /** Obiekt do przechowywania właściwości wczytanych z pliku `unit_attributes.properties`. */
    private static final Properties properties = new Properties();

    public static double randomFactor;

    //Statyczny blok inicjalizacyjny odpowiedzialny za wczytywanie właściwości z pliku
    static {
        try (InputStream input = UnitManager.class.getClassLoader().getResourceAsStream("unit_attributes.properties")) {
            if (input == null) {
                throw new IllegalStateException("Nie znaleziono pliku unit_attributes.properties");
            }
            properties.load(input);

            randomFactor = Double.parseDouble(properties.getProperty("randomFactor", "0.25"));

        } catch (IOException e) {
            throw new RuntimeException("Błąd podczas wczytywania pliku unit_attributes.properties", e);
        }
    }

    /**
     * Tworzy instancję `SimUnit` na podstawie identyfikatora jednostki i liczby początkowych podjednostek.
     *
     * @param unitId Identyfikator jednostki (np. "W1", "W2" z pliku konfiguracyjnego).
     * @param initialUnits Liczba początkowych podjednostek w grupie.
     * @return Nowa instancja klasy `SimUnit`.
     */
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
