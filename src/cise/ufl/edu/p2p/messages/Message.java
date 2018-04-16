package cise.ufl.edu.p2p.messages;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

import cise.ufl.edu.p2p.peer.FileHandler;

public abstract class Message {

	protected byte[] messageLength = null;
	protected byte type = 0;
	protected byte[] payload = null;

	public static enum Type {
		CHOKE, UNCHOKE, INTERESTED, NOTINTERESTED, HAVE, BITFIELD, REQUEST, PIECE;
	}

	public static int getLength(byte[] metadata) {
		return new BigInteger(Arrays.copyOfRange(metadata, 0, 4)).intValue();
	}

	public static Type getType(byte[] metadata) {
		Type messageType = null;
		int type = new BigInteger(Arrays.copyOfRange(metadata, 4, metadata.length)).intValue();
		switch (type) {
		case 0:
			messageType = Type.CHOKE;
			break;
		case 1:
			messageType = Type.UNCHOKE;
			break;
		case 2:
			messageType = Type.INTERESTED;
			break;
		case 3:
			messageType = Type.NOTINTERESTED;
			break;
		case 4:
			messageType = Type.HAVE;
			break;
		case 5:
			messageType = Type.BITFIELD;
			break;
		case 6:
			messageType = Type.REQUEST;
			break;
		case 7:
			messageType = Type.PIECE;
			break;
		}
		return messageType;
	}

	public static byte[] getMessage(Type messageType) {
		byte[] message = null;
		byte[] messageLength = null;
		byte type = 0;
		int totalLength = 0;
		switch (messageType) {
		case CHOKE:
			break;
		case UNCHOKE:
			break;
		case INTERESTED:
			messageLength = ByteBuffer.allocate(4).putInt(1).array();
			type = (byte) 2;
			message = new byte[5];
			System.arraycopy(messageLength, 0, message, 0, messageLength.length);
			message[4] = type;
			break;
		case NOTINTERESTED:
			break;
		case HAVE:
			break;
		case BITFIELD:
			if (FileHandler.isBitSetEmpty()) {
				message = null;
			} else {
				byte[] bitfield = FileHandler.getFilePieces().toByteArray();
				totalLength = bitfield.length + 5;
				messageLength = ByteBuffer.allocate(4).putInt(1).array();
				type = (byte) 5;
				message = new byte[totalLength];
				System.arraycopy(messageLength, 0, message, 0, messageLength.length);
				message[4] = type;
				System.arraycopy(bitfield, 0, message, 5, bitfield.length);
			}
			break;
		case REQUEST:
			break;
		case PIECE:
			break;
		}
		return message;
	}
}
