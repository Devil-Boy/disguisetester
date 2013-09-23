package pgDev.bukkit.DisguiseTester;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.disguise.*;
import pgDev.bukkit.DisguiseCraft.listeners.DCCommandListener;
import pgDev.bukkit.DisguiseCraft.listeners.DCPacketListener;
import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;

public class DisguiseTester extends JavaPlugin {
	// File Locations
    static String pluginMainDir = "./plugins/DisguiseTester";
    static String pluginConfigLocation = pluginMainDir + "/DisguiseTester.cfg";
    
    // Logger
    Logger logger;
    
    // DisguiseCraft Hook
    DisguiseCraft dc;
    DisguiseCraftAPI dcAPI;
    DCPacketListener dcPL;
    
    // Listener
    DTMainListener mainListener = new DTMainListener(this);
    
    // Plugin Configuration
    public DTConfig pluginSettings;
    
    // Disguise Databases
    public List<Integer> disguiseIDs = new LinkedList<Integer>();
    public Map<String, Disguise> testDisguises = new HashMap<String, Disguise>();
    public Map<String, Integer> objectData = new HashMap<String, Integer>();
    public Map<String, BukkitTask> scrollers = new HashMap<String, BukkitTask>();
    
    // Metadata editing methods
    public Method addData;
    public Method editData;
    
    public void onLoad() {
    	logger = getLogger();
    	
    	try {
	    	addData = Disguise.class.getDeclaredMethod("mAdd", int.class, Object.class);
	    	addData.setAccessible(true);
	        editData = Disguise.class.getDeclaredMethod("mWatch", int.class, Object.class);
	        editData.setAccessible(true);
    	} catch (Exception e) {
    		System.out.println("DisguiseTester could not find the methods it needs!");
    	}
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
        
		// Register our events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(mainListener, this);
		
		// Integrate!
        setupDisguiseCraft();
        
        // Ello!
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
	}
	
	public void onDisable() {
		System.out.println("DisguiseTester disabled!");
	}
    
    // DC API Obtainer
    public void setupDisguiseCraft() {
    	dc = (DisguiseCraft) getServer().getPluginManager().getPlugin("DisguiseCraft");
    	dcAPI = DisguiseCraft.getAPI();
    	
    	try {
    		Field field = dc.getClass().getDeclaredField("packetListener");
    		field.setAccessible(true);
			dcPL = (DCPacketListener) field.get(dc);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Could not get packetlistener object", e);
		}
    }
    
    // Functions
    public boolean isDC(String arg) {
    	return DisguiseType.subTypes.contains(arg) || DisguiseType.fromString(arg) != null ||
    			arg.toLowerCase().startsWith("hold") || Arrays.asList(DCCommandListener.subCommands).contains(arg);
    }
    
