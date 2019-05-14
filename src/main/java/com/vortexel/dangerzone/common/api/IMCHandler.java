package com.vortexel.dangerzone.common.api;

import com.google.common.collect.Maps;
import com.vortexel.dangerzone.DangerZone;
import lombok.val;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.Validate;

import java.io.StringReader;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Handle IMC messages from other mods. This allows exposing a limited API to other mods without having to publish
 * an API jar file or anything. It also means they don't have to have a soft-dependency on this mod.
 */
public class IMCHandler {

    public static final String IMC_KEY_MERCHANDISE = "AddOffers";
    public static final String IMC_KEY_ENTITY_CONFIG = "EntityConfig";

    public static final Map<String, Consumer<FMLInterModComms.IMCMessage>> HANDLERS = Maps.newHashMap();
    static {
        HANDLERS.put(IMC_KEY_MERCHANDISE, IMCHandler::handleMerchandiseMessage);
        HANDLERS.put(IMC_KEY_ENTITY_CONFIG, IMCHandler::handleEntityConfigMessage);
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(IMCHandler.class);
    }

    @SubscribeEvent
    public static void onIMCEvent(FMLInterModComms.IMCEvent event) {
        for (val msg : event.getMessages()) {
            val handler = HANDLERS.get(msg.key);
            if (handler != null) {
                handler.accept(msg);
            } else {
                DangerZone.log.warn("Received unknown IMC message: {}", msg.key);
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
}
