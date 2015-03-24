package pgDev.bukkit.DisguiseTester.DCHook;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;
import pgDev.bukkit.DisguiseCraft.disguise.Disguise;
import pgDev.bukkit.DisguiseCraft.disguise.DisguiseType;
import pgDev.bukkit.DisguiseCraft.listeners.DCCommandListener;
import pgDev.bukkit.DisguiseCraft.listeners.protocol.PLPacketListener;
import pgDev.bukkit.DisguiseTester.DisguiseTester;

public class DCHook {

	// DisguiseCraft Hook
    DisguiseCraft dc;
    DisguiseCraftAPI dcAPI;
    PLPacketListener dcPL;
    
    // Disguise Databases
    public List<Integer> disguiseIDs = new LinkedList<Integer>();
    public Map<String, Disguise> testDisguises = new HashMap<String, Disguise>();
    public Map<String, Integer> objectData = new HashMap<String, Integer>();
    public Map<String, BukkitTask> scrollers = new HashMap<String, BukkitTask>();
    
    // Metadata editing methods
    public Method addData;
    public Method editData;
    
    // Listener
    DCHookListener listener = new DCHookListener(this);
    
	public DCHook(DisguiseTester plugin, Plugin dc) {
		// Create references
		this.dc = (DisguiseCraft) dc;
		dcAPI = DisguiseCraft.getAPI();
		
		// Get a hook on the metadata modifiers
		try {
	    	addData = Disguise.class.getDeclaredMethod("mAdd", int.class, Object.class);
	    	addData.setAccessible(true);
	        editData = Disguise.class.getDeclaredMethod("mWatch", int.class, Object.class);
	        editData.setAccessible(true);
    	} catch (Exception e) {
    		System.out.println("DisguiseTester could not find the methods it needs!");
    	}
		
		// Register our events
		Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
	}
	
	public boolean isDCCommand(String arg) {
    	return DisguiseType.subTypes.contains(arg) || DisguiseType.fromString(arg) != null ||
    			arg.toLowerCase().startsWith("hold") || Arrays.asList(DCCommandListener.subCommands).contains(arg);
    }
    
	public void onCommand(CommandSender sender, String label, String[] args) {
		if (args.length == 0) { // Needs help
			sender.sendMessage(ChatColor.GREEN + "Usage: /dt [create/index/objectdata/delete] <test disguise name>");
		} else if (args[0].equalsIgnoreCase("create")) {
			if (sender.hasPermission("disguisetester.disguise.create")) {
				if (args.length < 3) {
					sender.sendMessage(ChatColor.GREEN + "Usage: /dt create <test disguise name> <mobtype>");
				} else {
					if (isDCCommand(args[1])) {
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
			} else {
				sender.sendMessage(ChatColor.RED + "You do not have the permission to use this command.");
			}
		} else if (args[0].equalsIgnoreCase("index")) {
			if (sender.hasPermission("disguisetester.disguise.create")) {
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
							return;
						}
						if (args[4].equalsIgnoreCase("byte")) {
							try {
								Byte num = Byte.decode(args[5]);
								value = num.byteValue();
							} catch (NumberFormatException e) {
								sender.sendMessage("The byte value could not be decoded");
								return;
							}
						} else if (args[4].equalsIgnoreCase("int")) {
							try {
								Integer num = Integer.decode(args[5]);
								value = num.intValue();
							} catch (NumberFormatException e) {
								sender.sendMessage("The int value could not be decoded");
								return;
							}
						} else if (args[4].equalsIgnoreCase("short")) {
							try {
								Short num = Short.decode(args[5]);
								value = num.shortValue();
							} catch (NumberFormatException e) {
								sender.sendMessage("The short value could not be decoded");
								return;
							}
						} else if (args[4].equalsIgnoreCase("string")) {
							value = args[5];
						} else {
							sender.sendMessage(ChatColor.RED + "You did not specify a valid data type");
							return;
						}
						if (args[2].equalsIgnoreCase("add")) {
							try {
								addData.invoke(disguise, index, value);
							} catch (Exception e) {
								sender.sendMessage(ChatColor.RED + "The index value could not be added: " + ChatColor.ITALIC + e);
								return;
							}
						} else if (args[2].equalsIgnoreCase("edit")) {
							try {
								editData.invoke(disguise, index, value);
							} catch (Exception e) {
								sender.sendMessage(ChatColor.RED + "The index value could not be edited: " + ChatColor.ITALIC + e);
								return;
							}
						} else {
							sender.sendMessage(ChatColor.RED + "Index action not recognized");
							return;
						}
						testDisguises.put(args[1], disguise);
						sender.sendMessage(ChatColor.GOLD + "Test disguise \"" + args[1] + "\" successfully modified");
					} else {
						sender.sendMessage(ChatColor.RED + "A disguise with the specified name was not found");
					}
				}
			} else {
				sender.sendMessage(ChatColor.RED + "You do not have the permission to use this command.");
			}
		} else if (args[0].equalsIgnoreCase("objectdata")) {
			if (sender.hasPermission("disguisetester.disguise.create")) {
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
			} else {
				sender.sendMessage(ChatColor.RED + "You do not have the permission to use this command.");
			}
		} else if (args[0].equalsIgnoreCase("delete")) {
			if (sender.hasPermission("disguisetester.disguise.create")) {
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
			} else {
				sender.sendMessage(ChatColor.RED + "You do not have the permission to use this command.");
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
	        					
	        					scrollers.put(player.getName(), Bukkit.getServer().getScheduler().runTaskTimer(dc, new DisguiseScroller(this, (Player) sender, disguises), 1, tickDelay));
	        					
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
        					
        					scrollers.put(player.getName(), Bukkit.getServer().getScheduler().runTaskTimer(dc, new DisguiseScroller(this, (Player) sender, disguises), 1, 10));
        					
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
	}
}
