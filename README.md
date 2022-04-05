# CC Prometheus Exporter
Exposes [Prometheus] metrics for [CC: Tweaked] and [CC: Restitched]. This provides the following metrics:

 - Total number of computers.
 - Total number of on computers.
 - Per-computer breakdown of all fields in `/computercraft profile`, including:
   - Time taken on the computer thread.
   - Time taken on the server thread.
   - HTTP usage (bandwidth, number of requests).
 - Number of threads in CC's underlying thread groups.

We also provide several non-CC related exporters, which are disabled by default. These are not designed to be
comprehensive (better monitoring solutions exist!) but are useful when you only need some basic monitoring and are
already running this.

 - All standard JVM metrics (memory, GC, thread counts).
 - Basic statistics about Minecraft:
   - Player count (total and per-dimension).
   - Chunks loaded per-dimension.
   - TPS (rolling average and as a histogram).

## Usage
Download the mod from the releases page and drop it into your `mods/` folder. Be careful to pick the correct jar for
Forge or Fabric!

Start your server, metrics are available at <http://127.0.0.1:9226/metrics>. This can be configured with the config
file, located at `<world_dir>/serverconfig/ccprometheus.toml`.

## Credits
The project layout is based on Jared's [MultiLoader Template](https://github.com/jaredlll08/MultiLoader-Template/).

[Prometheus]: https://prometheus.io/
[CC: Tweaked]: https://github.com/cc-tweaked/CC-Tweaked
[CC: Restitched]: https://github.com/cc-tweaked/cc-restitched
