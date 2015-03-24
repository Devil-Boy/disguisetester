package pgDev.bukkit.DisguiseTester.DCHook;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.logging.Level;

import net.minecraft.server.v1_8_R2.Packet;
import net.minecraft.server.v1_8_R2.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R2.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_8_R2.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.disguise.*;
import pgDev.bukkit.DisguiseCraft.api.*;
import pgDev.bukkit.DisguiseTester.DisguiseTester;

public class DCHookListener implements Listener {
	final DCHook dcHook;
	
	public DCHookListener(final DCHook dcHook) {
		this.dcHook = dcHook;
	}
	
	@EventHandler
	public void onDisguiseCommand(DCCommandEvent event) {
		if (event.getLabel().toLowerCase().startsWith("d") && event.getArgs().length > 0) {
			String disguiseName = event.getArgs()[0];
			if (dcHook.testDisguises.containsKey(disguiseName)) {
				if (event.getSender().hasPermission("disguisetester.disguise.wear")) {
					Player disguisee = event.getPlayer();
					Disguise disguise = dcHook.testDisguises.get(disguiseName);
					Player player = event.getPlayer();
					
					if (disguise.type.isObject()) {
						if (dcHook.dcAPI.isDisguised(player)) {
							dcHook.dcAPI.undisguisePlayer(player);
						}
						
						dcHook.dc.disguiseDB.put(player.getUniqueId(), disguise);
						dcHook.dc.disguiseIDs.put(disguise.entityID, player);
						disguiseBlockToWorld(disguiseName, player, player.getWorld());
				    	
				    	// Start position updater
						dcHook.dc.setPositionUpdater(player.getUniqueId(), disguise);
					} else {
						if (dcHook.dcAPI.isDisguised(player)) {
							dcHook.dcAPI.changePlayerDisguise(disguisee, disguise);
						} else {
							dcHook.dcAPI.disguisePlayer(disguisee, disguise);
						}
					}
					
					disguisee.sendMessage(ChatColor.GOLD + "You have taken the disguise: " + disguiseName);
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onDisguise(PlayerDisguiseEvent event) {
		if (!event.isCancelled()) {
			dcHook.disguiseIDs.add(event.getDisguise().entityID);
		}
	}
	
	@EventHandler
	public void onUnDisguise(PlayerUndisguiseEvent event) {
		if (!event.isCancelled()) {
			dcHook.disguiseIDs.remove((Object) dcHook.dcAPI.getDisguise(event.getPlayer()).entityID);
		}
	}
	
	public void disguiseBlockToWorld(String dName, Player player, World world) {
		LinkedList<Packet> toSend = new LinkedList<Packet>();
		Disguise disguise = dcHook.dcAPI.getDisguise(player);
    	
    	for (Player observer : world.getPlayers()) {
	    	if (observer != player) {
	    		if (observer.hasPermission("disguisecraft.seer")) {
	    			toSend.addFirst(getObjectSpawnPacket(dName, disguise, player.getLocation()));
	    			
	    			if (dcHook.dcPL != null) {
	    				// Keep them in tab list
		    			if (DisguiseCraft.pluginSettings.noTabHide) {
		    				
		    				dcHook.dcPL.recentlyDisguised.add(player.getUniqueId());
		    			} else {
		    				toSend.add(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, ((CraftPlayer) player).getHandle()));
		    			}
    				}
				} else {
					toSend.addFirst(getObjectSpawnPacket(dName, disguise, player.getLocation()));
					
					if (dcHook.dcPL != null) {
						if (DisguiseCraft.pluginSettings.noTabHide) {
							dcHook.dcPL.recentlyDisguised.add(player.getUniqueId());
						}
					}
				}
	    		observer.hidePlayer(player);
	    		dcHook.dc.sendPacketsToObserver(observer, toSend);
    		}
    	}
	}
	
	public PacketPlayOutSpawnEntity getObjectSpawnPacket(String name, Disguise disguise, Location location) {
		PacketPlayOutSpawnEntity packet = disguise.packetGenerator.getObjectSpawnPacket(location);
		if (dcHook.objectData.containsKey(name)) {
			try {
				Field dataField = packet.getClass().getDeclaredField("k");
				dataField.setAccessible(true);
				dataField.set(packet, dcHook.objectData.get(name).intValue());
			} catch (Exception e) {
				DisguiseTester.logger.log(Level.SEVERE, "Failes to set object data", e);
			}
		}
		return packet;
	}
}
