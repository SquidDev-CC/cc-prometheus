package cc.tweaked.prometheus.mixin;

import cc.tweaked.prometheus.collectors.VanillaCollector;
import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@link MinecraftServer#tickTimes}} (and the derived calculations) do not include main-thread tick tasks (those queued
 * by {@link MinecraftServer#tell(Runnable)}) or from Fabric/Forge's tick events. We use our own mixins to more
 * accurately capture this information.
 */
@Mixin(MinecraftServer.class)
class MinecraftServerMixin implements VanillaCollector.MinecraftServerTimings {
    @Unique
    private VanillaCollector.TimingObserver observer;

    @Unique
    private float averageTickTime;

    @Unique
    private long tickStart;

    @Inject(at = @At("HEAD"), method = "startMetricsRecordingTick")
    private void beforeTick(CallbackInfo ci) {
        tickStart = Util.getNanos();
    }

    @Inject(
        method = "waitUntilNextTick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;managedBlock(Ljava/util/function/BooleanSupplier;)V")
    )
    private void afterTick(CallbackInfo ci) {
        // We want to inject here rather than endMetricsRecordingTick, as that also counts the sleep until the next tick.

        long time = Util.getNanos() - tickStart;
        averageTickTime = averageTickTime * 0.8F + (float) time / 1000000.0F * 0.19999999F;
        if (observer != null) observer.onServerTick(time, averageTickTime);
    }

    @Override
    public void prometheus$setTimingObserver(VanillaCollector.TimingObserver observer) {
        this.observer = observer;
    }
}
