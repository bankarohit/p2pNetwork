package cise.ufl.edu.p2p.peer;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class ConnectionManager {

	private static ConnectionManager connectionManager = new ConnectionManager();
	private List<Connection> chokedConnections;
	private List<Connection> unchokedConnections;
	private PriorityQueue<Connection> topKNeighbors;
	private int k = CommonProperties.getNumberOfPreferredNeighbors();
	private int m = CommonProperties.getOptimisticUnchokingInterval();
	private int p = CommonProperties.getUnchokingInterval();

	private ConnectionManager() {
		chokedConnections = new ArrayList<>();
		unchokedConnections = new ArrayList<>(k + 1);
		topKNeighbors = new PriorityQueue<>(k + 1, (a, b) -> (int) b.getSpeed() - (int) a.getSpeed());
		monitor();
	}

	private void monitor() {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				unchokeKPreferredNeighbors();
			}
		}, new Date(), p * 1000);

		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				optimisticallyUnchokeNeighbor();
			}
		}, new Date(), m * 1000);

	}

	public boolean isUnchokedConnectionsFull() {
		return unchokedConnections.size() >= k + 1;
	}

	public static ConnectionManager getInstance() {
		return connectionManager;
	}

	protected void createConnection(Socket socket, String peerId) {
		new Connection(socket, peerId);
	}

	protected void createConnection(Socket socket) {
		new Connection(socket);
	}

	public void addConnection(Connection conn) {
		List<Connection> connection = unchokedConnections;
		if (isUnchokedConnectionsFull()) {
			connection = chokedConnections;
			conn.setChoked(true);
		}
		connection.add(conn);
		topKNeighbors.add(conn);
	}

	public void unchokeKPreferredNeighbors() {
		unchokedConnections = new ArrayList<>();
		chokedConnections = new ArrayList<>();
		for (int i = 0; i < k && !topKNeighbors.isEmpty(); i++) {
			Connection conn = topKNeighbors.poll();
			conn.setChoked(false);
			unchokedConnections.add(conn);
		}
		while (!topKNeighbors.isEmpty()) {
			Connection conn = topKNeighbors.poll();
			conn.setChoked(true);
			chokedConnections.add(conn);
		}
		topKNeighbors = new PriorityQueue<>(chokedConnections);
		topKNeighbors.addAll(unchokedConnections);
	}

	// TODO: Optimize if possible by using some other data structure instead of list
	public Connection optimisticallyUnchokeNeighbor() {
		if (chokedConnections.isEmpty())
			return null;
		Connection conn = chokedConnections.remove(new Random().nextInt(chokedConnections.size()));
		unchokedConnections.add(conn);
		return conn;
	}

}
