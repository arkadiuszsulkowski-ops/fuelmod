package pl.sula008.fuelmod.client;

public class FuelClientData {

    private static long fuel;
    private static long maxFuel;
    private static boolean hasData = false;

    public static void set(long fuelIn, long maxFuelIn) {
        fuel = fuelIn;
        maxFuel = maxFuelIn;
        hasData = true;
    }

    public static long getFuel() {
        return fuel;
    }

    public static long getMaxFuel() {
        return maxFuel;
    }

    public static boolean hasData() {
        return hasData;
    }
}
