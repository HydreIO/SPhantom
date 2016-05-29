package sceat.domain.protocol.handler;

import java.util.UUID;

import sceat.domain.Manager;
import sceat.domain.network.Core;
import sceat.domain.network.ServerProvider;
import sceat.domain.network.server.Servers;
import sceat.domain.network.server.Vpss;
import sceat.domain.trigger.PhantomTrigger;
import sceat.domain.utils.IronHeart;
import fr.aresrpg.commons.concurrent.ConcurrentHashMap;
import fr.aresrpg.commons.util.collection.HashSet;
import fr.aresrpg.commons.util.collection.Set;
import fr.aresrpg.commons.util.map.EnumHashMap;
import fr.aresrpg.sdk.mc.Grades;
import fr.aresrpg.sdk.mc.Statut;
import fr.aresrpg.sdk.network.Server;
import fr.aresrpg.sdk.network.Vps;
import fr.aresrpg.sdk.protocol.IHandler;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomBanned;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomBootServer;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomBroadcast;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomDestroyInstance;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomGradeUpdate;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomHeartBeat;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomHeartBeat.BeatType;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomKillProcess;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomPlayer;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomReduceServer;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomServerInfo;
import fr.aresrpg.sdk.protocol.packets.PacketPhantomSymbiote;
import fr.aresrpg.sdk.system.Broker;
import fr.aresrpg.sdk.system.Log;

public class Handler implements IHandler {

	private static Handler instance = new Handler();

	private Handler() {

	}

	public static Handler get() {
		return instance;
	}

	@Override
	public void handle(PacketPhantomBanned t) {
		cantHandle(t);
	}

	@Override
	public void handle(PacketPhantomBootServer t) {
		if (t.getVpsLabel() != null) {
			Vps v = Core.getInstance().getVps().getOrDefault(t.getVpsLabel(), null);
			if (v != null) PhantomTrigger.getAll().forEach(tt -> tt.handleVps(v));
		}
		if (t.cameFromLocal()) return;
		Log.packet(t, true);
		Servers.fromPacket(new PacketPhantomServerInfo(Statut.CREATING, t.getLabel(), t.getVpsLabel(), t.getIp(), t.getType(), t.getMaxP(), t.getPort(), new EnumHashMap<Grades, Set<UUID>>(
				Grades.class), false), false);
	}

	@Override
	public void handle(PacketPhantomBroadcast t) {
		cantHandle(t);
	}

	@Override
	public void handle(PacketPhantomDestroyInstance t) {
		if (t.cameFromLocal()) return;
		t.getLabels().forEach(vpsLabel -> { // apres le cameFromLocal() car c'est juste pour les autres instance (pour le publisher du packet on fait direct dans core)
					Vps v = Core.getInstance().getVps().getOrDefault(vpsLabel, null);
					if (v != null) PhantomTrigger.getAll().forEach(tt -> tt.handleVps(v));
				});
		Log.packet(t, true);
		t.getLabels().forEach(s -> {
			Core.getInstance().getVps().remove(s);
			ServerProvider.getInstance().getOrdered().entrySet().stream().filter(e -> e.getValue().getLabel().equals(s)).forEach(e -> ServerProvider.getInstance().getOrdered().put(e.getKey(), null));
		});
	}

	@Override
	public void handle(PacketPhantomGradeUpdate t) {
		if (t.cameFromLocal()) return;
		Log.packet(t, true);
		Server sv = Manager.getInstance().getServersByLabel().safeGet(t.getServerLabel());
		sv.getPlayersMap().safeGet(t.getLastGrade()).safeRemove(t.getPlayer());
		sv.getPlayersMap().safeGet(t.getNewGrade()).add(t.getPlayer());
	}

	@Override
	public void handle(PacketPhantomHeartBeat t) {
		if (t.getType() == BeatType.BEAT) {
			Log.packet(t, true);
			IronHeart.beat(t.getPulse());
		} else if (t.getType() == BeatType.LEAD) { // inutile mais en cas ou je rajoute un autre type pour ce pkt
			if (t.cameFromLocal()) return;
			Log.packet(t, true);
			IronHeart.letLead(t.getPulse());
		}
	}

	@Override
	public void handle(PacketPhantomKillProcess t) {
		cantHandle(t);
	}

