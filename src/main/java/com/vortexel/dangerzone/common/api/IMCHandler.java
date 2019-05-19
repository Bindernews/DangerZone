package com.vortexel.dangerzone.common.api;

import com.google.common.collect.Maps;
import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.api.IDangerZoneAPI;
import lombok.val;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import org.apache.commons.lang3.Validate;

import java.io.StringReader;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Handle IMC messages from other mods. This allows exposing a limited API to other mods without having to publish
 * an API jar file or anything. It also means they don't have to have a soft-dependency on this mod.
 */
public class IMCHandler {

    public static final String IMC_KEY_MERCHANDISE = "addOffers";
    public static final String IMC_KEY_ENTITY_CONFIG = "addEntityConfig";
    public static final String IMC_KEY_GET_API = "getAPI";

    public static final Map<String, Consumer<FMLInterModComms.IMCMessage>> HANDLERS = Maps.newHashMap();
    static {
        HANDLERS.put(IMC_KEY_MERCHANDISE, IMCHandler::handleMerchandiseMessage);
        HANDLERS.put(IMC_KEY_ENTITY_CONFIG, IMCHandler::handleEntityConfigMessage);
        HANDLERS.put(IMC_KEY_GET_API, IMCHandler::handleGetAPI);
    }

    public static void init() {
    }

    /**
     * Called from DangerZone to deal with IMCEvents.
     */
    public static void onIMCEvent(FMLInterModComms.IMCEvent event) {
        for (val msg : event.getMessages()) {
            val handler = HANDLERS.get(msg.key);
            if (handler != null) {
                handler.accept(msg);
            } else {
                DangerZone.getLog().warn("Received unknown IMC message: {}", msg.key);
            }
        }
    }

    public static void handleMerchandiseMessage(FMLInterModComms.IMCMessage msg) {
        Validate.isTrue(msg.isStringMessage());
        DangerZone.proxy.getMerchandise().addFromReader(new StringReader(msg.getStringValue()));
    }

    public static void handleEntityConfigMessage(FMLInterModComms.IMCMessage msg) {
        Validate.isTrue(msg.isStringMessage());
        DangerZone.proxy.getEntityConfigManager().addFile(msg.getStringValue());
    }

    public static void handleGetAPI(FMLInterModComms.IMCMessage msg) {
        Validate.isTrue(msg.isFunctionMessage());
        val fnOpt = msg.getFunctionValue(IDangerZoneAPI.class, Void.class);
        if (fnOpt.isPresent()) {
            fnOpt.get().apply(DangerZone.getMod().getAPI());
        }
    }
}
