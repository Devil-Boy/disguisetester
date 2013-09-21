package pgDev.bukkit.DisguiseTester;

import java.util.Queue;

import org.bukkit.entity.Player;

public class DisguiseScroller implements Runnable {
	DisguiseTester plugin;
	Player sender;
	
	Queue<String> disguises;
	
	public DisguiseScroller(DisguiseTester plugin, Player sender, Queue<String> disguises) {
		this.plugin = plugin;
		this.sender = sender;
		this.disguises = disguises;
	}

	@Override
	public void run() {
		if (sender.isOnline()) {
			String disguise = disguises.poll();
			if (disguise == null) {
				endScroll();
			} else {
				plugin.getServer().dispatchCommand(sender, "disguise " + disguise);
			}
		} else {
			endScroll();
		}
	}
	
	public void endScroll() {
		plugin.scrollers.remove(sender.getName()).cancel();
	}
}
