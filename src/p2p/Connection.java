package p2p;

import java.net.Socket;
import java.util.BitSet;

public class Connection {
	Upload upload;
	Download download;
	SharedData sharedData;
	double bytesDownloaded;
	Socket peerSocket;
	String remotePeerId;
	boolean choked;
	private ConnectionManager connectionManager = ConnectionManager.getInstance();

	public double getBytesDownloaded() {
		return bytesDownloaded;
	}

	protected Upload getUpload() {
		return upload;
	}

	public void addBytesDownloaded(long value) {
		bytesDownloaded += value;
	}

	public boolean isChoked() {
		return choked;
	}

	public Connection(Socket peerSocket) {
		sharedData = new SharedData(this);
		upload = new Upload(peerSocket, sharedData);
		download = new Download(peerSocket, sharedData);
		createThreads(upload, download);
		sharedData.setUpload(upload);
		sharedData.start();
	}

	public Connection(Socket peerSocket, String peerId) {
		sharedData = new SharedData(this);
		upload = new Upload(peerSocket, peerId, sharedData);
		download = new Download(peerSocket, peerId, sharedData);
		createThreads(upload, download);
		LoggerUtil.getInstance().logTcpConnectionTo(Peer.getInstance().getNetwork().getPeerId(), peerId);
		sharedData.sendHandshake();
		sharedData.setUpload(upload);
		sharedData.start();
	}

	public void createThreads(Upload upload, Download download) {
		Thread uploadThread = new Thread(upload);
		Thread downloadThread = new Thread(download);
		uploadThread.start();
		downloadThread.start();
	}

	public synchronized void sendMessage(int messageLength, byte[] payload) {
		upload.addMessage(messageLength, payload);
	}

	public synchronized String getRemotePeerId() {
		return remotePeerId;
	}

	public void tellAllNeighbors(int pieceIndex) {
		connectionManager.tellAllNeighbors(pieceIndex);
	}

	protected boolean isRequested(int pieceIndex) {
		return connectionManager.isRequested(pieceIndex);
	}

	protected void addRequestedPiece(int pieceIndex) {
		connectionManager.addRequestedPiece(this, pieceIndex);
	}

	public void addInterestedConnection() {
		connectionManager.addInterestedConnection(remotePeerId, this);
	}

	public void addNotInterestedConnection() {
		connectionManager.addNotInterestedConnection(remotePeerId, this);
	}

	public void receiveMessage() {
		download.receiveMessage();
	}

	public void setPeerId(String value) {
		remotePeerId = value;

	}

	public void removeRequestedPiece() {
		connectionManager.removeRequestedPiece(this);
	}

	public BitSet getPeerBitSet() {
		// TODO Auto-generated method stub
		return sharedData.getPeerBitSet();
	}
}
