package com.vortexel.dangerzone.common.config;

import com.google.common.collect.Maps;
import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.Consts;
import com.vortexel.dangerzone.common.Reflector;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;

import java.lang.reflect.Field;
import java.util.Map;

@Config(modid = DangerZone.MOD_ID, category = "main")
public class DZConfig {

    @Config.Ignore
    private static final String LANG = DangerZone.MOD_ID + ".config.";

    @Config.Ignore
    public static String configFile;

    @Config.Comment({"General settings which apply to the mod as a whole."})
    public static General general;

    @Config.Comment({"Options for various effects and how and when they are applied."})
    public static Effects effects;

    @Config.Name("World Defaults")
    @Config.Comment({"Default per-world config values. Override these on a per-world basis in \"worlds\"."})
    public static WorldConfig worldDefaults;

    @Config.Ignore
    @Config.Comment({"Per-dimension configuration values. They override the values in \"world\"."})
    public static Map<Integer, WorldConfig> worlds;

    static {
        general = new General();
        effects = new Effects();
        worldDefaults = new WorldConfig();
        worlds = Maps.newHashMap();

        getWorld(0).enabled = true;
    }

    public static class General {
        @Config.LangKey(LANG + "max_danger_level")
        @Config.Comment({"The maximum danger level. An enemy's difficulty can be anywhere within its danger level",
                "so the number of levels should be proportional to the danger multiplier."})
        @Config.RangeInt(min = 1, max = Consts.MAX_DANGER_LEVEL)
        public int maxDangerLevel = 100;

        @Config.Comment({"The maximum danger multiplier. Make this higher to make the toughest enemies tougher."})
        @Config.RangeDouble(min = 1.0, max = Consts.MAX_DANGER_LEVEL)
        public double dangerMultiplier = 100.0;

        @Config.LangKey(LANG + "looting_non_player_kills")
        @Config.Comment({"Should the increased looting level be applied even if the mob wasn't killed by a player?"})
        public boolean lootingOnNonPlayerKills = true;

        @Config.LangKey(LANG + "loot_coins_non_player_kills")
        @Config.Comment({"Should mobs drop loot coins even if they weren't killed by a player?"})
        public boolean lootCoinsOnNonPlayerKills = false;

        @Config.Comment({"Should Fake Players (e.g. Draconic Evolution mob_grinder) count as player kills?",
                "If this is true, then players can build a mob grinder and get ores from it. Be careful."})
        public boolean doFakePlayersDropLoot = false;

        @Config.Comment({"Numbers will generate using the entity's danger level +/- this value."})
        @Config.RangeInt(min = 0, max = Consts.MAX_DANGER_LEVEL)
        public int levelRange = 4;

        @Config.Comment({"Number of loot coins dropped per mob level. (default 0.25)"})
        @Config.RangeDouble(min = 0.0)
        public double coinsPerLevel = 0.25;

        @Config.Comment({"The stretch factor used to influence how the difficulty map is generated.",
                "DO NOT change this unless you really know what you are doing!"})
        @Config.RangeDouble(min = Consts.NOT_ZERO, max = 0.01)
        public double stretchFactor = 0.0002;
    }

    public static class Effects {

        @Config.Comment({"The duration (in seconds) of the Wither effect inflicted by decay-touch."})
        @Config.RangeDouble(min = 0.0)
        public double decayTouchTime = 4.0;
    }

    public static class WorldConfig implements ConfigHelper.Loadable, Cloneable {
        @Config.Comment({"Controls whether or not Danger Zone is enabled for this dimension."})
        public boolean enabled = false;

        @Config.Comment({"The radius around spawn where difficulty is always 0."})
        @Config.RangeInt(min = 0)
        public int spawnRadius = 96;

        @Config.Comment({"The radius around spawnRadius where the difficulty transitions from 0 to its real value.",
                "This acts as a safety buffer where mobs can spawn, but you won't be dumped into super-hard mode."})
        @Config.RangeInt(min = 0)
        public int spawnTransitionRadius = 256;

        public void load(ConfigCategory cat) {
            ConfigHelper.loadAllCommented(this, cat);
        }

        @Override
        public Object clone() {
            try {
                WorldConfig o = (WorldConfig) super.clone();
                o.enabled = enabled;
                o.spawnRadius = spawnRadius;
                o.spawnTransitionRadius = spawnTransitionRadius;
                return o;
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static WorldConfig getWorld(int dimensionId) {
        worlds.computeIfAbsent(dimensionId, (k) -> (WorldConfig) worldDefaults.clone());
        return worlds.get(dimensionId);
    }

    private static void load() {
        Configuration cfg = Reflector.callStaticMethod(ConfigManager.class, "getConfiguration",
                new Class<?>[] {String.class, String.class}, DangerZone.MOD_ID, null);

        ConfigCategory worldsCat = cfg.getCategory("main.worlds");
        worldsCat.setComment(ConfigHelper.getComment(DZConfig.class, "worlds"));
        ConfigHelper.loadIntegerMap(worldsCat, worlds, WorldConfig::new);
        cfg.save();
    }

    public static void loadAll() {
        ConfigManager.sync(DangerZone.MOD_ID, Config.Type.INSTANCE);
        BiomeConfig.afterLoad();
        load();
    }
}
