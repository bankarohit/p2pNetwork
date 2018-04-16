package cise.ufl.edu.p2p.messages;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;


public class Handshake {
	private static final String HANDSHAKE_HEADER = "P2PFILESHARINGPROJ0000000000";
	private static String message = "";

	private static void init(String id) {
		message += HANDSHAKE_HEADER + id;
	}

	public static String getMessage()  {
		return message;
	}

	public static void setId(String id) {
		init(id);
	}

	public static boolean verify(byte[] message, String peerId) {
		String recvdMessage = new String(message);
		return recvdMessage.indexOf(peerId) != -1 && recvdMessage.contains(HANDSHAKE_HEADER);
	}

	public static String getId(byte[] message) {
		byte[] remotePeerId = Arrays.copyOfRange(message, message.length - 4, message.length);
		return new String(remotePeerId);
	}
}
