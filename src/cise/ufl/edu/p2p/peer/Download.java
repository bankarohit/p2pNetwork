package cise.ufl.edu.p2p.peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import cise.ufl.edu.p2p.messages.Handshake;
import cise.ufl.edu.p2p.messages.Message;

public class Download implements Runnable {
	private Socket socket;
	private ObjectInputStream in;
	private SharedData sharedData;

	// client thread initialization
	public Download(Socket clientSocket, String peerId, SharedData data) {
		socket = clientSocket;
		sharedData = data;
		try {
			in = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void choke() {
		try {
			Thread.sleep(CommonProperties.getUnchokingInterval() * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// server thread initialization
	public Download(Socket clientSocket, SharedData data) {
		socket = clientSocket;
		sharedData = data;
		sharedData.setDownloadHandshake(true);
		try {
			in = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		synchronized (sharedData) {
			while (!sharedData.getDownloadHandshake()) {
				try {
					sharedData.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		receiveHandshake();
		processMessage();

	}

	// TODO : Define incorrect peer id error
	private void receiveHandshake() {
		synchronized (sharedData) {
			ConnectionManager connectionManager = ConnectionManager.getInstance();
			byte[] message = new byte[32];
			try {
				in.readFully(message);
				System.out.println("Received handshake message: " + new String(message, "UTF-8"));
			} catch (IOException e) {
				System.out.println("Error while receiving handshake message");
				e.printStackTrace();
			}
			String remotePeerId = Handshake.getRemotePeerId(message);
			sharedData.addConnection(remotePeerId);
			sharedData.setUploadHandshake(true);
			sharedData.notify();
		}
		sharedData.sendBitfieldMessage();
	}

	private void processMessage() {
		byte[] metadata = new byte[5];
		receiveMessage(metadata);
		Message.Type messageType = Message.getType(metadata);

		switch (messageType) {
		case CHOKE:
			choke();
			break;
		case UNCHOKE:
			break;
		case INTERESTED:
			break;
		case NOTINTERESTED:
			break;
		case HAVE:
			break;
		case BITFIELD:
			int length = Message.getLength(metadata);
			byte[] message = new byte[length];
			message = receiveMessage(message);
			sharedData.setPeerBitset(message);
			sharedData.sendInterestedNotinterested();
			break;
		case REQUEST:
			break;
		case PIECE:
			break;

		}

	}

	private byte[] receiveMessage(byte[] message) {
		try {
			in.readFully(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return message;
	}

}
