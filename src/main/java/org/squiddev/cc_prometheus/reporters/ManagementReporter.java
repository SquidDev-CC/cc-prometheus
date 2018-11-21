package org.squiddev.cc_prometheus.reporters;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;

import java.lang.management.*;
import java.util.List;

public class ManagementReporter implements Reporter {
    private final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean thread = ManagementFactory.getThreadMXBean();
    private final List<GarbageCollectorMXBean> gc = ManagementFactory.getGarbageCollectorMXBeans();

    private final MemoryGauge heapMemory = new MemoryGauge("heap", "Heap");
    private final MemoryGauge nonHeapMemory = new MemoryGauge("non_heap", "Non-heap");

    private final Gauge threadGaugeCount = Gauge.build()
        .namespace(NAMESPACE)
        .name("management_thread_count")
        .help("Number of currently live threads")
        .create();

    private final Gauge threadGaugeDaemon = Gauge.build()
        .namespace(NAMESPACE)
        .name("management_thread_daemon")
        .help("Number of currently live daemon threads")
        .create();

    private final Gauge threadGaugePeak = Gauge.build()
        .namespace(NAMESPACE)
        .name("management_thread_peak")
        .help("Maximum number of threads live at one time")
        .create();

    private final Gauge threadGaugeTotal = Gauge.build()
        .namespace(NAMESPACE)
        .name("management_thread_total")
        .help("Total number of threads ever started")
        .create();

    private final Gauge gcCount = Gauge.build()
        .namespace(NAMESPACE)
        .name("management_gc_count")
        .help("Total number of GC iterations which have occurred")
        .labelNames("Source")
        .create();

    private final Gauge gcTime = Gauge.build()
        .namespace(NAMESPACE)
        .name("management_gc_time")
        .help("Time since last GC in ms")
        .labelNames("Source")
        .create();

    @Override
    public void register(CollectorRegistry registry) {
        heapMemory.register(registry);
        nonHeapMemory.register(registry);

        registry.register(threadGaugeCount);
        registry.register(threadGaugeDaemon);
        registry.register(threadGaugePeak);
        registry.register(threadGaugeTotal);

        registry.register(gcCount);
        registry.register(gcTime);
    }

    @Override
    public void fetch() {
        heapMemory.fetch(memory.getHeapMemoryUsage());
        nonHeapMemory.fetch(memory.getNonHeapMemoryUsage());

        threadGaugeCount.set(thread.getThreadCount());
        threadGaugeDaemon.set(thread.getDaemonThreadCount());
        threadGaugePeak.set(thread.getPeakThreadCount());
        threadGaugeTotal.set(thread.getTotalStartedThreadCount());

        for (GarbageCollectorMXBean bean : gc) {
            String name = bean.getName();
            maybeSet(gcCount.labels(name), bean.getCollectionCount());
            maybeSet(gcTime.labels(name), bean.getCollectionTime());
        }
    }

    private static class MemoryGauge {
        private final Gauge init;
        private final Gauge used;
        private final Gauge committed;
        private final Gauge max;

        MemoryGauge(String name, String desc) {
            init = Gauge.build().namespace(NAMESPACE)
                .name("management_memory_" + name + "_init")
                .help("Initially requested memory (" + desc + ")")
                .create();

            used = Gauge.build().namespace(NAMESPACE)
                .name("management_memory_" + name + "_used")
                .help("Currently used memory (" + desc + ")")
                .create();

            committed = Gauge.build().namespace(NAMESPACE)
                .name("management_memory_" + name + "_committed")
                .help("Committed memory (" + desc + ")")
                .create();

            max = Gauge.build().namespace(NAMESPACE)
                .name("management_memory_" + name + "_max")
                .help("Maximum memory (" + desc + ")")
                .create();
        }


        void register(CollectorRegistry registry) {
            registry.register(init);
            registry.register(used);
            registry.register(committed);
            registry.register(max);
        }

        void fetch(MemoryUsage usage) {
            maybeSet(init, usage.getInit());
            maybeSet(used, usage.getUsed());
            maybeSet(committed, usage.getCommitted());
            maybeSet(max, usage.getMax());
        }
    }

    private static void maybeSet(Gauge gauge, long value) {
        if (value != -1) gauge.set(value);
    }

    private static void maybeSet(Gauge.Child gauge, long value) {
        if (value != -1) gauge.set(value);
    }
}
