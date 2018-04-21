package p2p;

import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public class ConnectionManager {

	private static ConnectionManager connectionManager = new ConnectionManager();
	private HashMap<String, Connection> interested; // interested but choked
	private HashSet<Connection> notInterested;
	private ArrayList<String> interestedPeerIds;
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
		interestedPeerIds = new ArrayList<>();
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

	public static synchronized ConnectionManager getInstance() {
		return connectionManager;
	}

	protected synchronized void tellAllNeighbors(int pieceIndex) {
		MessageManager messageManager = MessageManager.getInstance();
		for (Connection conn : preferredNeighbors) {
			int messageLength = messageManager.getMessageLength(Message.Type.HAVE, Integer.MIN_VALUE);
			byte[] payload = messageManager.getMessagePayload(Message.Type.HAVE, pieceIndex);
			conn.sendMessage(messageLength, payload);
		}
	}

	/*
	 * Add to preferred connections if total connections < n - 1 Remove from not
	 * interested if present. Add to interested.
	 */
	public synchronized void addInterestedConnection(String peerId, Connection connection) {
		System.out.println(totalConnections + " " + (n - 1));
		if (totalConnections < n - 1) {
			totalConnections++;

			interested.put(peerId, connection);
			interestedPeerIds.add(peerId);

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
	public synchronized void addNotInterestedConnection(String peerId, Connection connection) {
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
		System.out.println("Transfer started");
		MessageManager messageManager = MessageManager.getInstance();
		int messageLength = messageManager.getMessageLength(Message.Type.UNCHOKE, Integer.MIN_VALUE);
		byte[] messagePayload = messageManager.getMessagePayload(Message.Type.UNCHOKE, Integer.MIN_VALUE);
		for (int i = 1; i <= k + 1; i++) {
			String peerId = optimisticallyUnchokeNeighbor();
			System.out.println("Unchoking " + peerId);
			try {
				// System.out.println(new String(messageLength, "UTF-8"));
				System.out.println(new String(messagePayload, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (peerId == null) {
				break;
			}
			Connection conn = interested.remove(peerId);
			conn.sendMessage(messageLength, messagePayload);
			preferredNeighbors.add(conn);
		}
		monitor();
	}

	public synchronized String optimisticallyUnchokeNeighbor() {
		if (interestedPeerIds.size() <= 0) {
			return null;
		}
		System.out.println("Interested peer size:" + interestedPeerIds.size());
		int peerId = (int) (Math.random() * interestedPeerIds.size());
		System.out.println("Removing peer with id" + peerId);
		return interestedPeerIds.remove(peerId);
	}

	public boolean isRequested(int pieceIndex) {
		return requestedPieces.contains(pieceIndex);
	}

	protected synchronized void createConnection(Socket socket, String peerId) {
		new Connection(socket, peerId);
	}

	protected synchronized void createConnection(Socket socket) {
		new Connection(socket);
	}

	public synchronized void setRequested(int pieceIndex) {
		requestedPieces.add(pieceIndex);

	}

}
