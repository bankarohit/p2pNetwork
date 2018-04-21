package p2p;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;

public class SharedData {
	private volatile boolean bitfieldSent;
	private BitSet peerBitset;
	private String remotePeerId;
	private Connection conn;
	private volatile boolean uploadHandshake;
	private MessageManager messageManager = MessageManager.getInstance();
	private Peer host = Peer.getInstance();
	int i = 0;

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

	// private int getRandomPiece() {
	// int pieceIndex = 0;
	// int numberOfPieces = CommonProperties.getNumberOfPieces();
	// do {
	// pieceIndex = (int) (Math.random() * numberOfPieces);
	// } while (!peerHasPiece(pieceIndex) && !conn.isRequested(pieceIndex));
	// return pieceIndex;
	// }

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
		for (int i = 0; i < CommonProperties.getNumberOfPieces(); i++) {
			if (peerBitset.get(i) && !SharedFile.isPieceAvailable(i)) {
				return true;
			}
		}
		return false;
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
			LoggerUtil.getInstance().logTcpConnectionFrom(host.getNetwork().getPeerId(), remotePeerId);
			System.out.println("Received handshake from: " + remotePeerId);
			sendMessage(Message.Type.HANDSHAKE, null);
		}
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			// clear requested pieces of this connection
			break;
		case UNCHOKE:
			// respond with request
			responseMessageType = Message.Type.REQUEST;
			break;
		case INTERESTED:
			// add to interested connections
			conn.addInterestedConnection();
			messageType = null;
			break;
		case NOTINTERESTED:
			// add to not interested connections
			conn.addNotInterestedConnection();
			messageType = null;
			break;
		case HAVE:
			// update peer bitset
			// send interested/not interested
			int pieceIndex = ByteBuffer.wrap(payload, 1, 4).getInt();
			updatePeerBitset(pieceIndex);
			responseMessageType = getInterestedNotInterested();
			break;
		case BITFIELD:
			// update peer bitset
			// send interested/not interested
			setPeerBitset(payload);
			responseMessageType = getInterestedNotInterested();
			break;
		case REQUEST:
			// send requested piece
			responseMessageType = Message.Type.PIECE;
			content = new byte[4];
			System.arraycopy(payload, 1, content, 0, 4);
			break;
		case PIECE:
			// update own bitset & file
			// send have to all neighbors except this one
			// respond with request
			// pi = pieceIndex
			int pi = ByteBuffer.wrap(payload, 1, 4).getInt();
			System.out.println("Received pieceindex & setting: " + pi);
			SharedFile.setPiece(Arrays.copyOfRange(payload, 1, payload.length));
			responseMessageType = Message.Type.REQUEST;
			conn.tellAllNeighbors(pi);
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
			if (pieceIndex == Integer.MIN_VALUE) {
				SharedFile.writeToFile();
				System.exit(0);
			}
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
			// conn.chokeDownload();
			break;
		}
		messageLength = messageManager.getMessageLength(messageType, pieceIndex);
		payload = messageManager.getMessagePayload(messageType, pieceIndex);
		System.out.println("Sending message " + messageType + " of length " + messageLength + " to " + remotePeerId);
		conn.sendMessage(messageLength, payload);
	}

	private int getRequestPieceIndex() {
		int requestPieceIndex = 0;
		int numberOfPieces = CommonProperties.getNumberOfPieces();
		do {
			requestPieceIndex = (int) (Math.random() * numberOfPieces);
			if (SharedFile.getFileSize() == CommonProperties.getNumberOfPieces()) {
				requestPieceIndex = Integer.MIN_VALUE;
				break;
			}
		} while (!peerHasPiece(requestPieceIndex) && !conn.isRequested(requestPieceIndex)
				&& SharedFile.isPieceAvailable(requestPieceIndex));
		return requestPieceIndex;
	}

}
