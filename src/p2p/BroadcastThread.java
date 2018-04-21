package p2p;

import java.util.concurrent.LinkedBlockingQueue;

public class BroadcastThread extends Thread {
	private LinkedBlockingQueue<Object[]> queue;
	private MessageManager messageManager;
	private Connection conn;
	private Message.Type messageType;
	private int pieceIndex;
	private static final BroadcastThread broadcaster;

	static {
		broadcaster = new BroadcastThread();
		broadcaster.start();
	}

	private BroadcastThread() {
		queue = new LinkedBlockingQueue<>();
		messageManager = MessageManager.getInstance();
		conn = null;
		messageType = null;
		pieceIndex = Integer.MIN_VALUE;
	}

	protected static synchronized BroadcastThread getInstance() {
		return broadcaster;
	}

	protected synchronized void addMessage(Object[] data) {
		queue.offer(data);
	}

	@Override
	public void run() {
		while (true) {
			if (!queue.isEmpty()) {
				Object[] data = queue.poll();
				conn = (Connection) data[0];
				messageType = (Message.Type) data[1];
				pieceIndex = (int) data[2];
				int messageLength = messageManager.getMessageLength(messageType, pieceIndex);
				byte[] payload = messageManager.getMessagePayload(messageType, pieceIndex);
				conn.sendMessage(messageLength, payload);
				System.out.println("Sending message of type: " + messageType + " to peer " + conn.getRemotePeerId());
			}
		}
	}

}
