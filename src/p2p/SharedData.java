package p2p;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.concurrent.LinkedBlockingQueue;

public class SharedData extends Thread {
	private volatile boolean bitfieldSent;
	private BitSet peerBitset;
	private String remotePeerId;
	private Connection conn;
	private volatile boolean uploadHandshake;
	private volatile boolean isHandshakeDownloaded;
	private SharedFile sharedFile;

	public boolean isHandshakeDownloaded() {
		return isHandshakeDownloaded;
	}

	public void setHandshakeDownloaded() {
		isHandshakeDownloaded = true;
	}

	private MessageManager messageManager = MessageManager.getInstance();
	private Peer host = Peer.getInstance();
	int i = 0;
	private LinkedBlockingQueue<byte[]> payloadQueue;
	private boolean isAlive;
	Upload upload;

	public void addPayloadQueueMessage(byte[] payload) {
		payloadQueue.offer(payload);
	}
	// private static SharedData sharedData;
	//
	// static {
	// sharedData = new SharedData();
	// }

	public SharedData(Connection connection) {
		conn = connection;
		payloadQueue = new LinkedBlockingQueue<>();
		isAlive = true;
		sharedFile = SharedFile.getInstance();
	}

	public void setUpload(Upload value) {
		upload = value;
		if (getUploadHandshake()) {
			// System.out.println("Sending handshake: " + ++i);
			buildMessage(Message.Type.HANDSHAKE, null);
			// System.out.println("handshake sent");
		}
	}

