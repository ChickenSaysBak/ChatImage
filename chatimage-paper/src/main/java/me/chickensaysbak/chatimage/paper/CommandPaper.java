// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.paper;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.chickensaysbak.chatimage.core.adapters.CommandAdapter;
import org.bukkit.entity.Entity;

import java.util.Collection;
import java.util.UUID;

public class CommandPaper implements BasicCommand {

    private CommandAdapter command;

    public CommandPaper(CommandAdapter command) {
        this.command = command;
    }

    @Override
    public String permission() {
        return command.getPermission();
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        Entity executor = source.getExecutor();
        UUID uuid = executor != null ? executor.getUniqueId() : null;
        command.onCommand(uuid, args);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        Entity executor = source.getExecutor();
        UUID uuid = executor != null ? executor.getUniqueId() : null;
        return command.onTabComplete(uuid, args);
    }

}
