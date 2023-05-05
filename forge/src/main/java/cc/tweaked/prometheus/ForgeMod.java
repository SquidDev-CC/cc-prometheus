package cc.tweaked.prometheus;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(Constants.MOD_ID)
public class ForgeMod {
    public ForgeMod() {
        // Define our config
        var configBuilder = new ForgeConfigSpec.Builder();

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

        var config = configBuilder.build();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, config);

        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, (ServerStartedEvent event) ->
            ServerMetrics.onServerStart(event.getServer(), new Config(host.get(), port.get(), vanilla.get(), jvm.get())));
        MinecraftForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> ServerMetrics.onServerStop());
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) ServerMetrics.onServerTick();
        });
    }
}
