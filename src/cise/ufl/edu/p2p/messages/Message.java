package cise.ufl.edu.p2p.messages;

import java.nio.ByteBuffer;

public abstract class Message {

	protected ByteBuffer bytebuffer;
	protected byte type;
	protected byte[] content;
	protected byte[] messageLength = new byte[4];
	protected byte[] payload;

	public static enum Type {
		CHOKE, UNCHOKE, INTERESTED, NOTINTERESTED, HAVE, BITFIELD, REQUEST, PIECE;
	}

	abstract protected byte[] getMessageLength();

	abstract protected byte[] getPayload();

}
