package pgDev.bukkit.DisguiseTester;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

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
import pgDev.bukkit.DisguiseCraft.debug.DebugPacketOutput;

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
    	
    	return true;
    }
}
