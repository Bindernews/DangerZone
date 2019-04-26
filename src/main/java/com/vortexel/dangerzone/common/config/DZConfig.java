package com.vortexel.dangerzone.common.config;

import com.google.common.collect.Maps;
import com.vortexel.dangerzone.DangerZone;
import lombok.SneakyThrows;
import lombok.val;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;

import java.util.Map;

public class DZConfig {

    /**
     * The configuration we load from and save to.
     */
    public static Configuration cfg;

    /**
     * Constants.
     */
    public static class C {
        public static final String WORLDS_PREFIX = "worlds";
        public static final int REAL_MAX_DANGER_LEVEL = (1 << 15);
    }

    @Comment({"General settings which apply to the mod as a whole."})
    public static General general;

    @Comment({"Options for various effects and how and when they are applied."})
    public static Effects effects;

    @Comment({"Default per-world config values. Override these on a per-world basis in \"worlds\"."})
    public static WorldConfig world;

    @Comment({"Per-dimension configuration values. They override the values in \"world\"."})
    public static Map<Integer, WorldConfig> worlds;

    static {
        initialize();
    }

    public static void initialize() {
        general = new General();
        effects = new Effects();
        world = new WorldConfig();
        worlds = Maps.newHashMap();

        getWorld(0).enabled = true;
    }

    public static class General implements ConfigHelper.Loadable {
        @Comment({"Time in seconds between cache clears. Increasing this can improve performance slightly",
                "at the cost of memory usage"})
        @Config.RangeInt(min = 60)
        public int cacheClearTime =  60 * 5;

        @Comment({"The maximum danger level. An enemy's difficulty can be anywhere within its danger level",
                "so the number of levels should be proportional to the danger multiplier."})
        @Config.RangeInt(min = 1, max = C.REAL_MAX_DANGER_LEVEL)
        public int maxDangerLevel = 20;

        @Comment({"The maximum danger multiplier. Make this higher to make the toughest enemies tougher."})
        @Config.RangeDouble(min = 1.0, max = C.REAL_MAX_DANGER_LEVEL)
        public double dangerMultiplier = 10.0;

        @Comment({"Should the increased looting level be applied even if the mob wasn't killed by a player?"})
        public boolean lootingOnNonPlayerKills = true;

        @Comment({"Should mobs drop loot bags even if they weren't killed by a player?"})
        public boolean lootBagOnNonPlayerKills = false;

        @Comment({"Should Fake Players (e.g. Draconic Evolution mob_grinder) count as player kills?",
                "If this is true, then players can build a mob grinder and get ores from it. Be careful."})
        public boolean doFakePlayersDropLoot = false;

        @Comment({"The scale factor used to influence how the difficulty map is generated.",
                "DO NOT change this unless you really know what you are doing!"})
        @Config.RangeDouble(min = 0.000000001, max = 0.01)
        public double scaleFactor = 0.001;

        public void load(ConfigCategory cat) {
            ConfigHelper.loadAllCommented(this, cat);
        }
    }

    public static class Effects implements ConfigHelper.Loadable {

        @Comment({"How much health is gained for each added difficulty point."})
        public double healthScaling = 0.5;

        @Comment({"The minimum danger level where enemies can start having haste."})
        @Config.RangeInt(min = 1, max = C.REAL_MAX_DANGER_LEVEL)
        public int hasteLevel = 10;

        @Comment({"The chance of applying haste at the maximum danger level. This will scale to lower levels."})
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double hasteChance = 0.6;

        @Comment({"The minimum danger level where enemies can regenerate health."})
        @Config.RangeInt(min = 1, max = C.REAL_MAX_DANGER_LEVEL)
        public int regenLevel = 16;

        @Comment({"The chance of applying regeneration at the maximum danger level. This will scale to lower levels."})
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double regenChance = 0.7;

        public void load(ConfigCategory cat) {
            ConfigHelper.loadAllCommented(this, cat);
        }
    }

    public static class WorldConfig implements ConfigHelper.Loadable, Cloneable {
        @Comment({"Controls whether or not Danger Zone is enabled for this dimension."})
        public boolean enabled = false;

        @Comment({"The radius around spawn where difficulty is always 0. No mobs will spawn here."})
        @Config.RangeInt(min = 0)
        public int spawnRadius = 16;

        @Comment({"The radius around spawnRadius where the difficulty transitions from 0 to its real value.",
                "This acts as a safety buffer where mobs can spawn, but you won't be dumped into super-hard mode."})
        @Config.RangeInt(min = 0)
        public int spawnTransitionRadius = 64;

        public void load(ConfigCategory cat) {
            ConfigHelper.loadAllCommented(this, cat);
        }

        @Override
        @SneakyThrows(CloneNotSupportedException.class)
        public Object clone() {
            val o = (WorldConfig)super.clone();
            o.enabled = enabled;
            o.spawnRadius = spawnRadius;
            o.spawnTransitionRadius = spawnTransitionRadius;
            return o;
        }
    }

    public static WorldConfig getWorld(int dimensionId) {
        worlds.computeIfAbsent(dimensionId, (k) -> (WorldConfig)world.clone());
        return worlds.get(dimensionId);
    }

    public static void load() {
        cfg.load();
        ConfigHelper.loadStaticCategories(cfg, DZConfig.class, "general", "effects", "world");
        cfg.getCategory(C.WORLDS_PREFIX).setComment(ConfigHelper.getComment(DZConfig.class, "worlds"));
        ConfigHelper.loadIntegerMap(cfg, C.WORLDS_PREFIX, worlds, WorldConfig::new);
        cfg.save();
    }

    public static void loadAll() {
        ConfigManager.sync(DangerZone.MOD_ID, Config.Type.INSTANCE);
        BiomeConfig.afterLoad();
        load();
    }
}
