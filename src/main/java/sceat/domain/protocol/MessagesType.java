package sceat.domain.protocol;

/**
 * Enum de type de messages
 *
 * @author MrSceat
 */
public enum MessagesType {
	UPDATE_SERVER("exchange_server", 9),
	UPDATE_PLAYER_ACTION("exchange_playerAction", 3),
	UPDATE_PLAYER_GRADE("exchange_playerGrade", 8),
	HEART_BEAT("exchange_heartbeat", 1),
	BOOT_SERVER("exchange_symbiote_bootServer", 6),
	SYMBIOTE_INFOS("exchange_symbiote", 4),
	REDUCE_SERVER("exchange_reduceServer", 7),
	DESTROY_INSTANCE("exchange_destroyVps", 5),
	TAKE_LEAD("exchange_takelead", 2);

	private String exchangeName;
	private int priority;

	MessagesType(String name, int priority) {
		this.exchangeName = name;
		this.priority = priority;
	}

	public String getName() {
		return exchangeName;
	}

	public static MessagesType fromString(String exchangeN, boolean notNull) {
		for (MessagesType e : values())
			if (e.getName().equals(exchangeN)) return e;
		if (notNull) throw new NullPointerException("Aucun type de message n'a pour valeur " + exchangeN);
		return null;
	}

	public int getPriority() {
		return priority;
	}
}