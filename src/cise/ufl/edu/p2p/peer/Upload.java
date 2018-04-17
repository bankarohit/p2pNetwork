package cise.ufl.edu.p2p.peer;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import cise.ufl.edu.p2p.messages.Handshake;

public class Upload implements Runnable {
	private Socket socket;
	private ObjectOutputStream out;
	private SharedData sharedData;

	// client thread initialization
	public Upload(Socket socket, String id, SharedData data) {
		init(socket, data);
		sharedData.setUploadHandshake(true);
	}

	// server thread initialization
	public Upload(Socket socket, SharedData data) {
		init(socket, data);
	}

	private void init(Socket clientSocket, SharedData data) {
		socket = clientSocket;
		sharedData = data;
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		synchronized (sharedData) {
			while (!sharedData.getUploadHandshake()) {
				try {
					sharedData.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		sendHandshakeMessage();
	}

	private void sendHandshakeMessage() {
		String handshakeMessage = Handshake.getMessage();
		synchronized (sharedData) {
			try {
				System.out.println("Sending message: " + handshakeMessage);
				out.writeBytes(handshakeMessage);
				out.flush();
			} catch (IOException e) {
				System.out.println("Could not send handshake message. Retrying..");
				e.printStackTrace();
			}
			sharedData.setDownloadHandshake(true);
			sharedData.notify();
		}
	}

	public void sendMessage(byte[] messageLength, byte[] payload) {
		if (messageLength != null) {
			sendRawData(messageLength);
			sendRawData(payload);
		}
	}

	private void sendRawData(byte[] message) {
		try {
			out.write(message);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
