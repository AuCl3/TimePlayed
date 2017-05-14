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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;






public class TimePlayedDatabase {

	//SQL variables
	private SqlService sql;
	private String connection = "jdbc:mysql://"+"root"+":password"+""+"@"+"localhost"+"/"+"timeplayed";
	private Connection conn = null;
	
	
	private static TimePlayedDatabase instance = new TimePlayedDatabase();
	
	
	public static TimePlayedDatabase getInstance(){
		return instance;
	}
	
	
	//Check that connection is valid and that table exists
	public void setup(String connection) throws SQLException {
		
		this.connection = connection;
		
		
		conn = getDataSource().getConnection();
		
		try {
			//Check if table exists
			ResultSet Table = conn.getMetaData().getTables(null, null, "%", null);
			
			Table.next();
			if( !Table.getString(3).equalsIgnoreCase("timeplayed_table")) {

				conn.prepareStatement("CREATE TABLE `timeplayed_table` ( `Player Name` CHAR(32) NOT NULL , `Hours Played` CHAR(32) NOT NULL DEFAULT '0' , `Last Played` CHAR(32) NOT NULL ) ENGINE = InnoDB;").execute();
				
			}	
			
		} catch(Exception SQLException) {
			
			SQLException.printStackTrace();
			
			
		} finally {
			conn.close();
		}
		
	}
	

	
	
	
	public javax.sql.DataSource getDataSource() throws SQLException {
	    if (sql == null) {
	        sql = Sponge.getServiceManager().provide(SqlService.class).get();
	    }
	    return sql.getDataSource(connection);
	}
	
	
	
	
	public void mySQLQuery(String statement) throws SQLException {
		
		conn = getDataSource().getConnection();
		
		try {
			conn.prepareStatement(statement).execute();
		} catch(Exception SQLException) {
			SQLException.printStackTrace();
		} finally {
			conn.close();
		}
	}
	
	
	
	
	public ResultSet mySQLQueryReturn(String statement) throws SQLException {
		
		ResultSet result = null;
		
		conn = getDataSource().getConnection();
		
		try {
			result = conn.prepareStatement(statement).executeQuery();
		} catch(Exception SQLException) {
			SQLException.printStackTrace();
		} finally {
			conn.close();
		}
		
		return result;
	}
	
	
	
	
	public ResultSet grabPlayerfromTable(String playerName) throws SQLException {
		
		ResultSet playerData = null;
		
		try {
			playerData = mySQLQueryReturn("SELECT * FROM `timeplayed_table` WHERE `Player Name`='"+playerName+"'");
		} catch(Exception SQLException) {
			SQLException.printStackTrace();
		} finally {
			
		}
		
		return playerData;
	}
	
	
	
	
	public void addPlayertoTable(String playerName, String timeJoined) throws SQLException {
		
		try {
			mySQLQuery("INSERT INTO `timeplayed_table`(`Player Name`, `Last Played`) VALUES ('"+playerName+"','"+timeJoined+"')");
		} catch(Exception SQLException) {
			SQLException.printStackTrace();
		} finally {
			
		}
		
	}
	
	
	
	
	public void updatePlayerinTable(String playerName, String hoursPlayed, String time) throws SQLException {
		
		try {
			mySQLQuery("UPDATE timeplayed_table SET `Hours Played`='"+hoursPlayed+"', `Last Played`='"+time+"' WHERE `Player Name`='"+playerName+"'");	
		} catch(Exception SQLException) {
			SQLException.printStackTrace();
		} finally {
			
		}
		
	}
	
	
	
}

