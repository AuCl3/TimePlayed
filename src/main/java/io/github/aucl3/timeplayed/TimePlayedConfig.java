/*
 * This file is part of TimePlayed, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017 AuCl3 https://www.github.com/AuCl3
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.github.aucl3.timeplayed;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;


public final class TimePlayedConfig {

    /**
     * Holder for the {@link TimePlayedConfig} singleton instance.
     */
    private static class TimePlayedConfigHolder {

        /**
         * {@link TimePlayedConfig} singleton instance.
         */
        private static final TimePlayedConfig INSTANCE = new TimePlayedConfig();
    }

    /**
     * The {@link ConfigurationLoader}.
     */
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;

    /**
     * The {@link CommentedConfigurationNode}.
     */
    private CommentedConfigurationNode config;

    /**
     * Sql username.
     */
    private String user = "";

    /**
     * Sql user password.
     */
    private String pass = "";

    /**
     * Sql database address.
     */
    private String addr = "";

    /**
     * Sql database name.
     */
    private String data = "";

    /**
     * Sql connection url.
     */
    private String connection = "";

    /**
     * Enable auto update.
     */
    private Boolean updates = Boolean.FALSE;

    /**
     * Time interval for updating player times.
     */
    private int timeinterval = 1;

    /**
     * @return the {@link TimePlayedConfig} singleton instance.
     */
    public static TimePlayedConfig getInstance() {
        return TimePlayedConfigHolder.INSTANCE;
    }

    /**
     * @param defaultConfig path to the default config.
     * @param configLoader the configuration loader.
     */
    public void setup(Path defaultConfig, ConfigurationLoader<CommentedConfigurationNode> configLoader) {
        configLoader = HoconConfigurationLoader.builder().setPath(defaultConfig).build();
        this.configLoader = configLoader;
        try {
            config = this.configLoader.load();
            if (!defaultConfig.toFile().exists()) {
                //create default configuration file
                createConfig();
            }
            this.configLoader.save(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the {@link CommentedConfigurationNode}.
     */
    public CommentedConfigurationNode getConfig() {
        return config;
    }

    /**
     * @return the string version of the SQL connection url.
     */
    public String getmySQLConnection() {
        user = getConfig().getNode("MySQL", "username").getString();
        pass = getConfig().getNode("MySQL", "password").getString();
        addr = getConfig().getNode("MySQL", "address").getString();
        data = getConfig().getNode("MySQL", "database").getString();
        connection = "";
        // Connection format:
        // "jdbc:mysql://"+"root"+":"+"password"+"@"+"localhost"+"/"+"tutorial01"
        connection = "jdbc:mysql://" + user + ":" + pass + "@" + addr + "/" + data;
        return connection;
    }

    /**
     * @return true if the plugin should auto update.
     */
    public Boolean getUpdateEnabled() {
        // By default, return false
        updates = Boolean.FALSE;
        String check = getConfig().getNode("PeriodicUpdates", "updates").getString();
        if (check.equalsIgnoreCase("enable")) {
            updates = Boolean.TRUE;
        }
        return updates;
    }

    /**
     * @return the time interval to update players time.
     */
    public int getTimeInterval() {
        timeinterval = getConfig().getNode("PeriodicUpdates", "timeinterval").getInt();
        return timeinterval;
    }

    /**
     * Save the configuration.
     */
    public void saveConfig() {
        try {
            configLoader.save(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load the configuration.
     */
    public void loadConfig() {
        try {
            configLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize the configuration.
     */
    public void createConfig() {
        String top = "--------------------------------------\n"
                   + "     TimePlayed Configuration			\n"
                   + "--------------------------------------\n"
                   + "										\n"
                   + "MySQL Database Parameters				\n"
                   + "										\n"
                   + "address:								\n"
                   + "      local   - \"localhost\"			\n"
                   + "       or                				\n"
                   + "     IP Addr  - \"host:port\"			\n"
                   + "     default port is 3306				\n"
                   + "										\n"
                   + "database:								\n"
                   + "		Name of MySQL database			\n"
                   + "										\n"
                   + "username & password:					\n"
                   + "		Specify database credentials	\n"
                   + "--------------------------------------\n";
        // String database = "Database Variables";
        String updater = "--------------------------------------\n"
                       + "										\n"
                       + "Periodic Update Parameters			\n"
                       + "										\n"
                       + "updates:								\n"
                       + "      Turn the feature on  - \"enable\"\n"
                       + "      Turn the feature off - \"disable\"\n"
                       + "										\n"
                       + "interval:								\n"
                       + "		Time interval between updates	\n"
                       + "			 1 = 36 seconds	(0.01 hours)\n"
                       + "			10 =  6 minutes				\n"
                       + "--------------------------------------\n";
        // Init config nodes
        config.setComment(top);
        config.getNode("MySQL", "address").setComment(top).setValue("localhost");
        config.getNode("MySQL", "database").setValue("timeplayed");
        config.getNode("MySQL", "username").setValue("root");
        config.getNode("MySQL", "password").setValue("password");
        config.getNode("PeriodicUpdates", "timeinterval").setComment(updater).setValue("10");
        config.getNode("PeriodicUpdates", "updates").setValue("enable");
    }
}