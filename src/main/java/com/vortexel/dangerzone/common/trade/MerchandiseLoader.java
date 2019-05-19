package com.vortexel.dangerzone.common.trade;

import com.google.common.collect.Maps;
import com.google.gson.JsonParseException;
import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.util.MCUtil;
import lombok.val;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Map;

public class MerchandiseLoader {

    private static final String OTHER_ASSETS = "assets/" + DangerZone.MOD_ID + "/other/merchandise/";
    private static final String MERCHANDISE_FILE = "merchandise.json";

    private static final Map<String, String> MODS_TO_MERCHANDISE = Maps.newHashMap();
    static {
        val m = MODS_TO_MERCHANDISE;
        m.put("twilightforest", "twilightforest.json");
    }

    public static void loadDefaultMerchandise() {
        val merchFile = new File(DangerZone.getMod().getConfigDir(), MERCHANDISE_FILE);
        // Export the default file if it doesn't exist
        if (!merchFile.exists()) {
            try (
                    val reader = MCUtil.openResource(OTHER_ASSETS + "default.json");
                    val writer = new FileWriter(merchFile);
            ) {
                IOUtils.copy(reader, writer);
            } catch (IllegalArgumentException | IOException e) {
                DangerZone.getLog().error(e);
            }
        }
        // Load the file
        if (merchFile.isFile()) {
            try {
                DangerZone.getMod().getMerchandise().addFromReader(new FileReader(merchFile));
            } catch (IOException | JsonParseException e) {
                DangerZone.getLog().error(e);
            }
        } else {
            DangerZone.getLog().warn("No default merchandise file found.");
        }
    }

    public static void loadModMerchandise() {
        for (val mod : Loader.instance().getActiveModList()) {
            val merchFilename = MODS_TO_MERCHANDISE.get(mod.getModId());
            if (merchFilename != null) {
                loadFile(merchFilename);
            }
        }
    }

    private static void loadFile(String filename) {
        val fullPath = OTHER_ASSETS + filename;
        try {
            DangerZone.getMod().getMerchandise().addFromReader(MCUtil.openResource(fullPath));
        } catch (IllegalArgumentException | JsonParseException e) {
            DangerZone.getLog().error(e);
        }
    }
}
