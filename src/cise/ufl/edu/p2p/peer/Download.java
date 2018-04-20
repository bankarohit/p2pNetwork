package cise.ufl.edu.p2p.peer;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class Download implements Runnable {
	private Socket socket;
	private DataInputStream in;
	private SharedData sharedData;

	// client thread initialization
	public Download(Socket socket, String peerId, SharedData data) {
		init(socket, data);
	}

	private void init(Socket socket, SharedData data) {
		this.socket = socket;
		sharedData = data;
		try {
			in = new DataInputStream(socket.getInputStream());
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
	}

	@Override
	public void run() {
		byte[] handshake = new byte[32];
		receiveRawData(handshake);
		sharedData.processHandshake(handshake);
		receiveMessage();
	}

	protected void receiveMessage() {
		System.out.println("Receive started");
		int messageLength = Integer.MIN_VALUE;
		messageLength = receiveMessageLength();
		byte[] payload = new byte[messageLength];
		receiveMessagePayload(payload);
		sharedData.processPayload(payload);
		System.out.println("Receive finished");
		receiveMessage();
	}

	private int receiveMessageLength() {
		int len = Integer.MIN_VALUE;
		try {
			len = in.readInt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return len;
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