    // Command Handling
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	if (command.getName().equalsIgnoreCase("dt")) {
    		if (sender.hasPermission("disguisetester.disguise.create")) {
    			if (args.length == 0) { // Needs help
    				sender.sendMessage(ChatColor.GREEN + "Usage: /dt [create/index/objectdata/delete] <test disguise name>");
    			} else if (args[0].equalsIgnoreCase("create")) {
    				if (args.length < 3) {
    					sender.sendMessage(ChatColor.GREEN + "Usage: /dt create <test disguise name> <mobtype>");
    				} else {
    					if (isDC(args[1])) {
							sender.sendMessage(ChatColor.RED + "That test disguise name may conflict with DisguiseCraft. Please use another.");
						} else {
							DisguiseType type = DisguiseType.fromString(args[2]);
	    					if (type == null) {
	    						sender.sendMessage(ChatColor.RED + "Mob type not recognized");
	    					} else {
	    						testDisguises.put(args[1], new Disguise(dcAPI.newEntityID(), type));
	    						sender.sendMessage(ChatColor.GOLD + "Test disguise \"" + args[1] + "\" created");
	    					}
						}
    				}
    			} else if (args[0].equalsIgnoreCase("index")) {
    				if (args.length < 6) {
    					sender.sendMessage(ChatColor.GREEN + "Usage: /dt index <test disguise name> <add/edit> <index> <byte/short/int/string> <value>");
    				} else {
    					if (testDisguises.containsKey(args[1])) {
    						Disguise disguise = testDisguises.get(args[1]).clone();
    						int index;
    						Object value;
    						try {
    							index = Integer.decode(args[3]);
    						} catch (NumberFormatException e) {
    							sender.sendMessage(ChatColor.RED + "Invalid index specified");
    							return true;
    						}
    						if (args[4].equalsIgnoreCase("byte")) {
    							try {
    								Byte num = Byte.decode(args[5]);
    								value = num.byteValue();
    							} catch (NumberFormatException e) {
    								sender.sendMessage("The byte value could not be decoded");
    								return true;
    							}
    						} else if (args[4].equalsIgnoreCase("int")) {
    							try {
    								Integer num = Integer.decode(args[5]);
    								value = num.intValue();
    							} catch (NumberFormatException e) {
    								sender.sendMessage("The int value could not be decoded");
    								return true;
    							}
    						} else if (args[4].equalsIgnoreCase("short")) {
    							try {
    								Short num = Short.decode(args[5]);
    								value = num.shortValue();
    							} catch (NumberFormatException e) {
    								sender.sendMessage("The short value could not be decoded");
    								return true;
    							}
    						} else if (args[4].equalsIgnoreCase("string")) {
    							value = args[5];
    						} else {
    							sender.sendMessage(ChatColor.RED + "You did not specify a valid data type");
    							return true;
    						}
    						if (args[2].equalsIgnoreCase("add")) {
    							try {
    								addData.invoke(disguise, index, value);
    							} catch (Exception e) {
    								sender.sendMessage(ChatColor.RED + "The index value could not be added: " + ChatColor.ITALIC + e);
    								return true;
    							}
    						} else if (args[2].equalsIgnoreCase("edit")) {
    							try {
    								editData.invoke(disguise, index, value);
    							} catch (Exception e) {
    								sender.sendMessage(ChatColor.RED + "The index value could not be edited: " + ChatColor.ITALIC + e);
    								return true;
    							}
    						} else {
    							sender.sendMessage(ChatColor.RED + "Index action not recognized");
    							return true;
    						}
    						testDisguises.put(args[1], disguise);
    						sender.sendMessage(ChatColor.GOLD + "Test disguise \"" + args[1] + "\" successfully modified");
    					} else {
    						sender.sendMessage(ChatColor.RED + "A disguise with the specified name was not found");
    					}
    				}
    			} else if (args[0].equalsIgnoreCase("objectdata")) {
    				if (args.length < 3) {
    					sender.sendMessage(ChatColor.GREEN + "Usage: /" + label + " objectdata <test disguise name> <data value>");
    				} else {
    					if (testDisguises.containsKey(args[1])) {
    						Disguise disguise = testDisguises.get(args[1]);
    						
    						if (disguise.type.isObject()) {
    							try {
        							objectData.put(args[1], Integer.decode(args[2]));
        							sender.sendMessage(ChatColor.GOLD + "Data successfully set");
        						} catch (NumberFormatException e) {
        							sender.sendMessage(ChatColor.RED + "The data value you supplied could not be decoded");
        						}
    						} else {
    							sender.sendMessage(ChatColor.RED + "The disguise you specified is not of an object");
    						}
    					} else {
    						sender.sendMessage(ChatColor.RED + "A disguise with the specified name was not found");
    					}
    				}
    			} else if (args[0].equalsIgnoreCase("delete")) {
    				if (args.length < 2) {
    					sender.sendMessage(ChatColor.GREEN + "Usage: /dt delete <test disguise name>");
    				} else {
    					if (testDisguises.containsKey(args[1])) {
    						testDisguises.remove(args[1]);
    						if (objectData.containsKey(args[1])) {
    							objectData.remove(args[1]);
    						}
    						
    						sender.sendMessage(ChatColor.GOLD + "Test disguise \"" + args[1] + "\" deleted");
    					} else {
    						sender.sendMessage(ChatColor.RED + "A test disguise with the specified name could not be found");
    					}
    				}
    			} else if (args[0].equalsIgnoreCase("disguisescroll") || args[0].equalsIgnoreCase("ds")) {
    				if (sender instanceof Player) {
    					Player player = (Player) sender;
    					if (sender.hasPermission("disguisetester.disguise.scroll")) {
    						if (args.length > 1) {
    							if (args[1].equalsIgnoreCase("stop")) {
    								if (scrollers.containsKey(player.getName())) {
    									scrollers.get(player.getName()).cancel();
    									sender.sendMessage(ChatColor.GOLD + "Scroll stopped");
    								} else {
    									sender.sendMessage(ChatColor.RED + "You aren't scrolling");
    								}
        						} else {
        							try {
        								int tickDelay = Integer.parseInt(args[1]);
        								
        								LinkedList<String> disguises = new LinkedList<String>();
        	        					for (DisguiseType type : DisguiseType.values()) {
        	        						String disguise = type.toString().toLowerCase();
        	        						if (type == DisguiseType.Player) {
        	        							disguises.add(disguise + " Notch");
        	        						} else {
        	        							disguises.add(disguise);
        	        						}
        	        					}
        	        					
        	        					scrollers.put(player.getName(), getServer().getScheduler().runTaskTimer(this, new DisguiseScroller(this, (Player) sender, disguises), 1, tickDelay));
        	        					
        	        					sender.sendMessage(ChatColor.GOLD + "Scrolling...");
        							} catch (NumberFormatException e) {
        								sender.sendMessage(ChatColor.RED + "The provided delay could not be parsed");
        							}
        						}
    						} else {
    							if (scrollers.containsKey(player.getName())) {
    								sender.sendMessage(ChatColor.RED + "You are already scrolling");
    							} else {
    								LinkedList<String> disguises = new LinkedList<String>();
    	        					for (DisguiseType type : DisguiseType.values()) {
    	        						String disguise = type.toString().toLowerCase();
    	        						if (type == DisguiseType.Player) {
    	        							disguises.add(disguise + " Notch");
    	        						} else {
    	        							disguises.add(disguise);
    	        						}
    	        					}
    	        					
    	        					scrollers.put(player.getName(), getServer().getScheduler().runTaskTimer(this, new DisguiseScroller(this, (Player) sender, disguises), 1, 10));
    	        					
    	        					sender.sendMessage(ChatColor.GOLD + "Scrolling...");
    							}
    						}
        				} else {
        					sender.sendMessage(ChatColor.RED + "You do not have permission to scroll through disguises");
        				}
    				} else {
    					sender.sendMessage("You must be a player to scroll through disguises");
    				}
    			} else {
    				sender.sendMessage(ChatColor.RED + "First parameter not recognized");
    			}
    		} else {
    			sender.sendMessage(ChatColor.RED + "You do not have the permission to use this command.");
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
    			sender.sendMessage(ChatColor.RED + "You do not have permission to use this command");
    		}
    	}
    	return true;
    }
}
