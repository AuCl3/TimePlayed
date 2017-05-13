package io.github.aucl3.timeplayed;

import java.io.IOException;
import java.nio.file.Path;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;


public class TimePlayedConfig {
	
	//private Path defaultConfig;
	private ConfigurationLoader<CommentedConfigurationNode> configLoader;
	private CommentedConfigurationNode config;

	
	//private config variables
	private String user = "";
	private String pass = "";
	private String addr = "";
	private String data = "";
	private String connection = "";
	
	private Boolean updates = Boolean.FALSE;
	private int timeinterval = 1;
	
	
	
	private static TimePlayedConfig instance = new TimePlayedConfig();
	
	
	public static TimePlayedConfig getInstance(){
		return instance;
	}
	

	
	public void setup(Path defaultConfig, ConfigurationLoader<CommentedConfigurationNode> configLoader){
		
		
		configLoader = HoconConfigurationLoader.builder().setPath(defaultConfig).build();
		
		this.configLoader = configLoader;
		
		
		try{
			
			
			config = this.configLoader.load();
			
			if(!defaultConfig.toFile().exists()) {
				
				
				//create default configuration file
				
				createConfig();
				
				
			}
		
			this.configLoader.save(config);
			
			
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
	public CommentedConfigurationNode getConfig(){
		return config;
	}
	
	
	public String getmySQLConnection(){
		
		user = getConfig().getNode("MySQL","username").getString();
		pass = getConfig().getNode("MySQL","password").getString();
		addr = getConfig().getNode("MySQL","address").getString();
		data = getConfig().getNode("MySQL","database").getString();
		connection = "";
		
		// Connection format:
		// "jdbc:mysql://"+"root"+":"+"password"+"@"+"localhost"+"/"+"tutorial01"
		
		connection = "jdbc:mysql://"+user+":"+pass+"@"+addr+"/"+data;
		
		return connection;
	}
	
	
	
	
	public Boolean getUpdateEnabled(){
		
		//By default, return false
		updates = Boolean.FALSE;
		String check = getConfig().getNode("PeriodicUpdates","updates").getString();
		
		if ( check.equalsIgnoreCase("enable") ) {
			updates = Boolean.TRUE;
		}
		
		return updates;
	}
	
	
	
	
	public int getTimeInterval(){
		
		timeinterval = getConfig().getNode("PeriodicUpdates","timeinterval").getInt();
		return timeinterval;
	}
	
	
	
	
	public void saveConfig(){
		try{
			configLoader.save(config);
			} catch(IOException e){
				e.printStackTrace();
			}
	}
	
	
	
	
	public void loadConfig(){
		try{
			configLoader.load();
			} catch(IOException e){
				e.printStackTrace();
			}
	}
	
	
	
	
	public void createConfig(){
		config.setComment("--------------------------------------------------------------------------------");
		config.setComment("     TimePlayed Configuration");
		config.setComment("--------------------------------------------------------------------------------");
		config.setComment("");
		config.setComment("Configure MySQL database ( database=host:port , default port=3306 )");
		config.setComment("");
		config.getNode("MySQL","address").setValue("localhost");
		config.getNode("MySQL","database").setValue("timeplayed");
		config.getNode("MySQL","username").setValue("root");
		config.getNode("MySQL","password").setValue("password");
		config.setComment("");
		config.setComment("");
		config.setComment("Configure periodic updating");
		config.setComment("");
		config.setComment("This option updates the database at a set interval.  This feature");
		config.setComment("reduces loss of data in the event of a sudden server stop");
		config.setComment("");
		config.setComment("Default is 6 minutes ( 1 interval = 36 seconds (0.01 hours))");
		config.setComment("");
		config.getNode("PeriodicUpdates","updates").setValue("enable");
		config.getNode("PeriodicUpdates","timeinterval").setValue("10");
		
		
	}
	
	
	 

}