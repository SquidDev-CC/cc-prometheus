package org.squiddev.cc_prometheus;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.squiddev.cc_prometheus.reporters.ComputerReporter;
import org.squiddev.cc_prometheus.reporters.TrackingReporter;

import java.io.File;
import java.util.Map;

@Mod(
    modid = CCPrometheus.MOD_ID,
    name = "CC Prometheus",
    version = "${version}",
    dependencies = "required-after:computercraft",
    acceptableRemoteVersions = "*",
    acceptedMinecraftVersions = "*"
)
public class CCPrometheus {
    public static final String MOD_ID = "cc-prometheus";
    public static Logger log;

    private Server server;
    private PrometheusController controller;

    private Config config;
    private int port;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        log = event.getModLog();

        config = new Config(event.getSuggestedConfigurationFile());
        config.sync();

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                log.error("Cannot stop prometheus server", e);
            }
        }

        controller = new PrometheusController();
        controller.addReporter(new ComputerReporter());

        if (classExists("dan200.computercraft.core.tracking.Tracker")) {
            controller.addReporter(new TrackingReporter());
        }

        server = new Server(port);
        server.setHandler(controller);

        try {
            server.start();
        } catch (Exception e) {
            log.error("Cannot start prometheus server", e);

            server = null;
            controller = null;
        }
    }

    @Mod.EventHandler
    public void onServerStopped(FMLServerStoppedEvent event) {
        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                log.error("Cannot stop prometheus server", e);
            }

            server = null;
            controller = null;
        }
    }

    @NetworkCheckHandler
    public boolean onNetworkConnect(Map<String, String> mods, Side side) {
        // This can work on the server or on the client
        return true;
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent event) {
        if (event.getModID().equals(MOD_ID)) config.sync();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && controller != null) controller.update();
    }

    private class Config {
        private Configuration configuration;

        private Property port;

        Config(File file) {
            configuration = new Configuration(file);
            configuration.load();

            port = configuration.get(Configuration.CATEGORY_GENERAL, "port", 9226);
            port.setMinValue(0);
            port.setRequiresWorldRestart(true);
            port.setComment("The port on which to host the prometheus server");
        }

        void sync() {
            configuration.save();

            CCPrometheus.this.port = port.getInt();
        }
    }

    private static boolean classExists(String name) {
        try {
            Class.forName("dan200.computercraft.core.tracking.Tracker", false, CCPrometheus.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
