package cise.ufl.edu.p2p.messages;

import java.nio.ByteBuffer;

import cise.ufl.edu.p2p.messages.Message.Type;
import cise.ufl.edu.p2p.peer.FileHandler;

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

	public synchronized int processLength(byte[] messageLength) {
		ByteBuffer temp = ByteBuffer.wrap(messageLength);
		return temp.getInt();
	}

	public synchronized byte[] getMessageLength(Type messageType, ByteBuffer data) {
		byte[] messageLength = new byte[4];
		ByteBuffer bytebuffer = ByteBuffer.allocate(4);
		switch (messageType) {
		case CHOKE:
		case UNCHOKE:
		case INTERESTED:
		case NOTINTERESTED:
			bytebuffer.putInt(0, 1);
			break;
		case HAVE:
			bytebuffer.putInt(0, 5);
			break;
		case BITFIELD:
			BitField bitfield = BitField.getInstance();
			messageLength = bitfield.getMessageLength();
		case REQUEST:
			bytebuffer.putInt(0, 5);
			break;
		case PIECE:
			int pieceIndex = data.getInt();
			int payloadLength = 4 + FileHandler.getPiece(pieceIndex).length + 1;
			bytebuffer.putInt(0, payloadLength);
			break;
		}
		bytebuffer.get(messageLength, 0, 4);
		return messageLength;
	}

	public synchronized byte[] getMessagePayload(Type messageType, ByteBuffer data) {
		byte[] payload = new byte[5];

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
			payload[0] = 6;
			data.get(payload, 1, 4);
			break;
		case PIECE:
			int pieceIndex = data.getInt();
			byte[] piece = FileHandler.getPiece(pieceIndex);
			int pieceSize = piece.length;
			int totalLength = 1 + pieceSize + 4;
			payload = new byte[totalLength];
			payload[0] = 7;
			data = ByteBuffer.allocate(4).putInt(totalLength);
			data.get(payload, 1, 4);
			data = ByteBuffer.wrap(piece);
			data.get(payload, 5, pieceSize);
			break;
		}
		return payload;
	}

	public ByteBuffer getContent(byte[] payload) {
		return ByteBuffer.wrap(payload, 1, payload.length - 1);
	}
}