	@Override
	public void run() {
		while (isAlive) {
			try {
				byte[] p = payloadQueue.take();
				processPayload(p);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void addPayload(byte[] payload) {
		try {
			payloadQueue.put(payload);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized BitSet getPeerBitSet() {
		return peerBitset;
	}

	public synchronized void sendHandshake() {
		setUploadHandshake();
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

	private synchronized boolean isInterested() {
		for (int i = 0; i < CommonProperties.getNumberOfPieces(); i++) {
			if (peerBitset.get(i) && !sharedFile.isPieceAvailable(i)) {
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

	protected void processPayload(byte[] payload) {
		Message.Type messageType = null;
		byte[] content = null;
		Message.Type responseMessageType = null;
		if (!isHandshakeDownloaded()) {
			messageType = Message.Type.HANDSHAKE;
			setHandshakeDownloaded();
		} else {
			messageType = messageManager.getType(payload[0]);
		}
		if (messageType != Message.Type.HANDSHAKE)
			System.out.println("Received Message: " + messageType + " from " + remotePeerId);
		switch (messageType) {
		case CHOKE:
			// clear requested pieces of this connection
			LoggerUtil.getInstance().logChokingNeighbor(getTime(), peerProcessMain.getId(), conn.getRemotePeerId());
			conn.removeRequestedPiece();
			responseMessageType = null;
			break;
		case UNCHOKE:
			// respond with request
			LoggerUtil.getInstance().logUnchokingNeighbor(getTime(), peerProcessMain.getId(), conn.getRemotePeerId());
			responseMessageType = Message.Type.REQUEST;
			break;
		case INTERESTED:
			// add to interested connections
			LoggerUtil.getInstance().logReceivedInterestedMessage(getTime(), peerProcessMain.getId(),
					conn.getRemotePeerId());
			conn.addInterestedConnection();
			responseMessageType = null;
			break;
		case NOTINTERESTED:
			// add to not interested connections
			LoggerUtil.getInstance().logReceivedNotInterestedMessage(getTime(), peerProcessMain.getId(),
					conn.getRemotePeerId());
			conn.addNotInterestedConnection();
			responseMessageType = null;
			break;
		case HAVE:
			// update peer bitset
			// send interested/not interested
			int pieceIndex = ByteBuffer.wrap(payload, 1, 4).getInt();
			LoggerUtil.getInstance().logReceivedHaveMessage(getTime(), peerProcessMain.getId(), conn.getRemotePeerId(),
					pieceIndex);
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
			// send have to all neighbors & notinterested to neighbors with same bitset
			// respond with request
			// update bytesDownloaded
			// pi = pieceIndex
			int pi = ByteBuffer.wrap(payload, 1, 4).getInt();
			conn.addBytesDownloaded(payload.length);
			// System.out.println("Received pieceindex & setting: " + pi);
			sharedFile.setPiece(Arrays.copyOfRange(payload, 1, payload.length));
			LoggerUtil.getInstance().logDownloadedPiece(getTime(), peerProcessMain.getId(), conn.getRemotePeerId(), pi,
					sharedFile.getReceivedFileSize());
			responseMessageType = Message.Type.REQUEST;
			conn.tellAllNeighbors(pi);
			break;
		case HANDSHAKE:
			remotePeerId = Handshake.getId(payload);
			conn.setPeerId(remotePeerId);
			System.out.println("Received Message: " + messageType + " from " + remotePeerId);
			if (!getUploadHandshake()) {
				setUploadHandshake();
				LoggerUtil.getInstance().logTcpConnectionFrom(host.getNetwork().getPeerId(), remotePeerId);
				// System.out.println("Received handshake from: " + remotePeerId);
				responseMessageType = Message.Type.HANDSHAKE;
				buildMessage(Message.Type.HANDSHAKE, null);
			}
			if (host.hasFile()) {
				responseMessageType = Message.Type.BITFIELD;
			} else {
				responseMessageType = null;
			}
			break;
		}
		if (responseMessageType != null)
			buildMessage(responseMessageType, content);
	}

	/*
	 * CHOKE, UNCHOKE, INTERESTED, NOTINTERESTED, BITFIELD, HANDSHAKE message length
	 * & payload always fixed. Call to message manager will retrieve appropriate
	 * data
	 */
	private void buildMessage(Message.Type messageType, byte[] buffer) {
		int pieceIndex = Integer.MIN_VALUE;
		switch (messageType) {
		case HANDSHAKE:
			break;
		case BITFIELD:
			break;
		case REQUEST:
			// TODO: Send "close" message when pieceIndex = Integer.min_value
			// add to requested pieces
			pieceIndex = getRequestPieceIndex();
			if (pieceIndex == Integer.MIN_VALUE) {
				System.out.println("received file");
				conn.close();
				// System.exit(0);
			}
			// System.out.println("Requested piece: " + pieceIndex);
			break;
		case PIECE:
			// get piece index from buffer
			pieceIndex = ByteBuffer.wrap(buffer).getInt();
			if (pieceIndex == Integer.MIN_VALUE) {
				LoggerUtil.getInstance().logFinishedDownloading(getTime(), peerProcessMain.getId());
				messageType = null;
				isAlive = false;
				conn.close();
			}
			break;
		// CHOKE, UNCHOKE, HAVE, NOTINTERESTED types of messages will only be sent by
		// connection manager
		default:
			// System.out.println("Tying to send an incorrect message type");
		}
		if (messageType != null)
			sendMessage(messageType, pieceIndex);
	}

	protected void sendMessage(Message.Type messageType, int pieceIndex) {
		int messageLength = Integer.MIN_VALUE;
		byte[] payload = null;
		messageLength = messageManager.getMessageLength(messageType, pieceIndex);
		payload = messageManager.getMessagePayload(messageType, pieceIndex);
		upload.addMessage(messageLength, payload);
		// System.out.println("Sending message " + messageType + " of length " +
		// messageLength + " to " + remotePeerId);
	}

	// return piece index which has not been requested yet, peer has & i don't have
	// if file is complete return min value
	private int getRequestPieceIndex() {
		int requestPieceIndex = 0;
		int numberOfPieces = CommonProperties.getNumberOfPieces();
		do {
			if (sharedFile.isCompleteFile()) {
				System.out.println("File received");
				return Integer.MIN_VALUE;
			}
			requestPieceIndex = (int) (Math.random() * numberOfPieces);
		} while (!peerHasPiece(requestPieceIndex) && !conn.isRequested(requestPieceIndex)
				&& sharedFile.isPieceAvailable(requestPieceIndex));
		conn.addRequestedPiece(requestPieceIndex);
		return requestPieceIndex;
	}

	public String getTime() {
		return Calendar.getInstance().getTime() + ": ";
	}
}
