package cise.ufl.edu.p2p.peer;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import cise.ufl.edu.p2p.messages.Message;
import cise.ufl.edu.p2p.messages.MessageManager;
import cise.ufl.edu.p2p.utils.LoadPeerList;

public class ConnectionManager {

	private static ConnectionManager connectionManager = new ConnectionManager();
	private HashMap<String, Connection> interested; // interested but choked
	private HashSet<Connection> notInterested;
	private PriorityQueue<Connection> preferredNeighbors;
	private int k = CommonProperties.getNumberOfPreferredNeighbors();
	private int m = CommonProperties.getOptimisticUnchokingInterval();
	private int p = CommonProperties.getUnchokingInterval();
	private int n = LoadPeerList.numberOfPeers();
	private HashSet<Integer> requestedPieces;
	private int maxConnections = k + 1;
	private int totalConnections = 0;

	private ConnectionManager() {
		interested = new HashMap<>();
		notInterested = new HashSet<>();
		preferredNeighbors = new PriorityQueue<>(k + 1, (a, b) -> (int) a.getSpeed() - (int) b.getSpeed());
		requestedPieces = new HashSet<>();
	}

	private void monitor() {
		// new Timer().schedule(new TimerTask() {
		// @Override
		// public void run() {
		// unchokeKPreferredNeighbors();
		// }
		// }, new Date(), p * 1000);
		//
		// new Timer().schedule(new TimerTask() {
		// @Override
		// public void run() {
		// optimisticallyUnchokeNeighbor();
		// }
		// }, new Date(), m * 1000);

	}

	public static ConnectionManager getInstance() {
		return connectionManager;
	}

	protected void tellAllNeighbors(ByteBuffer pieceIndex) {
		MessageManager messageManager = MessageManager.getInstance();
		for (Connection conn : preferredNeighbors) {
			byte[] messageLength = messageManager.getMessageLength(Message.Type.HAVE, null);
			byte[] payload = messageManager.getPayload(Message.Type.HAVE, pieceIndex);
			conn.sendMessage(messageLength, payload);
		}
	}

	/*
	 * Add to preferred connections if total connections < n - 1 Remove from not
	 * interested if present. Add to interested.
	 */
	public void addInterestedConnection(String peerId, Connection connection) {
		if (totalConnections < n - 1) {
			totalConnections++;
			interested.put(peerId, connection);
			if (totalConnections == n - 1) {
				startTransfer();
			}
		} else {
			if (notInterested.contains(connection)) {
				notInterested.remove(connection);
				interested.put(peerId, connection);
			}
		}
	}

	/*
	 * If totalConnections < n - 1, increment totalConnections. Remove from
	 * interested, if present. Add to not interested.
	 */
	public void addNotInterestedConnection(String peerId, Connection connection) {
		if (totalConnections < n - 1) {
			totalConnections++;
			notInterested.add(connection);
			if (totalConnections == n - 1) {
				startTransfer();
			}
		} else if (interested.containsKey(peerId)) {
			interested.remove(connection);
			notInterested.add(connection);
		}
	}

	/*
	 * Optimistically unchoke k + 1 neighbors from preferred neighbors & start both
	 * timers
	 */
	private void startTransfer() {
		MessageManager messageManager = MessageManager.getInstance();
		byte[] messageLength = messageManager.getMessageLength(Message.Type.UNCHOKE, null);
		byte[] messagePayload = messageManager.getPayload(Message.Type.UNCHOKE, null);
		for (int i = 1; i <= k + 1 && interested.size() > 0; i++) {
			int peer = (int) (interested.size() * Math.random());
			Connection conn = interested.remove(peer);
			conn.sendMessage(messageLength, messagePayload);
			preferredNeighbors.add(conn);
		}
		monitor();
	}

	public boolean isRequested(int pieceIndex) {
		return requestedPieces.contains(pieceIndex);
	}

	protected void createConnection(Socket socket, String peerId) {
		new Connection(socket, peerId);
	}

	protected void createConnection(Socket socket) {
		new Connection(socket);
	}

}
