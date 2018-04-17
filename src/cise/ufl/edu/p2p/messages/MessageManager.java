package cise.ufl.edu.p2p.messages;

import java.nio.ByteBuffer;

import cise.ufl.edu.p2p.messages.Message.Type;
import cise.ufl.edu.p2p.peer.Peer;

public class MessageManager {
	private static MessageManager messageManager = new MessageManager();
	private Peer host = Peer.getInstance();

	private MessageManager() {

	}

	public static MessageManager getInstance() {
		return messageManager;
	}

	public Type getType(byte type) {
		switch (type) {
		case 0:
			return Type.CHOKE;
		case 1:
			return Type.UNCHOKE;
		case 2:
			return Type.INTERESTED;
		case 3:
			return Type.NOTINTERESTED;
		case 4:
			return Type.HAVE;
		case 5:
			return Type.BITFIELD;
		case 6:
			return Type.REQUEST;
		case 7:
			return Type.PIECE;
		}
		return null;
	}

	public int getLength(byte[] messageLength) {
		ByteBuffer temp = ByteBuffer.wrap(messageLength);
		return temp.getInt();
	}

	public byte[] getMessageLength(Type messageType) {
		byte[] messageLength = new byte[4];
		switch (messageType) {
		case CHOKE:
		case UNCHOKE:
		case INTERESTED:
		case NOTINTERESTED:
			ByteBuffer bytebuffer = ByteBuffer.allocate(4);
			bytebuffer.putInt(0, 1);
			bytebuffer.get(messageLength, 0, 4);
			break;
		case HAVE:
			break;
		case BITFIELD:
			if (host.hasFile()) {
				BitField bitfield = BitField.getInstance();
				messageLength = bitfield.getMessageLength();
			} else
				return null;
			break;
		case REQUEST:
			break;
		case PIECE:
			break;
		}
		return messageLength;
	}

	public byte[] getPayload(Type messageType) {
		byte[] payload = null;

		switch (messageType) {
		case CHOKE:
			return new byte[] { 0 };
		case UNCHOKE:
			return new byte[] { 1 };
		case INTERESTED:
			return new byte[] { 2 };
		case NOTINTERESTED:
			return new byte[] { 3 };
		case HAVE:
			break;
		case BITFIELD:
			BitField bitfield = BitField.getInstance();
			payload = bitfield.getPayload();
			break;
		case REQUEST:
			break;
		case PIECE:
			break;
		}
		return payload;
	}
}
