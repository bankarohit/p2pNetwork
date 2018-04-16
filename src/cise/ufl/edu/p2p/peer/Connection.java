package cise.ufl.edu.p2p.peer;

import java.net.Socket;

import cise.ufl.edu.p2p.messages.Message.Type;

public class Connection {
	Upload upload;
	Download download;
	SharedData sharedData;
	double speed;
	Socket peerSocket;
	String remotePeerId;
	boolean choked;

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
	}

	public void createThreads(Upload upload, Download download) {
		Thread uploadThread = new Thread(upload);
		Thread downloadThread = new Thread(download);
		uploadThread.start();
		downloadThread.start();
	}

	public void sendMessage(Type messageType) {
		upload.sendMessage(messageType);
	}
}
