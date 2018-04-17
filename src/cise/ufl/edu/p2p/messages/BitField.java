package cise.ufl.edu.p2p.messages;

import java.nio.ByteBuffer;

import cise.ufl.edu.p2p.peer.FileHandler;

public class BitField extends Message {

	private static BitField bitfield = new BitField();

	private BitField() {
		content = FileHandler.getFilePieces().toByteArray();
		type = 5;
		bytebuffer = ByteBuffer.allocate(4);
		bytebuffer.putInt(0, content.length + 1);
		bytebuffer.get(messageLength, 0, 4);
		bytebuffer = ByteBuffer.wrap(content);
		payload = new byte[content.length + 1];
		payload[0] = type;
		bytebuffer.get(payload, 1, content.length);
	}

	public static BitField getInstance() {
		return bitfield;
	}

	@Override
	protected byte[] getMessageLength() {
		return messageLength;
	}

	@Override
	protected byte[] getPayload() {
		return payload;
	}

}
