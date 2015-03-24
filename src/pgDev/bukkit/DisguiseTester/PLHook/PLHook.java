package pgDev.bukkit.DisguiseTester.PLHook;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.MonitorAdapter;
import com.comphenix.protocol.events.PacketListener;

import pgDev.bukkit.DisguiseTester.DisguiseTester;

public class PLHook {

	ProtocolManager pm;
	PacketListener listener;
	
	public PLHook(DisguiseTester plugin, Plugin pl) {
		pm = ProtocolLibrary.getProtocolManager();
		listener = new PLHookListener(plugin);
	}
	
	void startListen() {
		pm.addPacketListener(listener);
	}
	
	void endListen() {
		pm.removePacketListener(listener);
	}
	
	public void onCommand(CommandSender sender, String label, String[] args) {
		
	}
}
