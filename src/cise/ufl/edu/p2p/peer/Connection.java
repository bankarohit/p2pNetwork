package cise.ufl.edu.p2p.peer;
import java.net.Socket;

import cise.ufl.edu.p2p.peer.Download;
import cise.ufl.edu.p2p.peer.SharedData;
import cise.ufl.edu.p2p.peer.Upload;

public class Connection {
	Upload upload;
	Download download;
	SharedData sharedData;
	double speed;
	
	public double getSpeed() {
		return speed;
	}

	
	Socket peerSocket;
	String remotePeerId;
	boolean choked;
	
	public boolean isChoked() {
		return choked;
	}

	public void setChoked(boolean choked) {
		this.choked = choked;
		if(choked) {
			upload.choke();
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
}
