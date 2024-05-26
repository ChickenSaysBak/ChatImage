// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.plugin.spigot;

import me.chickensaysbak.chatimage.core.adapters.CommandAdapter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class CommandSpigot implements TabExecutor {

    private CommandAdapter command;

    public CommandSpigot(CommandAdapter command) {
        this.command = command;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        UUID uuid = sender instanceof Player player ? player.getUniqueId() : null;
        command.onCommand(uuid, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        UUID uuid = sender instanceof Player player ? player.getUniqueId() : null;
        return command.onTabComplete(uuid, args);
    }

}
