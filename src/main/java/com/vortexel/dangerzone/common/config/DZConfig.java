package com.vortexel.dangerzone.common.config;

import com.google.common.collect.Maps;
import com.vortexel.dangerzone.DangerZone;
import lombok.val;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.ConfigCategory;

import java.util.Map;

@Config(modid = DangerZone.ID, type = Config.Type.INSTANCE)
public class DZConfig {

    public static class C {
        public static final String WORLD_PREFIX = "world";
        public static final int REAL_MAX_DANGER_LEVEL = 500;
    }

    @Comment({"General settings which apply to the mod as a whole."})
    @Config.Name("general")
    public static General general;

    @Comment({"Map of biome-group names to lists of the biome IDs in each group."})
    @Config.Name("biomes")
    public static Map<String, Integer[]> biomes;

    @Config.RequiresMcRestart
    public static Map<Integer, PerWorld> world;


    static {
        initialize();
    }

    public static void initialize() {
        general = new General();
        biomes = Maps.newHashMap();
        world = Maps.newHashMap();

        put("ocean", 0, 10, 24);
        put("plains", 1, 129);
        put("desert", 2, 17, 130);
        put("hills", 3, 20, 34, 131);
        put("forest", 4, 132);
        put("taiga", 5, 19, 30, 31, 32, 33, 133, 158, 160, 161);
        put("swamp", 6, 134);
        put("river", 7, 10);
        put("hell", 8);
        put("sky", 9);
        put("ice", 12, 13, 140);
        put("mushroom", 14, 15);
        put("beach", 16, 25, 26);
        put("jungle", 21, 22, 23, 149, 151);
        put("birch", 27, 28, 155, 156);
        put("roofed_forest", 29, 157);
        put("savanna", 35, 36, 163, 164);
        put("mesa", 37, 38, 39, 165, 166, 167);
        put("void", 127);
    }

    private static void put(String key, Integer... elements) {
        biomes.put(key, elements);
    }

    public static class General {
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

        @Comment({"How much health is gained for each added difficulty point."})
        public double healthScaling = 0.5;

        @Comment({"Should the increased looting level be applied even if the mob wasn't killed by a player?"})
        public boolean lootingOnNonPlayerKills = true;

        @Comment({"Should mobs drop loot bags even if they weren't killed by a player?"})
        public boolean lootBagOnNonPlayerKills = false;

        @Comment({"Should Fake Players (e.g. Draconic Evolution mob_grinder) count as player kills?",
                "If this is true, then players can build a mob grinder and get ores from it. Be careful."})
        public boolean doFakePlayersDropLoot = false;

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

    public static PerWorld getWorld(int dimensionId) {
        world.putIfAbsent(dimensionId, new PerWorld());
        return world.get(dimensionId);
    }

    public static String getBiomeGroup(int id) {
        for (val entry : biomes.entrySet()) {
            for (val listVal : entry.getValue()) {
                if (listVal == id) {
                    return entry.getKey();
                }
            }
        }
        return "*";
    }

//    public void load(Configuration cfgGeneral, Configuration cfgBiomes) {
//        general.load(cfgGeneral.getCategory("general"));
//        ConfigHelper.loadIntegerMap(cfgGeneral, WORLD_PREFIX, PerWorld.class, world);
//        biomes.load(cfgBiomes.getCategory("biomes"));
//    }
//
//    public void loadFromDirectory(File configDir) {
//        Configuration cfgGeneral = new Configuration(new File(configDir, "general.cfg"));
//        Configuration cfgBiomes = new Configuration(new File(configDir, "biomes.cfg"));
//        cfgGeneral.load();
//        cfgBiomes.load();
//        DZConfig.INSTANCE.load(cfgGeneral, cfgBiomes);
//        cfgGeneral.save();
//        cfgBiomes.save();
//    }
}
