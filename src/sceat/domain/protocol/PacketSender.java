package sceat.domain.protocol;

import sceat.SPhantom;
import sceat.domain.adapter.mq.IMessaging;
import sceat.domain.protocol.dao.DAO_HeartBeat;
import sceat.domain.protocol.packets.PacketPhantomPlayer;
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

	/**
	 * Tout ce qui n'est pas interne comme le heartbeat doit verifier d'être sur le bon replica avant de pouvoir envoyer le packet, si cet instance de Sphantom est en pause alors on n'envoie rien
	 */
	public void sendServer(PacketPhantomServerInfo pkt) {
		if (allowed) getBroker().sendServer(pkt.toByteArray());
	}

	public void takeLead(DAO_HeartBeat json) {
		getBroker().takeLead(json);
	}

	public void heartBeat(DAO_HeartBeat json) {
		getBroker().heartBeat(json);
	}

	public void sendPlayer(PacketPhantomPlayer pkt) {
		if (allowed) getBroker().sendPlayer(pkt.toByteArray());
	}

}
