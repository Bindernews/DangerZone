package com.vortexel.dangerzone.common.trade;

import com.google.common.collect.Maps;
import com.google.gson.JsonParseException;
import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.util.MCUtil;
import lombok.val;
import net.minecraftforge.fml.common.Loader;

import java.util.Map;

public class MerchandiseLoader {

    private static final String OTHER_ASSETS = "assets/" + DangerZone.MOD_ID + "/other/merchandise/";

    private static final Map<String, String> MODS_TO_MERCHANDISE = Maps.newHashMap();
    static {
        val m = MODS_TO_MERCHANDISE;
        m.put("twilightforest", "twilightforest.json");
    }

    public static void loadDefaultMerchandise() {
        loadFile("default.json");
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
