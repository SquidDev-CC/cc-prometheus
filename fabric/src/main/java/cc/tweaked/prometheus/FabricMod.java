package cc.tweaked.prometheus;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Files;

public class FabricMod implements ModInitializer {
    private static final LevelResource configDir;
    private static final String configName = "ccprometheus-server.toml";

    static {
        try {
            var constructor = LevelResource.class.getDeclaredConstructor(String.class);
            constructor.setAccessible(true);
            configDir = constructor.newInstance("serverconfig");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

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

        // Need to run after CC has started.
        var phase = new ResourceLocation(Constants.MOD_ID, "after_cc");
        ServerLifecycleEvents.SERVER_STARTED.addPhaseOrdering(Event.DEFAULT_PHASE, phase);
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
