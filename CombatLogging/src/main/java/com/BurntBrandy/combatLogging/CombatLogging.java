package com.BurntBrandy.combatLogging;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class CombatLogging extends JavaPlugin implements Listener {
    private final List<String> combat_usernames = Collections.synchronizedList(new ArrayList<>());
    private final List<String> logged_usernames = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, Long> DamageTimeTracker = new ConcurrentHashMap<>();
    private final long combatTimeout = 50000L; // 50 seconds

    @Override
    public void onEnable() {
        System.out.println("[CL] Combat Logging: Initializing...");
        getServer().getPluginManager().registerEvents(this, this);

        try (BufferedReader reader = new BufferedReader(new FileReader("Logged Players.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logged_usernames.add(line);
            }
        } catch (IOException e) {
            System.err.println("[CL] Failed to load logged players: " + e.getMessage());
        }

        System.out.println("[CL] Combat Logging: Plugin enabled");
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player combat && event.getEntity() instanceof Player victim) {
            combat_usernames.add(combat.getName());
            combat_usernames.add(victim.getName());
            DamageTimeTracker.put(combat.getName(), System.currentTimeMillis());
            DamageTimeTracker.put(victim.getName(), System.currentTimeMillis());

            victim.sendMessage("[CL] You are in combat with " + combat.getName());
            combat.sendMessage("[CL] You are in combat with " + victim.getName());

            new BukkitRunnable() {
                @Override
                public void run() {
                    checkCombatStatus(combat);
                    checkCombatStatus(victim);
                }
            }.runTaskLater(this, combatTimeout / 50);
        }
    }

    private void checkCombatStatus(Player player) {
        if (player == null) return;

        long lastDamage = DamageTimeTracker.getOrDefault(player.getName(), 0L);
        if (System.currentTimeMillis() - lastDamage > combatTimeout) {
            combat_usernames.remove(player.getName());
            DamageTimeTracker.remove(player.getName());

            player.sendMessage("[CL] You are no longer in combat.");
            System.out.println("[CL] " + player.getName() + " is no longer in combat.");
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        String playerName = event.getPlayer().getName();
        if (combat_usernames.contains(playerName)) {
            System.out.println("[CL] " + playerName + " left the game whilst in combat");
            logged_usernames.add(playerName);
            combat_usernames.remove(playerName);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String playerName = event.getPlayer().getName();
        if (logged_usernames.contains(playerName)) {
            event.getPlayer().setHealth(0);
            Bukkit.broadcastMessage("[CL] " + playerName + " has been killed for combat logging");
            logged_usernames.remove(playerName);
        }
    }

    @Override
    public void onDisable() {
        System.out.println("[CL] Combat Logging: Shutting down...");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Logged Players.txt"))) {
            for (String playerName : logged_usernames) {
                writer.write(playerName);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("[CL] Failed to save logged players: " + e.getMessage());
        }

        System.out.println("[CL] Combat Logging: Plugin disabled");
    }
}
