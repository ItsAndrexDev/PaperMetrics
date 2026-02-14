# PaperMetrics 📊

**PaperMetrics** is a lightweight Prometheus metrics exporter for Paper Minecraft servers.

It exposes JVM, server, and gameplay metrics through an HTTP endpoint.

---

## Requirements ⚙️

- Java 17+ ☕  
- Paper server 🧱  

---

## Installation 🚀

1. Download the release from the **Releases** tab.  
2. Copy the `.jar` file into your server's `plugins/` folder.  
3. Restart the server. 🔄  

---

## Configuration 🛠️

Default `config.yml`:

```yml
metrics_port: 9400
```

---

## Metrics Endpoint 🌐

Metrics are available at:

```
http://<server-ip>:9400/metrics
```

---

## Exposed Metrics 📈

- `papermetrics_is_server_running`
- `papermetrics_server_start_time`
- `papermetrics_server_tps`
- `papermetrics_memory_usage_bytes`
- `papermetrics_max_memory_bytes`
- `papermetrics_thread_count`
- `papermetrics_online_players`
- `papermetrics_average_player_ping`
- `papermetrics_entity_count`
- `papermetrics_chunk_count`

---

## Example Prometheus Configuration 📡

```yml
scrape_configs:
  - job_name: "papermetrics"
    static_configs:
      - targets: ["localhost:9400"]
```

---

## Design Goals 🎯

- Minimal runtime overhead  
- No heavy external dependencies  
- Simple configuration  
- Clean lifecycle handling  
- Production-friendly metrics output  
