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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Provides a container object for the database used for this plugin.
 */
final class TimePlayedDatabase {

    /**
     * Holder for the {@link TimePlayedDatabase} singleton instance.
     */
    private static class TimePlayedDatabaseHolder {

        /**
         * {@link TimePlayedDatabase} singleton instance.
         */
        private static final TimePlayedDatabase INSTANCE = new TimePlayedDatabase();
    }

    /**
     * Sponge sql service.
     */
    private SqlService sql;

    /**
     * Connection url.
     */
    private String connectionUrl = "jdbc:mysql://" + "root" + ":password" + "" + "@" + "localhost" + "/" + "timeplayed";

    /**
     * The instance of the {@link TimePlayedDatabase#connectionUrl connection url}
     */
    private Connection connection = null;

    /**
     * @return the {@link TimePlayedDatabase} singleton instance.
     */
    public static TimePlayedDatabase getInstance() {
        return TimePlayedDatabaseHolder.INSTANCE;
    }

    /**
     * Setup the database connection link and create the table if it does not yet exist.
     *
     * @param connectionUrl the string form of a database connection.
     * @return true if the first result is a ResultSet object; false if the first result is an update count or there is
     *          no result
     * @throws SQLException
     *          if a database access error occurs while trying to close {@link TimePlayedDatabase#connection}.
     */
    public boolean setup(String connectionUrl) throws SQLException {
        // Init vars
        this.connectionUrl = connectionUrl;
        Sponge.getServiceManager().provide(SqlService.class).ifPresent(sqlService->this.sql = sqlService);
        // Check db table
        try {
            connection = getDataSource().getConnection();
            return connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS `timeplayed_table` ( " +
                    "`Player Name` CHAR(32) NOT NULL , " +
                    "`Hours Played` CHAR(32) NOT NULL DEFAULT '0' , " +
                    "`Last Played` CHAR(32) NOT NULL ) ENGINE = InnoDB;")
                    .execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
        return false;
    }

    /**
     * @return the sql data source from the {@link TimePlayedDatabase#connectionUrl}.
     * @throws SQLException if a connection to the given database could not be established.
     * @throws NullPointerException if {@link TimePlayedDatabase#sql} is null.
     */
    private DataSource getDataSource() throws SQLException, NullPointerException {
        return sql.getDataSource(connectionUrl);
    }

    /**
     * @param statement sql query to execute.
     * @throws SQLException
     *          if a database access error occurs while trying to close {@link TimePlayedDatabase#connection}.
     */
    private void mySQLQuery(String statement) throws SQLException {
        try {
            connection = getDataSource().getConnection();
            connection.prepareStatement(statement).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }

    /**
     * @param statement sql query to execute.
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements
     *         or (2) 0 for SQL statements that return nothing.
     * @throws SQLException
     *          if a database access error occurs while trying to close {@link TimePlayedDatabase#connection}.
     */
    private ResultSet mySQLQueryReturn(String statement) throws SQLException {
        ResultSet result = null;
        connection = getDataSource().getConnection();
        try {
            result = connection.prepareStatement(statement).executeQuery();
        } catch (Exception SQLException) {
            SQLException.printStackTrace();
        } finally {
            connection.close();
        }
        return result;
    }

    /**
     * @param playerName name of the player to update.
     * @return a {@link ResultSet} for the player.
     */
    public ResultSet grabPlayerfromTable(String playerName) {
        ResultSet playerData = null;
        try {
            playerData = mySQLQueryReturn("SELECT * FROM `timeplayed_table` WHERE `Player Name`='" + playerName + "'");
        } catch (Exception SQLException) {
            SQLException.printStackTrace();
        }
        return playerData;
    }

    /**
     * @param playerName name of the player to update.
     * @param timeJoined the time that the player joined the server.
     */
    public void addPlayertoTable(String playerName, String timeJoined) {
        try {
            mySQLQuery("INSERT INTO `timeplayed_table`(`Player Name`, `Last Played`) VALUES ('" + playerName + "','" + timeJoined + "')");
        } catch (Exception SQLException) {
            SQLException.printStackTrace();
        }
    }

    /**
     *
     * @param playerName  name of the player to update.
     * @param hoursPlayed the total time the player has played for.
     * @param time        the last played time of the player.
     */
    public void updatePlayerinTable(String playerName, String hoursPlayed, String time) {
        try {
            mySQLQuery("UPDATE timeplayed_table SET `Hours Played`='" + hoursPlayed + "', `Last Played`='" + time + "' WHERE `Player Name`='" + playerName + "'");
        } catch (Exception SQLException) {
            SQLException.printStackTrace();
        }
    }
}

