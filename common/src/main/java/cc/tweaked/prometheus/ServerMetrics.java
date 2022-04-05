package cc.tweaked.prometheus;

import cc.tweaked.prometheus.collectors.*;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class ServerMetrics {
    private static final Logger LOG = LoggerFactory.getLogger(ServerMetrics.class);

    private static HTTPServer server;
    private static List<Runnable> toTick;

    private ServerMetrics() {
    }

    public static void onServerStart(MinecraftServer server, Config config) {
        var collectorRegistry = new CollectorRegistry(true);
        var ticking = ServerMetrics.toTick = new ArrayList<>();
        var registry = new MetricContext(server, collectorRegistry, ticking::add);

        ComputerCollector.register(registry);
        ComputerFieldCollector.register(registry);
        ThreadGroupCollector.register(registry);
        if (config.vanilla()) VanillaCollector.export(registry);
        if (config.jvm()) DefaultExports.register(collectorRegistry);

        try {
            ServerMetrics.server = new HTTPServer.Builder()
                .withHostname(config.host())
                .withPort(config.port())
                .withDaemonThreads(true)
                .withRegistry(collectorRegistry)
                .build();

            LOG.info("Serving Prometheus metrics on http://{}:{}", config.host(), config.port());
        } catch (IOException e) {
            LOG.error("Failed to start HTTP server", e);
            ServerMetrics.server = null;
        }
    }

    public static void onServerStop() {
        if (server != null) server.close();
        server = null;
        toTick = null;

        LOG.info("Server stopped, no longer hosting metrics");
    }

    public static void onServerTick() {
        for (var action : toTick) action.run();
    }
}
