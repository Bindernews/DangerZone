package com.vortexel.dangerzone.api;

public class DangerZoneAPI {

    private static IDangerZoneAPI implementation;

    public static void setupAPI(IDangerZoneAPI impl) {
        implementation = impl;
    }

    public static IDangerZoneAPI getAPI() {
        return implementation;
    }

    private DangerZoneAPI() {}
}
