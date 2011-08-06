package com.minecarts.familyjewels.listener;

import com.minecarts.familyjewels.FamilyJewels;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class PlayerListener extends org.bukkit.event.player.PlayerListener {
    private FamilyJewels plugin;
    public PlayerListener(FamilyJewels plugin){
        this.plugin = plugin;
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent e){
        plugin.hookNSH(e.getPlayer());
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent e){
        plugin.unhookNSH(e.getPlayer());
    }

}