	@Override
	public void handle(PacketPhantomPlayer t) {
		if (t.cameFromLocal()) return;
		Log.packet(t, true);
		Manager m = Manager.getInstance();
		switch (t.getAction()) {
			case CONNECT:
				m.getPlayersOnNetwork().put(t.getPlayer(), t.getServerLabelNew());
				m.getServer(t.getServerLabelNew()).getPlayersMap().safeGet(t.getGrade()).add(t.getPlayer());
				break;
			case DISCONNECT:
				m.getPlayersOnNetwork().safeRemove(t.getPlayer());
				m.getServer(t.getServerLabelLast()).getPlayers().removeIf(e -> e == t.getPlayer());
				break;
			case SERVER_SWITCH:
				m.getPlayersOnNetwork().put(t.getPlayer(), t.getServerLabelNew());
				m.getServer(t.getServerLabelLast()).getPlayersMap().safeGet(t.getGrade()).safeRemove(t.getPlayer());
				m.getServer(t.getServerLabelNew()).getPlayersMap().safeGet(t.getGrade()).add(t.getPlayer());
				break;
			default:
				throw new IllegalStateException();
		}
		Broker.get().sendPlayer(t);
	}

	@Override
	public void handle(PacketPhantomReduceServer t) {
		Vps v = Core.getInstance().getVps().getOrDefault(t.getVpsLabel(), null);
		if (v != null) PhantomTrigger.getAll().forEach(tt -> tt.handleVps(v));
		if (t.cameFromLocal()) return;
		Log.packet(t, true);
		Manager.getInstance().getServersByLabel().safeGet(t.getLabel()).setStatus(Statut.REDUCTION);
	}

	@Override
	public void handle(PacketPhantomServerInfo t) {
		if (t.cameFromLocal()) return;
		Log.packet(t, true);
		Manager m = Manager.getInstance();
		if (t.getState() == Statut.CLOSING) {
			Server srv = Servers.fromPacket(t, true);
			Vps curr = null;
			if (srv == null) {
				Log.out("PacketPhantomServerInfo : State Closing | the server " + t.getLabel() + " is not registered | Ignoring (cause closing) ! break");
				return;
			} else if (t.getVpsLabel() == null) {
				bite: for (Vps vps : Core.getInstance().getVps().values()) {
					for (Server s : vps.getServers())
						if (s.getLabel().equalsIgnoreCase(t.getLabel())) {
							curr = vps;
							break bite;
						}
				}
			} else curr = Servers.getVps(srv);
			Set<Server> ss = Core.getInstance().getServersByType().safeGet(t.getType());
			ss.safeRemove(srv);
			m.getServersByLabel().safeRemove(t.getLabel());
			if (curr == null) {
				// vps not found osef car tt façon on le vire
				Log.out("PacketPhantomServerInfo : State Closing | the server " + t.getLabel() + " is registered but not in a Vps object | Info ! break");
				return;
			}
			curr.getServers().safeRemove(srv);
			Vps vsss = curr;
			PhantomTrigger.getAll().forEach(tt -> tt.handleVps(vsss)); // trigger
			PacketPhantomServerInfo.fromServer(srv).send();
			return;
		}
		Server srvf = Servers.fromPacket(t, false);
		srvf.heartBeat();
		m.getServersByLabel().put(t.getLabel(), srvf);
		Core.getInstance().getServersByType().safeGet(t.getType()).add(srvf);
		Set<UUID> pll = t.getPlayers();
		m.getPlayersOnNetwork().putAll(t.getPlayersMap());
		Core.getInstance().getPlayersByType().safeGet(t.getType()).addAll(pll);
		PacketPhantomServerInfo.fromServer(srvf).send();
		Vps v = Core.getInstance().getVps().getOrDefault(t.getVpsLabel(), null);
		if (v != null) PhantomTrigger.getAll().forEach(tt -> tt.handleVps(v));
	}

	@Override
	public void handle(PacketPhantomSymbiote t) {
		Log.packet(t, true);
		ConcurrentHashMap<String, Vps> varmap = Core.getInstance().getVps();
		if (varmap.containsKey(t.getVpsLabel())) varmap.get(t.getVpsLabel()).setUpdated(true).setState(t.getState()).setCreatedMilli(t.getCreated());
		else Vpss.register(new Vps(t.getVpsLabel(), t.getRam(), t.getIp(), new HashSet<>(), t.getCreated()).setUpdated(true).setState(t.getState()));
		Vps v = Core.getInstance().getVps().getOrDefault(t.getVpsLabel(), null);
		if (v != null) PhantomTrigger.getAll().forEach(tt -> tt.handleVps(v));
	}

}
