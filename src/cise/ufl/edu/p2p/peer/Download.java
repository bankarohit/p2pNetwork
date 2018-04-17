package cise.ufl.edu.p2p.peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import cise.ufl.edu.p2p.messages.Handshake;

public class Download implements Runnable {
	private Socket socket;
	private ObjectInputStream in;
	private SharedData sharedData;

	// client thread initialization
	public Download(Socket socket, String peerId, SharedData data) {
		init(socket, data);
	}

	private void init(Socket socket, SharedData data) {
		this.socket = socket;
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
	public Download(Socket socket, SharedData data) {
		init(socket, data);
		sharedData.setDownloadHandshake(true);
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
		int len = sharedData.processMessageLength(messageLength);
		byte[] payload = new byte[len];
		receiveMessagePayload(payload);
		sharedData.processPayload(payload);

	}

	private void receiveMessageLength(byte[] messageLength) {
		receiveRawData(messageLength);
	}

	private void receiveMessagePayload(byte[] payload) {
		receiveRawData(payload);
	}

	private void receiveRawData(byte[] message) {
		try {
			in.readFully(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
