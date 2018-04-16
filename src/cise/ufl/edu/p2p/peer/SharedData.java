package cise.ufl.edu.p2p.peer;

import java.util.BitSet;

public class SharedData {
	private volatile boolean bitfieldSent;
	private BitSet peerBitset;
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
