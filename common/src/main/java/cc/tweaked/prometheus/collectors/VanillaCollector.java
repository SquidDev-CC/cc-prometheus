package cc.tweaked.prometheus.collectors;

import cc.tweaked.prometheus.MetricContext;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

/**
 * Collects some <em>basic</em> metrics about the vanilla server.
 */
public class VanillaCollector {
    private static final String NAMESPACE = "minecraft";

    public static void export(MetricContext context) {
        var server = context.server();

        var averageTickTime = Gauge.build()
            .namespace(NAMESPACE).name("average_tick_time").unit("s")
            .help("The average tick time as defined by the MC server")
            .register(context.registry());

        var tickTime = Histogram.build()
            .namespace(NAMESPACE).name("tick_time").unit("s")
            .buckets(0.005, 0.01, 0.025, 0.05, 0.10, 0.25, 0.5, 1.0)
            .help("The average tick time as defined by the MC server")
            .register(context.registry());

        var playerCount = Gauge.build()
            .namespace(NAMESPACE).name("players").unit("count")
            .help("The number of players in each dimension.")
            .labelNames("dimension")
            .register(context.registry());

        var totalPlayerCount = Gauge.build()
            .namespace(NAMESPACE).name("total_players").unit("count")
            .help("The number of players on the server.")
            .register(context.registry());

        var chunksLoaded = Gauge.build()
            .namespace(NAMESPACE).name("chunks_loaded").unit("count")
            .help("The number of chunks loaded in each dimension.")
            .labelNames("dimension")
            .register(context.registry());

        context.onTick(() -> {
            for (var level : server.getAllLevels()) {
                var name = level.dimension().location().toString();
                playerCount.labels(name).set(level.players().size());
                chunksLoaded.labels(name).set(level.getChunkSource().getLoadedChunksCount());
            }

            totalPlayerCount.set(server.getPlayerCount());
        });

        ((MinecraftServerTimings) server).prometheus$setTimingObserver((time, averageTime) -> {
            averageTickTime.set(averageTime * 1e-3); // ms to s
            tickTime.observe(time * 1e-9); // ns to s.
        });
    }

    public interface MinecraftServerTimings {
        /**
         * Set the object which monitors this server's timings.
         *
         * @param observer The timings observer.
         */
        void prometheus$setTimingObserver(TimingObserver observer);
    }

    public interface TimingObserver {
        /**
         * Called when a server finishes its tick.
         *
         * @param tickTime    The time taken for the last tick measured in nanoseconds.
         * @param averageTime A smoothed version of tick times, measured in milliseconds.
         */
        void onServerTick(long tickTime, float averageTime);
    }
}
