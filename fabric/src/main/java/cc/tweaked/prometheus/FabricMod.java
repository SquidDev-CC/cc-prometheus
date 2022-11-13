package cc.tweaked.prometheus;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class FabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ModLoadingContext.registerConfig("ccprometheus", ModConfig.Type.SERVER, Config.spec);

        // Need to run after CC has reset the tracker.
        var phase = new ResourceLocation(Constants.MOD_ID, "after_cc");
        ServerLifecycleEvents.SERVER_STARTED.addPhaseOrdering(Event.DEFAULT_PHASE, phase);
        ServerLifecycleEvents.SERVER_STARTED.register(phase, ServerMetrics::onServerStart);

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> ServerMetrics.onServerStop());
        ServerTickEvents.END_SERVER_TICK.register(server -> ServerMetrics.onServerTick());
    }
}
