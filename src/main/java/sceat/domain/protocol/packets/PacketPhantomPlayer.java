package sceat.domain.protocol.packets;

import java.util.UUID;

import sceat.domain.Manager;
import sceat.domain.minecraft.Grades;
import sceat.domain.network.Core;
import sceat.domain.network.server.Server;
import sceat.domain.network.server.Server.ServerType;
import sceat.domain.protocol.PacketSender;
import sceat.domain.utils.ServerLabel;

public class PacketPhantomPlayer extends PacketPhantom {

	private UUID player;
	private PlayerAction action;
	private Grades grade;
	private String serverLabelLast;
	private String serverLabelNew;

    public PacketPhantomPlayer(UUID uid, PlayerAction action, Grades gr, String serverlabelLast, String serverLabelNew) {
        this.player = uid;
        this.action = action;
        this.grade = gr;
        this.serverLabelLast = serverlabelLast;
        this.serverLabelNew = serverLabelNew;
    }

    public PacketPhantomPlayer() {
    }

    @Override
	protected void serialize_() {
		writeString(this.player.toString());
		writeByte(this.action.getId());
		writeString(this.serverLabelLast);
		writeString(this.serverLabelNew);
		writeByte(grade.getValue());
	}

	@Override
	protected void deserialize_() {
		this.player = UUID.fromString(readString());
		this.action = PlayerAction.valueOf(readByte());
		this.serverLabelLast = readString();
		this.serverLabelNew = readString();
		this.grade = Grades.fromValue(readByte() , true);
	}

	@Override
	public void handleData() {
        if (action == PlayerAction.CONNECT) {
            Manager.getInstance().getPlayersOnNetwork().add(player);
            Manager.getInstance().getPlayersPerGrade().get(grade).add(player);
            getServerNew().getPlayersMap().get(grade).add(player);
            Core.getInstance().getPlayersByType().get(getServerTypeNew()).add(player);
        } else if (action == PlayerAction.DISCONNECT){
            Manager.getInstance().getPlayersOnNetwork().removeIf(e -> e.equals(player));
            Manager.getInstance().getPlayersPerGrade().get(grade).removeIf(e -> e.equals(player));
            getServerLast().getPlayers().removeIf(e -> e.equals(player));
            Core.getInstance().getPlayersByType().get(getServerTypeLast()).removeIf(e -> e.equals(player));
        }
        else
            throw new IllegalStateException();
        PacketSender.getInstance().sendPlayer(this);
	}

	public Grades getGrade() {
		return grade;
	}

	public ServerType getServerTypeLast() {
		if (getAction() == PlayerAction.CONNECT)
            throw new NullPointerException("Impossible de récuperer le type de serveurLast car le joueur vient de se connecter sur le network !");
		return ServerLabel.getTypeWithLabel(this.serverLabelLast);
	}

	public ServerType getServerTypeNew() {
		if (getAction() == PlayerAction.DISCONNECT)
            throw new NullPointerException("Impossible de récuperer le type de serveurNew car le joueur vient de se d�connecter du network !");
		return ServerLabel.getTypeWithLabel(this.serverLabelNew);
	}

	/**
	 * Attention cette methode peut creer du sale NPE
	 * 
	 * @return le serveur via le manager grace au label
	 */
	public Server getServerLast() {
		if (getAction() == PlayerAction.CONNECT)
			throw new NullPointerException("Impossible de récuperer le serveurLast car le joueur vient de se connecter sur le network !");
		return Manager.getInstance().getServersByLabel().get(this.serverLabelLast);
	}

	public Server getServerNew() {
		if (getAction() == PlayerAction.DISCONNECT)
            throw new NullPointerException("Impossible de récuperer le serveurNew car le joueur vient de se déconnecter du network !");
		return Manager.getInstance().getServersByLabel().get(this.serverLabelNew);
	}

	public UUID getPlayer() {
		return player;
	}

	public PlayerAction getAction() {
		return action;
	}

	public enum PlayerAction {
        CONNECT((byte)0),
        DISCONNECT((byte)1);
        private final byte id;

        PlayerAction(byte id){
            this.id = id;
        }

        public byte getId() {
            return id;
        }

        public static PlayerAction valueOf(byte id){
            for(PlayerAction action : values())
                if(action.id == id)
                    return action;
            return null;
        }
    }

}
