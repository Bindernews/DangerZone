package com.vortexel.dangerzone.common.config;

import com.google.common.collect.Maps;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.Map;

public class DZConfig {

    public static final int REAL_MAX_DANGER_LEVEL = 500;

    private static final String WORLD_PREFIX = "world";

    public static DZConfig INSTANCE = new DZConfig();

    public General general = new General();
    public Advanced advanced = new Advanced();
    public BiomesConfig biomes = new BiomesConfig();
    public Map<Integer, PerWorld> world = Maps.newHashMap();

    public static class General implements ConfigHelper.Loadable {
        @Comment({"Time in seconds between cache clears. Increasing this can improve performance slightly",
                "at the cost of memory usage"})
        @Config.RangeInt(min = 60)
        public int cacheClearTime =  60 * 5;

        @Comment({"The maximum danger level. An enemy's difficulty can be anywhere within its danger level",
                "so the number of levels should be proportional to the danger multiplier."})
        @Config.RangeInt(min = 1, max = REAL_MAX_DANGER_LEVEL)
        public int maxDangerLevel = 20;

        @Comment({"The maximum danger multiplier. Make this higher to make the toughest enemies tougher."})
        @Config.RangeDouble(min = 1.0, max = REAL_MAX_DANGER_LEVEL)
        public double dangerMultiplier = 10.0;

        @Comment({"How much health is gained for each added difficulty point."})
        public double healthScaling = 0.5;

        @Comment({"The minimum danger level where enemies can start having haste."})
        @Config.RangeInt(min = 1, max = REAL_MAX_DANGER_LEVEL)
        public int hasteLevel = 10;

        @Comment({"The chance of applying haste at the maximum danger level. This will scale to lower levels."})
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double hasteChance = 0.6;

        @Comment({"The minimum danger level where enemies can regenerate health."})
        @Config.RangeInt(min = 1, max = REAL_MAX_DANGER_LEVEL)
        public int regenLevel = 16;

        @Comment({"The chance of applying regeneration at the maximum danger level. This will scale to lower levels."})
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double regenChance = 0.7;

        public void load(ConfigCategory cat) {
            ConfigHelper.loadAllCommented(this, cat);
        }
    }

    public static class Advanced implements ConfigHelper.Loadable {

        public void load(ConfigCategory cat) {
            ConfigHelper.loadAllCommented(this, cat);
        }
    }

    public static class PerWorld implements ConfigHelper.Loadable {
        @Comment({"Controls whether or not Danger Zone is enabled for this dimension. The default is false."})
        public boolean enabled = false;

        @Comment({"The radius around spawn where difficulty is always 0.",
                "No mobs will spawn here."})
        @Config.RangeInt(min = 0)
        public int spawnRadius = 16;

        @Comment({"The radius around spawnRadius where the difficulty transitions from 0 to its real value.",
                "This acts as a safety buffer where mobs can spawn, but you won't be dumped into super-hard mode."})
        @Config.RangeInt(min = 0)
        public int spawnTransitionRadius = 64;

        @Comment({"The scale factor used to influence how the difficulty map is generated.",
                "DO NOT change this unless you really know what you are doing!"})
        @Config.RangeDouble(min = 0.000000001, max = 0.01)
        public double scaleFactor = 0.001;

        public void load(ConfigCategory cat) {
            ConfigHelper.loadAllCommented(this, cat);
        }
    }

    public PerWorld getWorld(int dimensionId) {
        world.putIfAbsent(dimensionId, new PerWorld());
        return world.get(dimensionId);
    }

    public void load(Configuration cfgGeneral, Configuration cfgBiomes) {
        general.load(cfgGeneral.getCategory("general"));
        advanced.load(cfgGeneral.getCategory("advanced"));
        ConfigHelper.loadIntegerMap(cfgGeneral, WORLD_PREFIX, PerWorld.class, world);
        biomes.load(cfgBiomes.getCategory("biomes"));
    }

    public void loadFromDirectory(File configDir) {
        Configuration cfgGeneral = new Configuration(new File(configDir, "general.cfg"));
        Configuration cfgBiomes = new Configuration(new File(configDir, "biomes.cfg"));
        cfgGeneral.load();
        cfgBiomes.load();
        DZConfig.INSTANCE.load(cfgGeneral, cfgBiomes);
        cfgGeneral.save();
        cfgBiomes.save();
    }
}
