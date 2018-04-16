package cise.ufl.edu.p2p.peer;

import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

	private static ConnectionManager connectionManager = new ConnectionManager();
	private ConcurrentHashMap<String, Connection> connections;
	
	private ConnectionManager() {
		connections = new ConcurrentHashMap<String, Connection>();
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

	public void addConnection(String peerId, Connection conn) {
		connections.put(peerId, conn);		
	}
	
}
