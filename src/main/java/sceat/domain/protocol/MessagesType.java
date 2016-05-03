package sceat.domain.protocol;

/**
 * Enum de type de messages
 *
 * @author MrSceat
 */
public enum MessagesType {
	UPDATE_SERVER("exchange_server", 8),
	UPDATE_PLAYER_ACTION("exchange_playerAction", 4),
	UPDATE_PLAYER_GRADE("exchange_playerGrade", 2),
	HEART_BEAT("exchange_heartbeat", 10),
	BOOT_SERVER("exchange_symbiote_bootServer", 5),
	SYMBIOTE_INFOS("exchange_symbiote", 7),
	REDUCE_SERVER("exchange_reduceServer", 3),
	DESTROY_INSTANCE("exchange_destroyVps", 6),
	TAKE_LEAD("exchange_takelead", 9),
	KILL_PROCESS("exchange_killproc", 1);

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