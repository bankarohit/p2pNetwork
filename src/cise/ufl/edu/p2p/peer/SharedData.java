package cise.ufl.edu.p2p.peer;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;

import cise.ufl.edu.p2p.messages.Handshake;
import cise.ufl.edu.p2p.messages.Message;
import cise.ufl.edu.p2p.messages.MessageManager;

public class SharedData {
	private volatile boolean bitfieldSent;
	private BitSet peerBitset;
	private String remotePeerId;
	private Connection conn;
	private volatile boolean uploadHandshake;
	private MessageManager messageManager = MessageManager.getInstance();
	private Peer host = Peer.getInstance();

	public SharedData(Connection connection) {
		conn = connection;
	}

	public synchronized void sendHandshake() {
		setUploadHandshake();
		sendMessage(Message.Type.HANDSHAKE, null);
	}

	public synchronized void setUploadHandshake() {
		uploadHandshake = true;
	}

	public synchronized boolean getUploadHandshake() {
		return uploadHandshake;
	}

	public void updatePeerId(String peerId) {
		remotePeerId = peerId;
	}

	public synchronized String getRemotePeerId() {
		return remotePeerId;
	}

	public synchronized void setRemotePeerId(String remotePeerId) {
		this.remotePeerId = remotePeerId;
	}

	private int getRandomPiece() {
		int pieceIndex = 0;
		int numberOfPieces = CommonProperties.getNumberOfPieces();
		do {
			pieceIndex = (int) (Math.random() * numberOfPieces);
		} while (!peerHasPiece(pieceIndex) && !conn.isRequested(pieceIndex));
		return pieceIndex;
	}

	public boolean isBitfieldSent() {
		return bitfieldSent;
	}

	public synchronized void setBitfieldSent() {
		bitfieldSent = true;
	}

	public void setPeerBitset(byte[] payload) {
		peerBitset = new BitSet(payload.length - 1);
		for (int i = 1; i < payload.length; i++) {
			// System.out.print(payload[i]);
			if (payload[i] == 1) {
				peerBitset.set(i - 1);
			}
		}
		for (int i = 1; i < peerBitset.length(); i++) {
			System.out.print(peerBitset.get(i) ? 1 : 0 + " ");
		}
	}

	public synchronized void updatePeerBitset(int index) {
		peerBitset.set(index);
	}

	public synchronized boolean peerHasPiece(int index) {
		return peerBitset.get(index);
	}

	private boolean isInterested() {
		peerBitset.andNot(SharedFile.getFilePieces());
		return peerBitset.cardinality() > 0;
	}

	private Message.Type getInterestedNotInterested() {
		Message.Type messageType = null;
		messageType = Message.Type.NOTINTERESTED;
		if (isInterested()) {
			messageType = Message.Type.INTERESTED;
		}
		return messageType;
	}

	protected void processHandshake(byte[] handshake) {
		remotePeerId = Handshake.getId(handshake);
		conn.setPeerId(remotePeerId);
		if (!getUploadHandshake()) {
			System.out.println("Received handshake from: " + remotePeerId);
			sendMessage(Message.Type.HANDSHAKE, null);
		}
		sendMessage(Message.Type.BITFIELD, null);
	}

	protected void processPayload(byte[] payload) {
		Message.Type messageType = null;
		byte[] content = null;
		Message.Type responseMessageType = null;
		messageType = messageManager.getType(payload[0]);
		System.out.println("Received Message: " + messageType + " from " + remotePeerId);
		switch (messageType) {
		case CHOKE:
			// conn.choke();
			break;
		case UNCHOKE:
			// System.out.println("Received unchoke");
			responseMessageType = Message.Type.REQUEST;
			break;
		case INTERESTED:
			conn.addInterestedConnection();
			messageType = null;
			break;
		case NOTINTERESTED:
			conn.addNotInterestedConnection();
			messageType = null;
			break;
		case HAVE:
			peerHasPiece(ByteBuffer.wrap(content).getInt());
			responseMessageType = getInterestedNotInterested();
			break;
		case BITFIELD:
			setPeerBitset(payload);
			responseMessageType = getInterestedNotInterested();
			break;
		case REQUEST:
			responseMessageType = Message.Type.PIECE;
			content = new byte[4];
			System.arraycopy(payload, 1, content, 0, 4);
			break;
		case PIECE:
			System.out.println("Received pieceindex & setting: " + ByteBuffer.wrap(payload, 1, 4).getInt());
			SharedFile.setPiece(Arrays.copyOfRange(payload, 1, payload.length));
			responseMessageType = Message.Type.REQUEST;
			break;
		default:
			System.out.println("Received hanshake in error");
			break;
		}
		if (responseMessageType != null)
			sendMessage(responseMessageType, content);
	}

	/*
	 * CHOKE, UNCHOKE, INTERESTED, NOTINTERESTED, BITFIELD, HANDSHAKE message length
	 * & payload always fixed. Call to message manager will retrieve appropriate
	 * data
	 */
	private void sendMessage(Message.Type messageType, byte[] buffer) {
		int messageLength = Integer.MIN_VALUE;
		byte[] payload = null;
		int pieceIndex = Integer.MIN_VALUE;
		switch (messageType) {
		case HANDSHAKE:
			byte[] handshake = Handshake.getMessage();
			conn.sendMessage(32, Arrays.copyOfRange(handshake, 4, 32));
			return;
		case BITFIELD:
			if (!host.hasFile()) {
				System.out.println("Don't have bitfield waiting to receive message.. ");
				return;
			}
			break;
		case REQUEST:
			// assume getRequestPieceIndex() works correctly
			pieceIndex = getRequestPieceIndex();
			conn.setRequested(pieceIndex);
			System.out.println("Requested piece: " + pieceIndex);
			break;
		case PIECE:
			pieceIndex = ByteBuffer.wrap(buffer).getInt();
			System.out.println("Received request for piece " + pieceIndex);
			break;
		case HAVE:
			break;
		case NOTINTERESTED:
			// choke download of sender
			conn.chokeDownload();
			break;
		}
		messageLength = messageManager.getMessageLength(messageType, pieceIndex);
		payload = messageManager.getMessagePayload(messageType, pieceIndex);
		System.out.println("Sending message " + messageType + " of length " + messageLength + " to " + remotePeerId);
		conn.sendMessage(messageLength, payload);
	}

	private int getRequestPieceIndex() {
		int requestPiece = 0;
		do {
			requestPiece = getRandomPiece();
		} while (SharedFile.isPieceAvailable(requestPiece));
		return requestPiece;
	}

}
