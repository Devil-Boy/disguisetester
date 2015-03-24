package pgDev.bukkit.DisguiseTester;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseTester.DCHook.DCHook;
import pgDev.bukkit.DisguiseTester.PLHook.PLHook;

public class DisguiseTester extends JavaPlugin {
	// File Locations
    static String pluginMainDir = "./plugins/DisguiseTester";
    static String pluginConfigLocation = pluginMainDir + "/DisguiseTester.cfg";
    
    // Logger
    public static Logger logger;
    
    // Plugin Configuration
    public DTConfig pluginSettings;
    
    // Hooks
    DCHook dcHook;
    PLHook plHook;
    
    public void onLoad() {
    	logger = getLogger();
    }
	
	public void onEnable() {
		// Check for the plugin directory (create if it does not exist)
    	File pluginDir = new File(pluginMainDir);
		if(!pluginDir.exists()) {
			boolean dirCreation = pluginDir.mkdirs();
			if (dirCreation) {
				System.out.println("New DisguiseTester directory created!");
			}
		}
		
		// Load the Configuration
    	try {
        	Properties preSettings = new Properties();
        	if ((new File(pluginConfigLocation)).exists()) {
        		preSettings.load(new FileInputStream(new File(pluginConfigLocation)));
        		pluginSettings = new DTConfig(preSettings, this);
        		if (!pluginSettings.upToDate) {
        			pluginSettings.createConfig();
        			System.out.println("DisguiseTester Configuration updated!");
        		}
        	} else {
        		pluginSettings = new DTConfig(preSettings, this);
        		pluginSettings.createConfig();
        		System.out.println("DisguiseTester Configuration created!");
        	}
        } catch (Exception e) {
        	System.out.println("Could not load DisguiseTester configuration! " + e);
        }
        
        // Check if DisguiseCraft is on the server
        Plugin dc = getServer().getPluginManager().getPlugin("DisguiseCraft");
        if (dc != null) {
        	dcHook = new DCHook(this, dc);
        }
        
        // Check if ProtocolLib is on the server
        Plugin pl = this.getServer().getPluginManager().getPlugin("ProtocolLib");
        if (pl != null) {
        	plHook = new PLHook(this, pl);
        }
        
        // Ello!
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
	}
	
	public void onDisable() {
		System.out.println("DisguiseTester disabled!");
	}
    
    // Command Handling
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	if (command.getName().equalsIgnoreCase("dt")) {
    		if (dcHook != null) {
    			dcHook.onCommand(sender, label, args);
    		}
    	} else if (command.getName().equalsIgnoreCase("dtsu")) {
    		if (sender.hasPermission("disguisetester.su")) {
    			if (args.length < 2) {
    				sender.sendMessage(ChatColor.GREEN + "Usage: /" + label + " <player> <command>");
    			} else {
    				Player player = getServer().getPlayerExact(args[0]);
    				if (player == null) {
    					sender.sendMessage(ChatColor.RED + "That player could not be found");
    				} else {
    					String commandLine = null;
    					for (int i=1; i < args.length; i++) {
    						if (commandLine == null) {
    							commandLine = args[i];
    						} else {
    							commandLine += " " + args[1];
    						}
    					}
    					getServer().dispatchCommand(player, commandLine);
    					sender.sendMessage(ChatColor.GOLD + "Command \"" + commandLine + "\" sent by " + player.getName());
    				}
    			}
    		} else {
    			sender.sendMessage(ChatColor.RED + "You do not have permission to run commands as other players");
    		}
    	} else if (command.getName().equalsIgnoreCase("dtprofile")) {
    		if (sender.hasPermission("disguisetester.profile")) {
    			if (args.length == 0) {
    				if (sender instanceof Player) {
    					sender.sendMessage(ChatColor.GOLD + "Information for " + sender.getName());
    					sender.sendMessage(ChatColor.YELLOW + profileInfo(sender.getName()));
    				} else {
    					sender.sendMessage(ChatColor.RED + "You must specify a player name");
    				}
    			} else {
    				sender.sendMessage(ChatColor.GOLD + "Information for " + args[0]);
					sender.sendMessage(ChatColor.YELLOW + profileInfo(args[0]));
    			}
    		} else {
    			sender.sendMessage(ChatColor.RED + "You do not have permission to look up GameProfile information");
    		}
    	}
    	return true;
    }
    
    String profileInfo(String playerName) {
    	GameProfile profile = DisguiseCraft.profileCache.cache(playerName);
    	StringBuffer sb = new StringBuffer();
    	
    	sb.append("{\"id\":\"" + profile.getId() + "\",");
    	sb.append("\"name\":\"" + profile.getName() + "\",");
    	sb.append("\"properties\":[");
    	
    	for (Property property : profile.getProperties().values()) {
    		sb.append("{\"name\":\"" + property.getName() + "\",");
    		sb.append("\"value\":\"" + property.getValue() + "\",");
    		sb.append("\"signature\":\"" + property.getSignature() + "\"}");
    	}
    	
    	sb.append("]}");
    	
    	return sb.toString();
    }
}
