package cc.tweaked.prometheus;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(Constants.MOD_ID)
public class ForgeMod {
    public ForgeMod() {
        var configBuilder = new ModConfigSpec.Builder();

        var host = configBuilder
            .comment(Config.HOST_HELP)
            .define("host", Config.HOST_DEFAULT);

        var port = configBuilder
            .comment(Config.PORT_HELP)
            .defineInRange("port", Config.PORT_DEFAULT, Config.PORT_MIN, Config.PORT_MAX);

        var vanilla = configBuilder
            .comment(Config.VANILLA_HELP)
            .define("vanilla", Config.VANILLA_DEFAULT);

        var jvm = configBuilder
            .comment(Config.JVM_HELP)
            .define("jvm", Config.JVM_DEFAULT);

        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.SERVER, configBuilder.build());

        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, (ServerStartedEvent event) ->
            ServerMetrics.onServerStart(event.getServer(), new Config(host.get(), port.get(), vanilla.get(), jvm.get())));
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> ServerMetrics.onServerStop());
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> ServerMetrics.onServerTick());
    }
}
