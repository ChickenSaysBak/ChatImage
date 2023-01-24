// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.adapters;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface YamlAdapter {

    void save(File file) throws IOException;
    boolean getBoolean(String path, boolean def);
    int getInt(String path, int def);
    long getLong(String path, long def);
    double getDouble(String path, double def);
    String getString(String path, String def);
    List<String> getStringList(String path);

}