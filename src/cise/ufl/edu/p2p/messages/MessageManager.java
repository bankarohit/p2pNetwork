package cise.ufl.edu.p2p.messages;

import java.nio.ByteBuffer;

import cise.ufl.edu.p2p.messages.Message.Type;

public class MessageManager {
	private static MessageManager messageManager = new MessageManager();

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
			break;
		case UNCHOKE:
			break;
		case INTERESTED:
			break;
		case NOTINTERESTED:
			break;
		case HAVE:
			break;
		case BITFIELD:
			BitField bitfield = BitField.getInstance();
			messageLength = bitfield.getMessageLength();
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
			break;
		case UNCHOKE:
			break;
		case INTERESTED:
			break;
		case NOTINTERESTED:
			break;
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
