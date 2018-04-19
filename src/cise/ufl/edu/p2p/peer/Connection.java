package cise.ufl.edu.p2p.peer;

import java.net.Socket;
import java.nio.ByteBuffer;

public class Connection {
	Upload upload;
	Download download;
	SharedData sharedData;
	double speed;
	Socket peerSocket;
	String remotePeerId;
	boolean choked;
	private ConnectionManager connectionManager = ConnectionManager.getInstance();

	public double getSpeed() {
		return speed;
	}

	public boolean isChoked() {
		return choked;
	}

	public void setChoked(boolean choked) {
		this.choked = choked;
		if (choked) {
			download.choke();
		}
	}

	public Connection(Socket peerSocket) {
		sharedData = new SharedData(this);
		upload = new Upload(peerSocket, sharedData);
		download = new Download(peerSocket, sharedData);
		createThreads(upload, download);
	}

	public Connection(Socket peerSocket, String peerId) {
		sharedData = new SharedData(this);
		upload = new Upload(peerSocket, peerId, sharedData);
		download = new Download(peerSocket, peerId, sharedData);
		createThreads(upload, download);
		sharedData.setUploadClientHandshake();
	}

	public void createThreads(Upload upload, Download download) {
		Thread uploadThread = new Thread(upload);
		Thread downloadThread = new Thread(download);
		uploadThread.start();
		downloadThread.start();
	}

	public void sendMessage(byte[] messageLength, byte[] payload) {
		synchronized (upload.lengthQueue) {
			upload.addMessageLength(messageLength);
			upload.lengthQueue.notify();
			// System.out.println("Added message length");
		}
		synchronized (upload.payloadQueue) {
			/// System.out.println("Added message payload");
			upload.addMessagePayload(payload);
			upload.payloadQueue.notify();
		}
	}

	public void tellAllNeighbors(ByteBuffer pieceIndex) {
		connectionManager.tellAllNeighbors(pieceIndex);
	}

	protected boolean isRequested(int pieceIndex) {
		return connectionManager.isRequested(pieceIndex);
	}

	public void addInterestedConnection() {
		connectionManager.addInterestedConnection(remotePeerId, this);
	}

	public void addNotInterestedConnection() {
		connectionManager.addNotInterestedConnection(remotePeerId, this);
	}

	public void chokeDownload() {
		download.choke();
	}

	public void chokeUpload() {
		upload.choke();
	}

	public void receiveMessage() {
		download.receiveMessage();

	}
}
