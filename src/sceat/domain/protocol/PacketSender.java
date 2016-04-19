package sceat.domain.protocol;

import sceat.SPhantom;
import sceat.domain.adapter.mq.IMessaging;
import sceat.domain.protocol.packets.PacketPhantom;
import sceat.domain.protocol.packets.PacketPhantomBootServer;
import sceat.domain.protocol.packets.PacketPhantomDestroyInstance;
import sceat.domain.protocol.packets.PacketPhantomHeartBeat;
import sceat.domain.protocol.packets.PacketPhantomPlayer;
import sceat.domain.protocol.packets.PacketPhantomReduceServer;
import sceat.domain.protocol.packets.PacketPhantomServerInfo;

public class PacketSender {

	private static PacketSender instance;
	private IMessaging broker;
	private boolean allowed = false;

	public PacketSender(String user, String pass, boolean local) {
		instance = this;
		broker = SPhantom.getInstance().initBroker(user, pass, local);
		allowed = local;
	}

	public static PacketSender getInstance() {
		return instance;
	}

	/**
	 * Empeche Sphantom d'envoyer des packet mise a part HeartBeat/Takelead
	 * 
	 * @param pause
	 *            mettre en pause ou reveiller
	 */
	public void pause(boolean pause) {
		allowed = !pause;
	}

	public IMessaging getBroker() {
		return broker;
	}

	private void setSecurity(PacketPhantom pkt) {
		pkt.setSecu(SPhantom.getInstance().getSecurity());
	}

	/**
	 * Tout ce qui n'est pas interne comme le heartbeat doit verifier d'être sur le bon replica avant de pouvoir envoyer le packet, si cet instance de Sphantom est en pause alors on n'envoie rien
	 */
	public void sendServer(PacketPhantomServerInfo pkt) {
		setSecurity(pkt);
		if (allowed) getBroker().sendServer(pkt.serialize());
	}

	public void takeLead(PacketPhantomHeartBeat pkt) {
		setSecurity(pkt);
		if (SPhantom.getInstance().logPkt()) SPhantom.print(">>>>]SEND] PacketTakeLead |to:SPHANTOM");
		getBroker().takeLead(pkt.serialize());
	}

	public void heartBeat(PacketPhantomHeartBeat pkt) {
		setSecurity(pkt);
		if (SPhantom.getInstance().logPkt()) SPhantom.print(">>>>]SEND] PacketHeartBeat |to:SPHANTOM");
		getBroker().heartBeat(pkt.serialize());
	}

	public void sendPlayer(PacketPhantomPlayer pkt) {
		setSecurity(pkt);
		if (allowed) {
			if (SPhantom.getInstance().logPkt()) SPhantom.print(">>>>]SEND] PacketPlayer |to:HUBS_PROXY_SPHANTOM");
			getBroker().sendPlayer(pkt.serialize());
		}
	}

	/**
	 * Only for get up to date other sphantom instances
	 * 
	 * @param pkt
	 */
	public void triggerDestroyInstance(PacketPhantomDestroyInstance pkt) {
		setSecurity(pkt);
		if (allowed) {
			if (SPhantom.getInstance().logPkt()) SPhantom.print(">>>>]SEND] PacketDestroyInstance |to:SPHANTOM");
			getBroker().destroyInstance(pkt.serialize());
		}
	}
	
	public void reduceServer(PacketPhantomReduceServer pkt) {
		setSecurity(pkt);
		if(allowed) {
			if (SPhantom.getInstance().logPkt()) SPhantom.print(">>>>]SEND] PacketReduceServer |to:ALL_AND_SPHANTOM");
			getBroker().destroyInstance(pkt.serialize());
		}
	}

	public void bootServer(PacketPhantomBootServer pkt) {
		setSecurity(pkt);
		if (allowed) {
			if (SPhantom.getInstance().logPkt()) SPhantom.print(">>>>]SEND] PacketBootServer |to:HUBS_PROXY_SPHANTOM_SYMBIOTE");
			getBroker().bootServer(pkt.serialize());
		}
	}

}
