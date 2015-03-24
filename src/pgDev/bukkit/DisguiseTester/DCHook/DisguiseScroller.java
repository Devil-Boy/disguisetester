package pgDev.bukkit.DisguiseTester.DCHook;

import java.util.Queue;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DisguiseScroller implements Runnable {
	DCHook dcHook;
	Player sender;
	
	Queue<String> disguises;
	
	public DisguiseScroller(DCHook dcHook, Player sender, Queue<String> disguises) {
		this.dcHook = dcHook;
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
				Bukkit.getServer().dispatchCommand(sender, "disguise " + disguise);
			}
		} else {
			endScroll();
		}
	}
	
	public void endScroll() {
		dcHook.scrollers.remove(sender.getName()).cancel();
	}
}
