package cise.ufl.edu.p2p.peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.BitSet;

import cise.ufl.edu.p2p.messages.Handshake;
import cise.ufl.edu.p2p.messages.Message;
import cise.ufl.edu.p2p.messages.MessageManager;

public class Download implements Runnable {
	private Socket socket;
	private ObjectInputStream in;
	private SharedData sharedData;
	private MessageManager messageManager = MessageManager.getInstance();

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
		receiveMessage();

	}

	// TODO : Define incorrect peer id error
	private void receiveHandshake() {
		synchronized (sharedData) {

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

	private void receiveMessage() {
		byte[] messageLength = new byte[4];
		receiveMessageLength(messageLength);
		int len = messageManager.getLength(messageLength);
		byte[] payload = new byte[len];
		receiveMessagePayload(payload);
		processPayload(payload);

	}

	private void processPayload(byte[] payload) {
		Message.Type messageType = messageManager.getType(payload[0]);
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
			FileHandler.setFilePieces(BitSet.valueOf(ByteBuffer.wrap(payload, 1, payload.length - 1)));
			System.out.println("In download class - Bitfield received");
			System.out.println("BitSet cardinality: " + FileHandler.getFilePieces().length());
			break;
		case REQUEST:
			break;
		case PIECE:
			break;
		}
	}

	private void receiveMessageLength(byte[] messageLength) {
		receiveRawData(messageLength);
	}

	private void receiveMessagePayload(byte[] payload) {
		receiveRawData(payload);
	}

	private void receiveRawData(byte[] message) {
		System.out.println("Reading message of len: " + message.length);
		try {
			in.readFully(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Message read successfully!");
	}

}
