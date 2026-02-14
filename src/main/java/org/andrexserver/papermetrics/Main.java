package org.andrexserver.papermetrics;

import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Instant;

public final class Main extends JavaPlugin {

    private Gauge isRunningGauge;
    private Gauge serverStartTimeGauge;
    private Gauge tpsGauge;
    private Gauge memoryUsageGauge;
    private Gauge maxMemoryGauge;
    private Gauge threadCountGauge;
    private Gauge entityCountGauge;
    private Gauge onlinePlayersGauge;
    private Gauge playerPingGauge;
    private Gauge chunkCountGauge;

    private HTTPServer httpServer;

    @Override
    public void onEnable() {
        getLogger().info("PaperMetrics enabled.");

        registerMetrics();
        startHttpServer();
        startMetricsTask();
    }

    @Override
    public void onDisable() {
        if (isRunningGauge != null) {
            isRunningGauge.set(0);
        }

        if (httpServer != null) {
            httpServer.stop();
        }
    }

    private void registerMetrics() {
        isRunningGauge = Gauge.builder()
                .name("papermetrics_is_server_running")
                .help("1 if the server is running, 0 otherwise")
                .register();

        serverStartTimeGauge = Gauge.builder()
                .name("papermetrics_server_start_time")
                .help("Server start time in epoch seconds")
                .register();

        tpsGauge = Gauge.builder()
                .name("papermetrics_server_tps")
                .help("Current 1-minute TPS")
                .register();

        memoryUsageGauge = Gauge.builder()
                .name("papermetrics_memory_usage_bytes")
                .help("Current heap memory usage in bytes")
                .register();

        maxMemoryGauge = Gauge.builder()
                .name("papermetrics_max_memory_bytes")
                .help("Maximum available heap memory in bytes")
                .register();

        threadCountGauge = Gauge.builder()
                .name("papermetrics_thread_count")
                .help("Current JVM thread count")
                .register();

        onlinePlayersGauge = Gauge.builder()
                .name("papermetrics_online_players")
                .help("Number of online players")
                .register();

        playerPingGauge = Gauge.builder()
                .name("papermetrics_average_player_ping")
                .help("Average player ping in ms")
                .register();

        entityCountGauge = Gauge.builder()
                .name("papermetrics_entity_count")
                .help("Total loaded entities")
                .register();

        chunkCountGauge = Gauge.builder()
                .name("papermetrics_chunk_count")
                .help("Total loaded chunks")
                .register();

        isRunningGauge.set(1);
        serverStartTimeGauge.set(Instant.now().getEpochSecond());
    }

    private void startHttpServer() {
        try {
            httpServer = HTTPServer.builder()
                    .port(getConfig().getInt("metrics_port", 9400))
                    .buildAndStart();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start Prometheus HTTP server", e);
        }
    }

    private void startMetricsTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateMetrics();
            }
        }.runTaskTimer(this, 0L, 1200L); // 60 seconds (20 ticks * 60)
    }

    private void updateMetrics() {
        updatePerformanceMetrics();
        updatePlayerMetrics();
        updateWorldMetrics();
    }

    private void updatePerformanceMetrics() {
        var server = getServer();

        double tps = server.getTPS()[0];
        tpsGauge.set(Math.round(tps * 10.0) / 10.0);

        Runtime runtime = Runtime.getRuntime();
        maxMemoryGauge.set(runtime.maxMemory());

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        memoryUsageGauge.set(memoryBean.getHeapMemoryUsage().getUsed());

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        threadCountGauge.set(threadBean.getThreadCount());
    }

    private void updatePlayerMetrics() {
        var players = getServer().getOnlinePlayers();
        int onlineCount = players.size();
        onlinePlayersGauge.set(onlineCount);

        if (onlineCount == 0) {
            playerPingGauge.set(0);
            return;
        }

        int totalPing = 0;
        for (Player player : players) {
            totalPing += player.getPing();
        }

        playerPingGauge.set(totalPing / (double) onlineCount);
    }

    private void updateWorldMetrics() {
        int totalChunks = 0;
        int totalEntities = 0;

        for (World world : Bukkit.getWorlds()) {
            totalChunks += world.getLoadedChunks().length;
            totalEntities += world.getEntities().size();
        }

        chunkCountGauge.set(totalChunks);
        entityCountGauge.set(totalEntities);
    }
}
