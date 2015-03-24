package pgDev.bukkit.DisguiseTester.PLHook;

import java.util.List;

import pgDev.bukkit.DisguiseTester.DisguiseTester;

import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.MonitorAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

public class PLHookListener extends MonitorAdapter {
	
	List<PacketContainer> packets;
	
	public PLHookListener(DisguiseTester plugin) {
		super(plugin, ConnectionSide.SERVER_SIDE);
	}
	
	public void setDestination(List<PacketContainer> dest) {
		packets = dest;
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		packets.add(event.getPacket());
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		// Not used
	}

}
