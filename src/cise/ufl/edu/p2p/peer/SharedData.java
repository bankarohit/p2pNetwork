package cise.ufl.edu.p2p.peer;

import java.nio.ByteBuffer;
import java.util.BitSet;

import cise.ufl.edu.p2p.messages.Message;

public class SharedData {
	private volatile boolean bitfieldSent;
	private BitSet peerBitset;
	private String remotePeerId;
	private Connection conn;
	private volatile boolean uploadHandshake;
	private volatile boolean downloadHandshake;

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

	public void setPeerBitset(byte[] payload) {
		this.peerBitset = BitSet.valueOf(ByteBuffer.wrap(payload, 1, payload.length - 1));
	}

	public synchronized void updatePeerBitset(int index) {
		peerBitset.set(index);
	}

	public synchronized boolean peerHasPiece(int index) {
		return peerBitset.get(index);
	}

	public void sendInterestedNotinterested() {
		Message.Type messageType = null;
		if (peerBitset.equals(FileHandler.getFilePieces())) {
			messageType = Message.Type.NOTINTERESTED;
		}
		messageType = Message.Type.INTERESTED;
		conn.sendMessage(messageType);
	}

	public void sendBitfieldMessage() {
		conn.sendMessage(Message.Type.BITFIELD);
	}

}
