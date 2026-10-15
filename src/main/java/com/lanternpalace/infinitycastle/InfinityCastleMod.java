package com.lanternpalace.infinitycastle;

import com.lanternpalace.infinitycastle.worldgen.CastleFeature;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfinityCastleMod implements ModInitializer {
    public static final String MOD_ID = "lantern_palace";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        // The feature is referenced by data/lantern_palace/worldgen/{configured,placed}_feature/castle.json and by the
        // lantern_void biome, so it must be registered before the world's data registries load.
        Registry.register(BuiltInRegistries.FEATURE, id("castle"), new CastleFeature());
        CastleCommands.register();
        PeacefulRules.register();
        TravelManager.register();
        LOGGER.info("Infinite Lantern Palace loaded: /infinitycastle enter | exit");
    }
}
