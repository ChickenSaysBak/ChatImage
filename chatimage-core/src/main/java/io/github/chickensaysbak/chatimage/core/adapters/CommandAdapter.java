// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package io.github.chickensaysbak.chatimage.core.adapters;

import java.util.List;
import java.util.UUID;

public abstract class CommandAdapter {

    private PluginAdapter plugin;

    public CommandAdapter(PluginAdapter plugin) {
        this.plugin = plugin;
    }

    public PluginAdapter getPlugin() {
        return plugin;
    }

    public abstract String getName();
    public abstract String getPermission();
    public abstract String[] getAliases();
    public abstract void onCommand(UUID sender, String[] args);
    public abstract List<String> onTabComplete(UUID sender, String[] args);

}
