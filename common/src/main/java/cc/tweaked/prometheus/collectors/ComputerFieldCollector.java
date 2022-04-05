package cc.tweaked.prometheus.collectors;

import cc.tweaked.prometheus.MetricContext;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.tracking.Tracker;
import dan200.computercraft.core.tracking.Tracking;
import dan200.computercraft.core.tracking.TrackingField;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Histogram;
import io.prometheus.client.SimpleCollector;
import io.prometheus.client.Summary;
import net.minecraft.locale.Language;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static cc.tweaked.prometheus.Constants.NAMESPACE;

/**
 * Reports the values for all {@link TrackingField}s.
 */
public class ComputerFieldCollector implements Tracker {
    private static final Set<TrackingField> SKIP = Set.of(
        TrackingField.TASKS, TrackingField.AVERAGE_TIME, TrackingField.MAX_TIME, TrackingField.TOTAL_TIME,
        TrackingField.SERVER_TIME, TrackingField.SERVER_COUNT
    );

    private final Map<TrackingField, Summary> summaries = new HashMap<>();
    private final Histogram taskTime;
    private final Histogram serverTime;

    private ComputerFieldCollector(CollectorRegistry registry) {
        for (var field : TrackingField.fields().values()) {
            if (SKIP.contains(field)) continue;

            summaries.put(field, buildField(field, Summary.build()).register(registry));
        }

        taskTime = buildField(TrackingField.TASKS, Histogram.build()).name("task_time")
            .unit("s")
            .register(registry);

        serverTime = buildField(TrackingField.SERVER_TIME, Histogram.build())
            .unit("s")
            .buckets(0.0005, 0.001, 0.005, 0.01, 0.025, 0.05, 0.10, 0.25)
            .register(registry);

        Tracking.add(this);
    }

    public static void register(MetricContext context) {
        new ComputerFieldCollector(context.registry());
    }

    @Override
    public void addTaskTiming(Computer computer, long time) {
        taskTime.labels(Integer.toString(computer.getID())).observe(time * 1e-9);
    }

    @Override
    public void addServerTiming(Computer computer, long time) {
        serverTime.labels(Integer.toString(computer.getID())).observe(time * 1e-9);
    }

    @Override
    public void addValue(Computer computer, TrackingField field, long count) {
        summaries.get(field).labels(Integer.toString(computer.getID())).observe(count);
    }

    @SuppressWarnings("unchecked")
    private static <B extends SimpleCollector.Builder<?, ?>> B buildField(TrackingField field, B builder) {
        return (B) builder
            .namespace(NAMESPACE)
            .name(field.id())
            .help(Language.getInstance().getOrDefault(field.translationKey()))
            .labelNames("computer_id");
    }
}
