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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class CombatLogging extends JavaPlugin implements Listener {
    private final List<String> combat_usernames = new ArrayList<>();
    private final List<String> logged_usernames = new ArrayList<>();
    private final HashMap<String, Long> lastDamageTime = new HashMap<>(); // Track the last damage time for each player
    private final long combatTimeout = 50000L; // Time in milliseconds before a player is considered out of combat (10 seconds)

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println("[CL] " + "Combat Logging: Initializing...");
        getServer().getPluginManager().registerEvents(this, this);  // Register events
        System.out.println("[CL] " + "Combat Logging: Plugin enabled");
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player combat && event.getEntity() instanceof Player victim) {
            System.out.printf("[CL] " + "Attacker: " + combat.getName());
            System.out.println("[CL] " + "Victim: " + victim.getName());
            combat_usernames.add(combat.getName());
            logged_usernames.add(victim.getName());
            lastDamageTime.put(combat.getName(), System.currentTimeMillis());  // Record the time of attack for the attacker
            lastDamageTime.put(victim.getName(), System.currentTimeMillis());  // Record the time of attack for the victim

            victim.sendMessage("[CL] You are in combat with " + combat);
            combat.sendMessage("[CL] You are in combat with " + victim);

            // Start a delayed task to check if the player should be removed from combat
            new BukkitRunnable() {
                @Override
                public void run() {
                    checkCombatStatus(combat);  // Check the combat status for the attacker
                    checkCombatStatus(victim);  // Check the combat status for the victim
                }
            }.runTaskLater(this, combatTimeout / 50);  // Run after combatTimeout (converted to ticks)
        }
    }

    private void checkCombatStatus(Player player) {
        long lastDamage = lastDamageTime.getOrDefault(player.getName(), 0L);
        if (System.currentTimeMillis() - lastDamage > combatTimeout) {  // Player has not taken damage for the set amount of time, so remove them from combat list
            combat_usernames.remove(player.getName());
            System.out.println("[CL] " + player.getName() + " is no longer in combat.");
            Player remove_combat_message = player.getPlayer();
            assert remove_combat_message != null;
            remove_combat_message.sendMessage("[CL] You are no longer in combat.");
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        String playerName = event.getPlayer().getDisplayName();
        if (combat_usernames.contains(playerName)) {
            System.out.println("[CL] " + playerName + " left the game whilst in combat");
            logged_usernames.add(playerName);  // Add to logged usernames
            combat_usernames.remove(playerName);  // Remove from combat list
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String name = event.getPlayer().getDisplayName();
        if (logged_usernames.contains(name)) {
            String command = "kill " + name + " [CL] You have been killed for combat logging!";
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "say [CL] " + name + "has been killed for combat logging");

        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        System.out.println("[CL] " + "Combat Logging: Shutting down...");
        System.out.println("[CL] " + "Combat Logging: Plugin disabled");
    }
}
