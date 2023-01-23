// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.plugin.bungee;

import me.chickensaysbak.chatimage.core.wrappers.CommandWrapper;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.List;
import java.util.UUID;

public class CommandBungee extends Command implements TabExecutor {

    private CommandWrapper command;

    public CommandBungee(CommandWrapper command) {
        super(command.getName(), command.getPermission());
        this.command = command;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        UUID uuid = sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId() : null;
        command.onCommand(uuid, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        UUID uuid = sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId() : null;
        return command.onTabComplete(uuid, args);
    }

}