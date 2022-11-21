package cc.tweaked.prometheus.collectors;

import cc.tweaked.prometheus.MetricContext;
import dan200.computercraft.core.metrics.Metric;
import dan200.computercraft.core.metrics.Metrics;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.core.ServerContext;
import dan200.computercraft.shared.computer.metrics.ComputerMetricsObserver;
import dan200.computercraft.shared.computer.metrics.basic.Aggregate;
import dan200.computercraft.shared.computer.metrics.basic.AggregatedMetric;
import io.prometheus.client.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static cc.tweaked.prometheus.Constants.COMPUTERCRAFT_NAMESPACE;

/**
 * Reports the values for all {@link Metrics}s.
 */
public class ComputerFieldCollector implements ComputerMetricsObserver {
    private final Map<Metric.Counter, Counter> counters = new HashMap<>();
    private final Map<Metric.Event, Summary> summaries = new HashMap<>();
    private final Histogram computerTime;
    private final Histogram serverTime;

    private ComputerFieldCollector(CollectorRegistry registry) {
        for (var field : Metric.metrics().values()) {
            if (field == Metrics.COMPUTER_TASKS || field == Metrics.SERVER_TASKS) continue;

            if (field instanceof Metric.Counter counter) {
                counters.put(counter, buildField(field, Counter.build()).register(registry));
            } else if (field instanceof Metric.Event event) {
                summaries.put(event, buildField(field, Summary.build()).register(registry));
            }
        }

        computerTime = buildField(Metrics.COMPUTER_TASKS, Histogram.build()).name("task_time")
            .unit("s")
            .register(registry);

        serverTime = buildField(Metrics.SERVER_TASKS, Histogram.build())
            .unit("s")
            .buckets(0.0005, 0.001, 0.005, 0.01, 0.025, 0.05, 0.10, 0.25)
            .register(registry);
    }

    public static void register(MetricContext context) {
        ServerContext.get(context.server()).metrics().addObserver(new ComputerFieldCollector(context.registry()));
    }

    @Override
    public void observe(ServerComputer computer, Metric.Counter counter) {
        counters.get(counter).labels(Integer.toString(computer.getID())).inc();
    }

    @Override
    public void observe(ServerComputer computer, Metric.Event event, long value) {
        if (event == Metrics.SERVER_TASKS) {
            serverTime.labels(Integer.toString(computer.getID())).observe(value);
        } else if (event == Metrics.COMPUTER_TASKS) {
            computerTime.labels(Integer.toString(computer.getID())).observe(value);
        } else {
            summaries.get(event).labels(Integer.toString(computer.getID())).observe(value);
        }
    }

    @SuppressWarnings("unchecked")
    private static <B extends SimpleCollector.Builder<?, ?>> B buildField(Metric field, B builder) {
        return (B) builder
            .namespace(COMPUTERCRAFT_NAMESPACE)
            .name(field.name())
            .help(Objects.toString(new AggregatedMetric(field, Aggregate.NONE).displayName()))
            .labelNames("computer_id");
    }
}
