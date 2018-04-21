package p2p;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class Upload implements Runnable {
	private Socket socket;
	private DataOutputStream out;
	private SharedData sharedData;
	protected LinkedBlockingQueue<Integer> lengthQueue;
	protected LinkedBlockingQueue<byte[]> payloadQueue;
	private boolean isAlive;

	// client thread initialization
	public Upload(Socket socket, String id, SharedData data) {
		init(socket, data);
	}

	// server thread initialization
	public Upload(Socket socket, SharedData data) {
		init(socket, data);
	}

	private void init(Socket clientSocket, SharedData data) {
		payloadQueue = new LinkedBlockingQueue<>();
		lengthQueue = new LinkedBlockingQueue<>();
		isAlive = true;
		this.socket = clientSocket;
		sharedData = data;
		try {
			out = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (isAlive) {
			synchronized (lengthQueue) {
				try {
					lengthQueue.wait();
					int messageLength = lengthQueue.poll();
					out.writeInt(messageLength);
					out.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			synchronized (payloadQueue) {
				try {
					if (payloadQueue.size() <= 0) {
						payloadQueue.wait();
					}
					byte[] payload = payloadQueue.poll();
					sendRawData(payload);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void sendRawData(byte[] message) {
		// try {
		// System.out.println("Sent message: " + new String(message, "UTF-8"));
		// } catch (UnsupportedEncodingException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		try {
			out.write(message);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void addMessageLength(int length) {
		lengthQueue.offer(length);
	}

	public void addMessagePayload(byte[] payload) {
		payloadQueue.offer(payload);
	}

	protected void choke() {
		try {
			Thread.sleep(CommonProperties.getUnchokingInterval() * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
