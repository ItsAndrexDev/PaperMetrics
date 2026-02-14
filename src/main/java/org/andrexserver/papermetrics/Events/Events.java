package org.andrexserver.papermetrics.Events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import static org.andrexserver.papermetrics.Main.onlinePlayersGauge;
import static org.bukkit.Bukkit.getServer;

public class Events implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        int onlinePlayers = getServer().getOnlinePlayers().size();
        onlinePlayersGauge.set(onlinePlayers);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        int onlinePlayers = getServer().getOnlinePlayers().size() -1; // subtract one because the event is ran before the player is removed from the list
        onlinePlayersGauge.set(onlinePlayers);
    }
}
