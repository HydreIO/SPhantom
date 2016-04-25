package sceat.domain.protocol.packets;

import java.util.HashSet;
import java.util.Set;

import sceat.SPhantom;
import sceat.domain.network.Core;
import sceat.domain.network.ServerProvider;
import sceat.infra.connector.mq.RabbitMqConnector.MessagesType;

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
		if (SPhantom.getInstance().logPkt()) SPhantom.print("<<<<]RECV] PacketDestroyInstance [" + getLabels().stream().reduce((a, b) -> a + " " + b) + "]");
		labels.forEach(s -> {
			Core.getInstance().getVps().remove(s);
			ServerProvider.getInstance().getOrdered().entrySet().stream().filter(e -> e.getValue().getLabel().equals(s)).forEach(e -> ServerProvider.getInstance().getOrdered().put(e.getKey(), null));
		});
	}

	public Set<String> getLabels() {
		return labels;
	}

}
