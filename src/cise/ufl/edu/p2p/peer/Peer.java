package cise.ufl.edu.p2p.peer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import cise.ufl.edu.p2p.utils.LoadPeerList;

public class Peer {

	private static Peer host = new Peer();
	private NetworkInfo network;
	ConnectionManager connectionManager;

	private Peer() {
		network = LoadPeerList.getPeer(peerProcess.getId());
		connectionManager = ConnectionManager.getInstance();
	}

	public static Peer getInstance() {
		return host;
	}

	// TODO: Use filePieces of filehandler instead of network
	public boolean hasFile() {
		return network.hasSharedFile();
	}
	// TODO: Optimize by maintaining index upto which all files have been received

	public NetworkInfo getNetwork() {
		return network;
	}

	public void setNetwork(NetworkInfo network) {
		this.network = network;
	}

	public void listenForConnections() throws IOException {
		boolean allPeersReceivedFiles = false;
		ServerSocket socket;

		socket = new ServerSocket(network.getPort());
		// TODO: End connection when all peers have received files
		while (!allPeersReceivedFiles) {
			Socket peerSocket = socket.accept();
			connectionManager.createConnection(peerSocket);

		}
		socket.close();
	}

	public void createTCPConnections() {
		HashMap<String, NetworkInfo> map = LoadPeerList.getPeerList();
		int myNumber = network.getNumber();
		for (String peerId : map.keySet()) {
			NetworkInfo peerInfo = map.get(peerId);
			if (peerInfo.getNumber() < myNumber) {
				createConnection(peerInfo);
			}
		}
	}

	private void createConnection(NetworkInfo peerInfo) {
		int peerPort = peerInfo.getPort();
		String peerHost = peerInfo.getHostName();
		try {
			Socket clientSocket = new Socket(peerHost, peerPort);
			connectionManager.createConnection(clientSocket, peerInfo.getPeerId());

		} catch (UnknownHostException e) {
			System.out.println("Could not find host: " + peerHost);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Found peerhost " + peerHost + " but could not establish TCP connection");
			e.printStackTrace();
		}
	}
}
