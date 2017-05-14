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

import java.nio.file.Path;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;

import com.google.inject.Inject;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;




@Plugin(id = "timeplayed", name = "TimePlayed", version = "1.0.0")

public class TimePlayed {
	
	//Configuration varables
	@Inject
	@DefaultConfig(sharedRoot = false)
	private Path defaultConfig;
	
	@Inject
    @DefaultConfig(sharedRoot = false)
    ConfigurationLoader<CommentedConfigurationNode> configLoader;

	//SQL variables
	private String connection = "";
	
	
	//Task variables
	private Task updater;
	private Boolean taskEnabled = Boolean.FALSE;
	private int timeInterval = 10;
	private int time = 360;
	
	
	//Sponge variables
	@Inject
	private Logger logger;
	

	
	
	public Logger getLogger(){
		return logger;
	}
		

	
	
	@Listener
	public void onInitialize(GameInitializationEvent event){
		
		this.getLogger().info("TimePlayed Plugin...   Loading");
		
		//Check for configuration file
		TimePlayedConfig.getInstance().setup(defaultConfig,configLoader);
		
		//Pull data from config file
		connection = TimePlayedConfig.getInstance().getmySQLConnection();
		
		taskEnabled = TimePlayedConfig.getInstance().getUpdateEnabled();
		timeInterval = TimePlayedConfig.getInstance().getTimeInterval();
		
		//timeInterval is a multiple of 36 seconds
		time = timeInterval * 36;
		
		//this.getLogger().info("Connection: "+connection.toString());
		
		//Initialize mySQL connection
		try {
			TimePlayedDatabase.getInstance().setup(connection);
			this.getLogger().info("Database loaded Successfully");
			
		} catch(Exception SQLException) {
			taskEnabled = Boolean.FALSE;
			this.getLogger().info("Database loading Failed");
		}

		
		
		//If database is successfully loaded, activate periodic updates task
		if( taskEnabled == Boolean.TRUE ) {
			
			//Re-init and execute task
			updater = Task.builder().execute(() -> ingamePlayerList())
				.async().delay(100, TimeUnit.MILLISECONDS)
				.interval(time, TimeUnit.SECONDS)
				.name("In-Game Player Task")
				.submit(this);
			
			this.getLogger().info("Periodic Updates Enabled with Interval = "+time+" seconds");
		} else {
			this.getLogger().info("Periodic Updates Disabled");
		}
		this.getLogger().info("Timeplayed Plugin...   Loaded");
		
	}
	
	
	@Listener
    public void onDisable(GameStoppingServerEvent event) {
	
		//Update hours played of all players still in server
		ingamePlayerList();
		
		if( taskEnabled == Boolean.TRUE ) {
			updater.cancel();
		}
	}
	
	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join event){
	
		ResultSet playerData = null;
		//Player variables
		String playerName = event.getTargetEntity().getName();
		String timeJoined = Instant.ofEpochMilli(System.currentTimeMillis()).toString();
		
		//this.getLogger().info("A Player has joined the server: "+playerName);
		
		//Check if Player Exists
		try {
			
			playerData = TimePlayedDatabase.getInstance().grabPlayerfromTable(playerName);
			
			if( playerData.first() == Boolean.TRUE ){
				
				//Player Exists, Update player in database
				String hoursPlayed = playerData.getString("Hours Played");
				
				TimePlayedDatabase.getInstance().updatePlayerinTable(playerName,hoursPlayed,timeJoined);
			} else {
				//Create player in database
				TimePlayedDatabase.getInstance().addPlayertoTable(playerName,timeJoined);
			}
		} catch(Exception SQLException) {
			SQLException.printStackTrace();
		}
		
	}
	
	
	
	
	@Listener
	public void onPlayerLeave(ClientConnectionEvent.Disconnect event){
		
		//Player name
		String playerName = event.getTargetEntity().getName();
		Instant timeLeft = Instant.ofEpochMilli(System.currentTimeMillis());
		
		//this.getLogger().info("A Player has left the server: "+playerName);
		
		try {
			ResultSet playerData = TimePlayedDatabase.getInstance().grabPlayerfromTable(playerName);
			if( playerData.first() == Boolean.TRUE ){
				updatePlayerHours(playerName,timeLeft,playerData);
			}
		} catch(Exception SQLException) {
			SQLException.printStackTrace();
		}
		
		
		
	}
	
	
	
	
	public void ingamePlayerList(){
		String playerName = "";
		
		Collection<Player> onlinePlayers = Sponge.getServer().getOnlinePlayers();
	
		Object[] onlinePlayersArray = onlinePlayers.toArray();
		
		//If there are players
		for(int i = 0 ; i < onlinePlayersArray.length ; i++){
			//this.getLogger().info("Player in-game: "+onlinePlayersArray[i].toString());
			playerName = onlinePlayersArray[i].toString().split("'")[1];
			//this.getLogger().info("Player: "+player1Name);
			Instant time = Instant.ofEpochMilli(System.currentTimeMillis());
			
			try {
				ResultSet playerData = TimePlayedDatabase.getInstance().grabPlayerfromTable(playerName);
				if( playerData.first() == Boolean.TRUE ){
					updatePlayerHours(playerName,time,playerData);
				}
			} catch(Exception SQLException) {
				SQLException.printStackTrace();
			}
			
		}
	}
	
	
	
	
	public void updatePlayerHours(String playerName,Instant time, ResultSet playerData){
		
		Instant oldtime = null;
		float oldHoursPlayed=0;
		
		float newHoursPlayed = 0;
		float sessionHoursPlayed = 0;
		
		try {
			playerData.absolute(1);
					
			oldtime = Instant.parse(playerData.getString("Last Played"));
			oldHoursPlayed = Float.parseFloat(playerData.getString("Hours Played"));	
		} catch(Exception SQLException) {
			SQLException.printStackTrace();
		}
		
		//Convert to milliseconds
		long sessionTime = time.toEpochMilli() - oldtime.toEpochMilli();
		
		//milliseconds to hour: 	ms * ( 1s * 1m * 1h / 1000 ms * 60s * 60m )
		
		//milliseconds to seconds
		sessionHoursPlayed = sessionTime / 1000L;
		
		//seconds to 36 seconds (0.01 hours)
		sessionHoursPlayed = sessionHoursPlayed / 36;
		
		//round to the nearest 0.01 hours
		sessionHoursPlayed = Math.round(sessionHoursPlayed);
		
		//convert from 0.01 hours to hours
		sessionHoursPlayed = sessionHoursPlayed / 100;
		
		//new total hours played
		newHoursPlayed = oldHoursPlayed + sessionHoursPlayed;
		
		
		//timePlayed = String.valueOf(newHoursPlayed);
		DecimalFormat df = new DecimalFormat("#.00");
		String timePlayed = df.format(newHoursPlayed);
		
		try {
			TimePlayedDatabase.getInstance().updatePlayerinTable(playerName,timePlayed, time.toString());
		} catch(Exception SQLException) {
			SQLException.printStackTrace();
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
}
