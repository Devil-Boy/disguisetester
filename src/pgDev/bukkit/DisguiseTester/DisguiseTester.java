package pgDev.bukkit.DisguiseTester;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import pgDev.bukkit.DisguiseCraft.Disguise;
import pgDev.bukkit.DisguiseCraft.Disguise.MobType;
import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class DisguiseTester extends JavaPlugin {
	// File Locations
    static String pluginMainDir = "./plugins/DisguiseTester";
    static String pluginConfigLocation = pluginMainDir + "/DisguiseTester.cfg";
    
	// Permissions support
    static PermissionHandler Permissions;
    
    // DisguiseCraft Hook
    DisguiseCraftAPI dcAPI;
    
    // Listener
    DTMainListener mainListener = new DTMainListener(this);
    
    // Plugin Configuration
    public DTConfig pluginSettings;
    
    // Disguise Databases
    public LinkedList<Integer> disguiseIDs = new LinkedList<Integer>();
    public HashMap<String, Disguise> testDisguises = new HashMap<String, Disguise>();
	
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
        
		// Register our events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(mainListener, this);
		
		// Integrate!
        setupPermissions();
        setupDisguiseCraft();
		if (pluginSettings.packetDebug) {
			if (spoutEnabled()) {
				new DTPacketListener(this);
			} else {
				System.out.println("DisguiseTester's packet monitor requires Spout.");
			}
		}
        
        // Ello!
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
	}
	
	public void onDisable() {
		System.out.println("DisguiseTester disabled!");
	}
	
	// Permissions Methods
    private void setupPermissions() {
        Plugin permissions = this.getServer().getPluginManager().getPlugin("Permissions");

        if (Permissions == null) {
            if (permissions != null) {
                Permissions = ((Permissions)permissions).getHandler();
            } else {
            }
        }
    }
    
    public boolean hasPermissions(Player player, String node) {
        if (Permissions != null) {
        	return Permissions.has(player, node);
        } else {
            return player.hasPermission(node);
        }
    }
    
    // DC API Obtainer
    public void setupDisguiseCraft() {
    	dcAPI = DisguiseCraft.getAPI();
    }
    
    public boolean spoutEnabled() {
		return (this.getServer().getPluginManager().getPlugin("Spout") != null);
	}
    
    // Functions
    public boolean isDC(String arg) {
    	return MobType.subTypes.contains(arg) || MobType.fromString(arg) != null || arg.toLowerCase().startsWith("hold");
    }
    
    // Command Handling
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	if (label.equalsIgnoreCase("dt")) {
    		if (!(sender instanceof Player) || hasPermissions((Player) sender, "disguisetester.disguise.create")) {
    			if (args.length == 0) { // Needs help
    				sender.sendMessage(ChatColor.GREEN + "Usage: /dt [create/index/delete] <test disguise name>");
    			} else if (args[0].equalsIgnoreCase("create")) {
    				if (args.length < 3) {
    					sender.sendMessage(ChatColor.GREEN + "Usage: /dt create <test disguise name> <mobtype>");
    				} else {
    					if (isDC(args[1])) {
							sender.sendMessage(ChatColor.RED + "That test disguise name may conflict with DisguiseCraft. Please use another.");
						} else {
							MobType type = MobType.fromString(args[2]);
	    					if (type == null) {
	    						sender.sendMessage(ChatColor.RED + "Mob type not recognized");
	    					} else {
	    						testDisguises.put(args[1], new Disguise(dcAPI.newEntityID(), type));
	    						sender.sendMessage(ChatColor.GOLD + "Test disguise \"" + args[1] + "\" created");
	    					}
						}
    				}
    			} else if (args[0].equalsIgnoreCase("index")) {
    				
    			} else if (args[0].equalsIgnoreCase("delete")) {
    				if (args.length < 2) {
    					sender.sendMessage(ChatColor.GREEN + "Usage: /dt delete <test disguise name>");
    				} else {
    					if (testDisguises.containsKey(args[1])) {
    						testDisguises.remove(args[1]);
    						sender.sendMessage(ChatColor.GOLD + "Test disguise \"" + args[1] + "\" deleted");
    					} else {
    						sender.sendMessage(ChatColor.RED + "A test disguise with the specified name could not be found");
    					}
    				}
    			}
    		} else {
    			sender.sendMessage(ChatColor.RED + "You do not have the permission to use this command.");
    		}
    	}
    	return true;
    }
}
