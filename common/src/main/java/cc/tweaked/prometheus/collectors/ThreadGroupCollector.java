package cc.tweaked.prometheus.collectors;

import cc.tweaked.prometheus.MetricContext;
import dan200.computercraft.core.util.ThreadUtils;
import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;

import java.util.ArrayList;
import java.util.List;

import static cc.tweaked.prometheus.Constants.COMPUTERCRAFT_NAMESPACE;

/**
 * Counts the number of threads active in each of CC:T's thread groups.
 */
public class ThreadGroupCollector extends Collector implements Collector.Describable {
    private static final String FULL_NAME = COMPUTERCRAFT_NAMESPACE + "_thread_count";
    private static final String HELP = "Number of currently live threads";
    private static final List<String> LABEL_NAMES = List.of("group");

    private final ThreadGroup mainGroup = ThreadUtils.group();
    private ThreadGroup[] groups;

    private ThreadGroupCollector() {
    }

    public static void register(MetricContext context) {
        context.registry().register(new ThreadGroupCollector());
    }

    @Override
    public List<Collector.MetricFamilySamples> describe() {
        return List.of(new GaugeMetricFamily(FULL_NAME, HELP, LABEL_NAMES));
    }

    @Override
    public List<Collector.MetricFamilySamples> collect() {
        var size = mainGroup.activeGroupCount();
        var groups = this.groups;
        if (groups == null || size > groups.length) groups = this.groups = new ThreadGroup[size];

        List<MetricFamilySamples.Sample> samples = new ArrayList<>(size);

        int count = mainGroup.enumerate(groups, true);
        for (int i = 0; i < count; i++) {
            var group = groups[i];
            samples.add(new MetricFamilySamples.Sample(FULL_NAME, LABEL_NAMES, List.of(group.getName()), group.activeCount()));
        }

        return List.of(new MetricFamilySamples(FULL_NAME, "count", Type.GAUGE, HELP, samples));
    }
}
