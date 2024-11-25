# Combat Logging Plugin

## Description
The **Combat Logging Plugin** for Minecraft Bukkit helps prevent and manage **combat logging** (leaving the game during combat). It automatically handles punishments for players who disconnect mid-combat and ensures they are penalized upon rejoining.

This plugin includes:
- Detection of combat-related actions.
- Enforcement of penalties for combat loggers, such as executing a `kill` command.
- Timeout management, where players are automatically removed from combat after a set period of inactivity.
- Notifications to players when they combat log.

## Features
- **Combat Detection**: Tracks when players engage in combat with each other (PvP).
- **Combat Log Prevention**: If a player disconnects during combat, they are added to the logged list and punished upon rejoining.
- **Timed Combat Check**: Players are removed from the combat list after a specified timeout if no damage is dealt.
- **Customizable Timeout**: Set the timeout period (in milliseconds) for considering a player out of combat after being damaged.
- **Message Notifications**: Players receive messages when they combat log, enter combat, or are punished.

## Setup & Installation

### Requirements
- **Minecraft Server** running **Bukkit** or **Spigot** (1.21.3 or compatible version).
- **Java 17** or higher.
  
### Installation Steps
1. Download the compiled `.jar` file.
2. Place the `.jar` file into your server's `plugins` directory.
3. Restart the server to enable the plugin.
4. The plugin will automatically register events and start functioning.

### Configuration
You can modify the combat timeout period by adjusting the `combatTimeout` variable in the code (in milliseconds):

```java
private final long combatTimeout = 10000L; // Time in milliseconds before a player is considered out of combat (10 seconds)
```

### Messages
You can customize the messages sent to players for various actions such as combat log detection. The plugin uses the `sendMessage()` method to notify players:

```java
player.sendMessage("You have combat-logged! You are being punished for leaving in combat.");
```

- **Bukkit API**: Required for event handling and interaction with the server.
  
---


### How it Works:
- Players who leave during combat (disconnect) are added to the `logged_usernames` list and punished upon rejoining.
- After a player is no longer in combat (after a set timeout), they are removed from the `combat_usernames` list automatically.
  
## Contributions
Feel free to fork this repository and create a pull request for any features or improvements!

---

### Acknowledgments
- Special thanks to the [Bukkit API](https://hub.spigotmc.org/javadocs/bukkit/) documentation for event handling and plugin development.
---

