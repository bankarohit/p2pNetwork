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
	private volatile boolean downloadHandshake;
	private MessageManager messageManager = MessageManager.getInstance();
	private Peer host = Peer.getInstance();

	public SharedData(Connection connection) {
		conn = connection;
	}

	public synchronized void setUploadClientHandshake() {
		uploadHandshake = true;
		sendMessage(Message.Type.HANDSHAKE, null);
	}

	public synchronized void setUploadHandshake() {
		uploadHandshake = true;
	}

	public synchronized boolean getUploadHandshake() {
		return uploadHandshake;
	}

	public synchronized void setDownloadHandshake() {
		downloadHandshake = true;
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
		peerBitset.andNot(FileHandler.getFilePieces());
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

	protected void processPayload(byte[] payload) {
		Message.Type messageType = null;
		ByteBuffer content = null;
		Message.Type responseMessageType = null;
		if (!getDownloadHandshake()) {
			messageType = Message.Type.HANDSHAKE;
			setDownloadHandshake();
		} else {
			messageType = messageManager.getType(payload[0]);
			if (payload.length > 1) {
				content = messageManager.getContent(payload);
			}
		}
		System.out.println("Received Message: " + messageType);
		switch (messageType) {
		case HANDSHAKE:
			if (!getUploadHandshake()) {
				responseMessageType = Message.Type.HANDSHAKE;
				setUploadHandshake();
			} else {
				conn.receiveMessage();
			}
			break;
		case CHOKE:
			// conn.choke();
			break;
		case UNCHOKE:
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
			peerHasPiece(content.getInt());
			responseMessageType = getInterestedNotInterested();
			break;
		case BITFIELD:
			setPeerBitset((BitSet.valueOf(content)));
			responseMessageType = getInterestedNotInterested();
			break;
		case REQUEST:
			responseMessageType = Message.Type.PIECE;

			break;
		case PIECE:
			// conn.tellAllNeighbors(content);
			responseMessageType = getInterestedNotInterested();
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
	private void sendMessage(Message.Type messageType, ByteBuffer byteBuffer) {
		byte[] messageLength = null;
		byte[] payload = null;
		ByteBuffer data = null;
		switch (messageType) {
		case HANDSHAKE:
			byte[] handshake = Handshake.getMessage();
			conn.sendMessage(Arrays.copyOfRange(handshake, 0, 4), Arrays.copyOfRange(handshake, 4, 32));
			if (downloadHandshake) {
				sendMessage(Message.Type.BITFIELD, null);
				conn.receiveMessage();
			}
			return;
		case BITFIELD:
			if (!host.hasFile()) {
				System.out.println("Don't have bitfield waiting to receive message.. ");
				conn.receiveMessage();
				return;
			}
			break;
		case REQUEST:
			int requestPiece = getRequestPieceIndex();
			data = ByteBuffer.allocate(4).putInt(requestPiece);
			break;
		case PIECE:
			data = byteBuffer;
			break;
		case HAVE:
			break;
		case NOTINTERESTED:
			// choke download of sender
			conn.chokeDownload();
			break;
		}
		System.out.println("Send message: " + messageType);
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

	public synchronized boolean getDownloadHandshake() {
		return downloadHandshake;
	}

}
