package sceat.domain.protocol.packets;

import java.util.HashSet;
import java.util.Set;

public class PacketPhantomDestroyInstance extends PacketPhantom {

	@Override
	protected void serialize_() {
		writeCollection(getLabels(), t -> writeString(t));
	}

	@Override
	protected void deserialize_() {
		this.labels = readCollection(labels, () -> readString());
	}

	private Set<String> labels = new HashSet<String>();

	public PacketPhantomDestroyInstance(Set<String> label) {
		this.labels = label;
	}

	public Set<String> getLabels() {
		return labels;
	}

}
