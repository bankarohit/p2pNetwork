package p2p;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.List;

public class LoggerUtil {

	private static LoggerUtil customLogger;

	public static PrintWriter printWriter = null;

	private LoggerUtil() {
		try {
			System.out.println(Peer.getInstance().getNetwork().getPeerId());

			File file = new File(Constants.PEER_LOG_FILE_PATH + Peer.getInstance().getNetwork().getPeerId()
					+ Constants.PEER_LOG_FILE_EXTENSION);
			file.getParentFile().mkdirs(); // Will create parent directories if not exists
			file.createNewFile();
			FileOutputStream fileOutputStream = new FileOutputStream(file, false);
			printWriter = new PrintWriter(fileOutputStream, true);
		} catch (Exception e) {
			System.out.println("Error: Failed to create log file");
		}
	}

	public static synchronized LoggerUtil getInstance() {
		if (customLogger == null) {
			customLogger = new LoggerUtil();
		}
		return customLogger;
	}

	private void writeToFile(String message) {
		synchronized (this) {
			printWriter.println(message);
		}
	}

	// [Time]: Peer [peer_ID 1] makes a connection to Peer [peer_ID 2].
	public void logTcpConnectionTo(String peerFrom, String peerTo) {
		writeToFile(getTime() + "Peer " + peerFrom + " makes a connection to Peer " + peerTo + ".");
	}

	// [Time]: Peer [peer_ID 1] is connected from Peer [peer_ID 2].
	public void logTcpConnectionFrom(String peerFrom, String peerTo) {
		writeToFile(getTime() + "Peer " + peerFrom + " is connected from Peer " + peerTo + ".");
	}

	// [Time]: Peer [peer_ID] has the preferred neighbors [preferred neighbor ID
	// list].
	public void logChangePreferredNeighbors(String peerId, List<Peer> peers) {
		StringBuilder log = new StringBuilder();
		log.append(getTime());
		log.append("Peer " + peerId + " has the preferred neighbors ");
		String prefix = "";
		for (Peer p : peers) {
			log.append(prefix);
			prefix = ", ";
			log.append(p.getNetwork().getPeerId());
		}
		writeToFile(log.toString() + ".");
	}

	// [Time]: Peer [peer_ID] has the optimistically unchoked neighbor
	// [optimistically unchoked neighbor ID].
	public void logOptimisticallyUnchokeNeighbor(String source, String unchokedNeighbor) {
		writeToFile(
				getTime() + "Peer " + source + " has the optimistically unchoked neighbor " + unchokedNeighbor + ".");
	}

	// [Time]: Peer [peer_ID 1] is unchoked by [peer_ID 2].
	public void logUnchokingNeighbor(String peerId1, String peerId2) {
		writeToFile(getTime() + "Peer " + peerId1 + " is unchoked by " + peerId2 + ".");
	}

	// [Time]: Peer [peer_ID 1] is choked by [peer_ID 2].
	public void logChokingNeighbor(String peerId1, String peerId2) {
		writeToFile(getTime() + "Peer " + peerId1 + " is choked by " + peerId2 + ".");
	}

	// [Time]: Peer [peer_ID 1] received the ‘have’ message from [peer_ID 2] for
	// the piece [piece index].
	public void logReceivedHaveMessage(String to, String from, int pieceIndex) {
		writeToFile(getTime() + "Peer " + to + " received the 'have' message from " + from + " for the piece "
				+ pieceIndex + ".");
	}

	// [Time]: Peer [peer_ID 1] received the ‘interested’ message from [peer_ID
	// 2].
	public void logReceivedInterestedMessage(String to, String from) {
		writeToFile(getTime() + "Peer " + to + " received the 'interested' message from " + from + ".");
	}

	// [Time]: Peer [peer_ID 1] received the ‘not interested’ message from
	// [peer_ID 2].
	public void logReceivedNotInterestedMessage(String to, String from) {
		writeToFile(getTime() + "Peer " + to + " received the 'not interested' message from " + from + ".");
	}

	// [Time]: Peer [peer_ID 1] has downloaded the piece [piece index] from
	// [peer_ID 2].
	public void logDownloadedPiece(String to, String from, int pieceIndex) {
		writeToFile(getTime() + "Peer " + to + " has downloaded the piece " + pieceIndex + " from " + from);
	}

	// [Time]: Peer [peer_ID] has downloaded the complete file.
	public void logFinishedDownloading(String peerId) {
		writeToFile(getTime() + "Peer " + peerId + " has downloaded the complete file.");
	}

	// public void logDebug(String str) {
	// logger.debug(str);
	// }

	public String getTime() {
		return Calendar.getInstance().getTime() + ": ";
	}
}