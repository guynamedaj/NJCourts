package edu.njit.njcourts.data;

public final class ApiConstants {
    // Emulator default: 10.0.2.2 resolves to the host machine's localhost.
    // For a physical phone on the same WiFi, replace with your laptop's LAN IP,
    // e.g. "http://192.168.1.42:3000/api/". Trailing slash is required.
    public static final String BASE_URL = "http://10.0.2.2:3000/api/";

    public static final String API_KEY = "dev-local-key-change-me";

    private ApiConstants() {}
}
