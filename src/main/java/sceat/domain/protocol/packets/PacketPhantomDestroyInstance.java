package sceat.domain.protocol.packets;

import java.util.HashSet;
import java.util.Set;

import sceat.domain.common.mq.Broker;
import sceat.domain.network.Core;
import sceat.domain.network.ServerProvider;
import sceat.domain.network.server.Vps;
import sceat.domain.trigger.PhantomTrigger;
import fr.aresrpg.sdk.protocol.MessagesType;
import fr.aresrpg.sdk.protocol.PacketPhantom;
import fr.aresrpg.sdk.system.Log;

public class PacketPhantomDestroyInstance extends PacketPhantom {

	private Set<String> labels = new HashSet<String>();

	public PacketPhantomDestroyInstance(Set<String> label) {
		this.labels = label;
	}

	public PacketPhantomDestroyInstance() {
	}

	@Override
	protected void serialize_() {
		writeCollection(getLabels(), t -> writeString(t));
	}

	@Override
	protected void deserialize_() {
		this.labels = readCollection(labels, () -> readString());
	}

	@Override
	public void handleData(MessagesType tp) {
		if (cameFromLocal()) return;
		labels.forEach(vpsLabel -> { // apres le cameFromLocal() car c'est juste pour les autres instance (pour le publisher du packet on fait direct dans core)
			Vps v = Core.getInstance().getVps().getOrDefault(vpsLabel, null);
			if (v != null) PhantomTrigger.getAll().forEach(t -> t.handleVps(v));
		});
		Log.packet(this, true);
		labels.forEach(s -> {
			Core.getInstance().getVps().remove(s);
			ServerProvider.getInstance().getOrdered().entrySet().stream().filter(e -> e.getValue().getLabel().equals(s)).forEach(e -> ServerProvider.getInstance().getOrdered().put(e.getKey(), null));
		});
	}

	@Override
	public String toString() {
		return "PacketDestroyInstance [" + getLabels().stream().reduce((a, b) -> a + " " + b) + "]";
	}

	public Set<String> getLabels() {
		return labels;
	}

	@Override
	public void send() {
		Broker.get().destroyInstance(this);
	}

}
