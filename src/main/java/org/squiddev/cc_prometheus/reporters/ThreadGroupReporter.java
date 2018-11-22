package org.squiddev.cc_prometheus.reporters;

import dan200.computercraft.shared.util.ThreadUtils;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;

/**
 * Reports
 */
public class ThreadGroupReporter implements Reporter {
    private final ThreadGroup mainGroup = ThreadUtils.group();
    private ThreadGroup[] groups;

    private final Gauge threadCount = Gauge.build()
        .namespace(NAMESPACE)
        .name("thread_count")
        .help("Number of currently live threads")
        .labelNames("group")
        .create();

    @Override
    public void register(CollectorRegistry registry) {
        registry.register(threadCount);
    }

    @Override
    public void fetch() {
        int size = mainGroup.activeGroupCount();
        ThreadGroup[] groups = this.groups;
        if (groups == null || size > groups.length) groups = this.groups = new ThreadGroup[size];

        int count = mainGroup.enumerate(groups, true);
        for (int i = 0; i < count; i++) {
            ThreadGroup group = groups[i];
            threadCount.labels(group.getName()).set(group.activeCount());
        }
    }
}
