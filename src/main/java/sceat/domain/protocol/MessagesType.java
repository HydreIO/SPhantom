package sceat.domain.protocol;
/**
	 * Enum de type de messages
	 *
	 * @author MrSceat
	 */
	public enum MessagesType {
		UPDATE_SERVER("exchange_server", true, 9),
		UPDATE_PLAYER_ACTION("exchange_playerAction", false, 3),
		UPDATE_PLAYER_GRADE("exchange_playerGrade", false, 8),
		HEART_BEAT("exchange_heartbeat", false, 1),
		BOOT_SERVER("exchange_symbiote_bootServer", false, 6),
		SYMBIOTE_INFOS("exchange_symbiote", false, 4),
		REDUCE_SERVER("exchange_reduceServer", false, 7),
		DESTROY_INSTANCE("exchange_destroyVps", false, 5),
		TAKE_LEAD("exchange_takelead", false, 2);

		private final boolean canBeDropped;
		private String exchangeName;
		private int priority;

		MessagesType(String name, boolean canBeDropped, int priority) {
			this.exchangeName = name;
			this.canBeDropped = canBeDropped;
			this.priority = priority;
		}

		public String getName() {
			return exchangeName;
		}

		public boolean canBeDropped() {
			return canBeDropped;
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