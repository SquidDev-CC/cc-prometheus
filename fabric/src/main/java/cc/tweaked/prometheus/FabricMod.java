package cc.tweaked.prometheus;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dan200.computercraft.fabric.mixin.LevelResourceAccess;
import dan200.computercraft.shared.util.CommentedConfigSpec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Files;

public class FabricMod implements ModInitializer {
    private static final LevelResource configDir = LevelResourceAccess.create("serverconfig");
    private static final String configName = "ccprometheus.toml";

    private static final CommentedConfigSpec configSpec = new CommentedConfigSpec();

    static {
        configSpec.comment("host", Config.HOST_HELP);
        configSpec.define("host", Config.HOST_DEFAULT);

        configSpec.comment("port", Config.PORT_HELP);
        configSpec.defineInRange("port", Config.PORT_DEFAULT, Config.PORT_MIN, Config.PORT_MAX);

        configSpec.comment("vanilla", Config.VANILLA_HELP);
        configSpec.define("vanilla", Config.VANILLA_DEFAULT);

        configSpec.comment("jvm", Config.JVM_HELP);
        configSpec.define("jvm", Config.JVM_DEFAULT);
    }

    private Config config;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);

        // Need to run after CC:R has reset the tracker. Some additional ugliness due to
        // https://github.com/QuiltMC/quilted-fabric-api/issues/12
        var phase = new ResourceLocation(Constants.MOD_ID, "after_cc");
        ServerLifecycleEvents.SERVER_STARTED.addPhaseOrdering(Event.DEFAULT_PHASE, phase);
        ServerLifecycleEvents.SERVER_STARTED.addPhaseOrdering(new ResourceLocation("quilt", "default"), phase);
        ServerLifecycleEvents.SERVER_STARTED.register(phase, server -> ServerMetrics.onServerStart(server, config));

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> ServerMetrics.onServerStop());
        ServerTickEvents.END_SERVER_TICK.register(server -> ServerMetrics.onServerTick());
    }

    private void onServerStarting(MinecraftServer server) {
        var configPath = server.getWorldPath(configDir).resolve(configName);
        var configBuilder = CommentedFileConfig.builder(configPath)
            .onFileNotFound((path, format) -> {
                Files.createDirectories(path.getParent());
                format.initEmptyFile(path);
                return false;
            })
            .preserveInsertionOrder();

        try (var config = configBuilder.build()) {
            config.load();
            configSpec.correct(config);
            config.save();

            this.config = new Config(config.get("host"), config.get("port"), config.get("vanilla"), config.get("jvm"));
        }
    }
}
