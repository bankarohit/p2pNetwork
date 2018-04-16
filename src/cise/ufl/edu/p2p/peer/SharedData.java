package cise.ufl.edu.p2p.peer;

import java.util.BitSet;

public class SharedData {
	private volatile boolean bitfieldSent;
	private BitSet peerBitset;
	private String remotePeerId;
	private Connection conn;
	

	public SharedData(Connection connection) {
		conn = connection;
	}
	
	public void addConnection(String peerId) {
		remotePeerId = peerId;
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		connectionManager.addConnection(peerId, conn);
	}

	public synchronized String getRemotePeerId() {
		return remotePeerId;
	}

	public synchronized void setRemotePeerId(String remotePeerId) {
		this.remotePeerId = remotePeerId;
	}

	private volatile boolean uploadHandshake;
	private volatile boolean downloadHandshake;
	
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

	public void setPeerBitset(BitSet peerBitset) {
		this.peerBitset = peerBitset;
	}

	public synchronized void updatePeerBitset(int index) {
		peerBitset.set(index);
	}

	public synchronized boolean peerHasPiece(int index) {
		return peerBitset.get(index);
	}

}
