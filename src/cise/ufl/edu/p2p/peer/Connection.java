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
	Socket peerSocket;
	String remotePeerId;
	
	public Connection(Socket peerSocket) {
		sharedData = new SharedData(this);
		Upload upload = new Upload(peerSocket, sharedData);
		Download download = new Download(peerSocket, sharedData);
		Thread uploadThread = new Thread(upload);
		Thread downloadThread = new Thread(download);
		uploadThread.start();
		downloadThread.start();
	}
	
	public Connection(Socket peerSocket, String peerId) {
		sharedData = new SharedData(this);
		Upload upload = new Upload(peerSocket, peerId, sharedData);
		Download download = new Download(peerSocket, peerId, sharedData);
		Thread uploadThread = new Thread(upload);
		Thread downloadThread = new Thread(download);
		uploadThread.start();
		downloadThread.start();
	}
}
