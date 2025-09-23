// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.velocity;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import me.chickensaysbak.chatimage.core.adapters.CommandAdapter;

import java.util.List;
import java.util.UUID;

public class CommandVelocity implements SimpleCommand {

    private CommandAdapter command;

    public CommandVelocity(CommandAdapter command) {
        this.command = command;
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        String perm = command.getPermission();
        return perm == null || invocation.source().hasPermission(perm);
    }

    @Override
    public void execute(Invocation invocation) {
        UUID uuid = invocation.source() instanceof Player player ? player.getUniqueId() : null;
        command.onCommand(uuid, invocation.arguments());
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        UUID uuid = invocation.source() instanceof Player player ? player.getUniqueId() : null;
        return command.onTabComplete(uuid, invocation.arguments());
    }

}
