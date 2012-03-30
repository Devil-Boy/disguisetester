package pgDev.bukkit.DisguiseTester;

import org.bukkit.entity.Player;
import org.bukkit.event.*;

import pgDev.bukkit.DisguiseCraft.Disguise;
import pgDev.bukkit.DisguiseCraft.api.*;

public class DTMainListener implements Listener {
	final DisguiseTester plugin;
	
	public DTMainListener(final DisguiseTester plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onDisguiseCommand(DCCommandEvent event) {
		if (event.getLabel().toLowerCase().startsWith("d")) {
			String disguiseName = event.getArgs()[0];
			if (plugin.testDisguises.containsKey(disguiseName)) {
				Player disguisee = event.getPlayer();
				if (!(event.getSender() instanceof Player) || plugin.hasPermissions(disguisee, "disguisetester.disguise.wear")) {
					Disguise disguise = plugin.testDisguises.get(disguiseName);
					if (plugin.dcAPI.isDisguised(event.getPlayer())) {
						plugin.dcAPI.changePlayerDisguise(disguisee, disguise);
					} else {
						plugin.dcAPI.disguisePlayer(disguisee, disguise);
					}
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onDisguise(PlayerDisguiseEvent event) {
		if (!event.isCancelled()) {
			plugin.disguiseIDs.add(event.getDisguise().entityID);
		}
	}
	
	@EventHandler
	public void onUnDisguise(PlayerUndisguiseEvent event) {
		if (!event.isCancelled()) {
			plugin.disguiseIDs.remove(plugin.dcAPI.getDisguise(event.getPlayer()).entityID);
		}
	}
}
