package cise.ufl.edu.p2p.peer;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import cise.ufl.edu.p2p.messages.Handshake;
import cise.ufl.edu.p2p.messages.Message;
import cise.ufl.edu.p2p.messages.Message.Type;

public class Upload implements Runnable {
	private Socket socket;
	private ObjectOutputStream out;
	private SharedData sharedData;

	// client thread initialization
	public Upload(Socket clientSocket, String id, SharedData data) {
		socket = clientSocket;
		sharedData = data;
		sharedData.setUploadHandshake(true);
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// server thread initialization
	public Upload(Socket clientSocket, SharedData data) {
		socket = clientSocket;
		sharedData = data;
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
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

	public void sendMessage(Type messageType) {
		byte[] message = null;
		switch (messageType) {
		case CHOKE:
			break;
		case UNCHOKE:
			break;
		case INTERESTED:
			message = Message.getMessage(messageType);
			break;
		case NOTINTERESTED:
			break;
		case HAVE:
			break;
		case BITFIELD:
			message = Message.getMessage(messageType);
			if (message != null) {
				System.out.println("Sending bitfield message");
			}
			break;
		case REQUEST:
			break;
		case PIECE:
			break;
		}
		if (message != null) {
			send(message);
		}

	}

	private void send(byte[] message) {
		try {
			out.write(message);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
