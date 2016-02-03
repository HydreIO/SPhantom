package sceat.domain.protocol;

import sceat.SPhantom;
import sceat.domain.adapter.mq.IMessaging;
import sceat.domain.protocol.dao.DAO_HeartBeat;

public class PacketSender implements IMessaging {

	private static PacketSender instance;
	private IMessaging broker;
	private boolean allowed = false;

	public PacketSender(String user, String pass) {
		instance = this;
		broker = SPhantom.getInstance().initBroker(user, pass);
		allowed = true;
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
		allowed = pause;
	}

	public IMessaging getBroker() {
		return broker;
	}

	/**
	 * Tout ce qui n'est pas interne comme le heartbeat doit verifié d'être sur le bon replica avant de pouvoir envoyer le packet, si cet instance de Sphantom est en pause alors on n'evnoie rien
	 */
	@Override
	public void sendServer(String json) {
		if (allowed) getBroker().sendServer(json);
	}

	@Override
	public void takeLead(DAO_HeartBeat json) {
		getBroker().takeLead(json);
	}

	@Override
	public void heartBeat(DAO_HeartBeat json) {
		getBroker().heartBeat(json);
	}

	@Override
	public void sendPlayer(String json) {
		if (allowed) getBroker().sendPlayer(json);
	}

}
