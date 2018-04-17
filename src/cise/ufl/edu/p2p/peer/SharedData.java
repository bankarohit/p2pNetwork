package cise.ufl.edu.p2p.peer;

import java.nio.ByteBuffer;
import java.util.BitSet;

import cise.ufl.edu.p2p.messages.Message;
import cise.ufl.edu.p2p.messages.MessageManager;

public class SharedData {
	private volatile boolean bitfieldSent;
	private BitSet peerBitset;
	private String remotePeerId;
	private Connection conn;
	private volatile boolean uploadHandshake;
	private volatile boolean downloadHandshake;
	private MessageManager messageManager = MessageManager.getInstance();

	public SharedData(Connection connection) {
		conn = connection;
	}

	public void addConnection(String peerId) {
		remotePeerId = peerId;
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		connectionManager.addConnection(conn);
	}

	public synchronized String getRemotePeerId() {
		return remotePeerId;
	}

	public synchronized void setRemotePeerId(String remotePeerId) {
		this.remotePeerId = remotePeerId;
	}

	public synchronized boolean getDownloadHandshake() {
		return downloadHandshake;
	}

	public synchronized void setDownloadHandshake(boolean downloadHandshake) {
		this.downloadHandshake = downloadHandshake;
	}

	private int getRandomPiece() {
		int pieceIndex = 0;
		int numberOfPieces = CommonProperties.getNumberOfPieces();
		do {
			pieceIndex = (int) (Math.random() * numberOfPieces);
		} while (!peerHasPiece(pieceIndex));
		return pieceIndex;
	}

	public synchronized boolean getUploadHandshake() {
		return uploadHandshake;
	}

	public synchronized void setUploadHandshake(boolean value) {
		this.uploadHandshake = value;
	}

	public boolean isBitfieldSent() {
		return bitfieldSent;
	}

	public synchronized void setBitfieldSent() {
		bitfieldSent = true;
	}

	public void setPeerBitset(BitSet bitset) {
		this.peerBitset = bitset;
	}

	public synchronized void updatePeerBitset(int index) {
		peerBitset.set(index);
	}

	public synchronized boolean peerHasPiece(int index) {
		return peerBitset.get(index);
	}

	private boolean isInterested() {
		return !peerBitset.equals(FileHandler.getFilePieces());
	}

	private Message.Type getInterestedNotInterested() {
		Message.Type messageType = null;
		messageType = Message.Type.NOTINTERESTED;
		if (isInterested()) {
			messageType = Message.Type.INTERESTED;
		}
		return messageType;
	}

	protected void sendBitfieldMessage() {
		sendMessage(Message.Type.BITFIELD);
	}

	protected void processPayload(byte[] payload) {
		ByteBuffer content = null;
		Message.Type responseMessageType = null;
		Message.Type messageType = messageManager.getType(payload[0]);
		System.out.println("Received message of type: " + messageType);
		if (payload.length > 1) {
			content = messageManager.getContent(payload);
		}
		switch (messageType) {
		case CHOKE:
			break;
		case UNCHOKE:
			responseMessageType = Message.Type.REQUEST;
			break;
		case INTERESTED:
			System.exit(0);
			break;
		case NOTINTERESTED:
			break;
		case HAVE:
			peerHasPiece(content.getInt());
			responseMessageType = getInterestedNotInterested();
			break;
		case BITFIELD:
			setPeerBitset((BitSet.valueOf(content)));
			responseMessageType = getInterestedNotInterested();
			break;
		case REQUEST:
			break;
		case PIECE:
			responseMessageType = getInterestedNotInterested();
			break;
		}
		sendMessage(responseMessageType);
	}

	/*
	 * CHOKE, UNCHOKE, INTERESTED, NOTINTERESTED, BITFIELD message length & payload
	 * always fixed. Call to message manager will retrieve appropriate data
	 */
	private void sendMessage(Message.Type messageType) {
		byte[] messageLength = null;
		byte[] payload = null;
		ByteBuffer data = null;
		switch (messageType) {
		case REQUEST:
			int requestPiece = getRequestPieceIndex();
			data = ByteBuffer.allocate(4).putInt(requestPiece);
			break;
		case PIECE:
			break;
		case HAVE:
			break;
		default:
			// System.out.println("Received message of type: " + messageType);
			break;
		}
		messageLength = messageManager.getMessageLength(messageType, data);
		payload = messageManager.getPayload(messageType, data);
		conn.sendMessage(messageLength, payload);
	}

	private int getRequestPieceIndex() {
		int requestPiece = 0;
		do {
			requestPiece = getRandomPiece();
		} while (FileHandler.isPieceAvailable(requestPiece));
		return requestPiece;
	}

	protected int processMessageLength(byte[] messageLength) {
		return messageManager.getLength(messageLength);
	}
	//
	// protected byte[] getMessageLength(Message.Type messageType) {
	// return messageManager.getMessageLength(messageType);
	// }
	//
	// protected byte[] getPayload(Message.Type messageType) {
	// return messageManager.getPayload(messageType);
	// }

}
