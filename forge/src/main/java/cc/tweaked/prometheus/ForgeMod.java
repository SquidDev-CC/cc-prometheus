package cc.tweaked.prometheus;

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
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.spec);

        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, (ServerStartedEvent event) -> ServerMetrics.onServerStart(event.getServer()));
        MinecraftForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> ServerMetrics.onServerStop());
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) ServerMetrics.onServerTick();
        });
    }
}
