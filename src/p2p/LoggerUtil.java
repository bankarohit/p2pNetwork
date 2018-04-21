package p2p;

import java.util.Calendar;
import java.util.List;


public class LoggerUtil {

//	private static Logger logger = Logger.getLogger(LoggerUtil.class);
	private static LoggerUtil customLogger;

	private LoggerUtil() {
	}

	public static synchronized LoggerUtil getInstance() {
		if (customLogger == null) {
			customLogger = new LoggerUtil();
		}
		return customLogger;
	}

	// [Time]: Peer [peer_ID 1] makes a connection to Peer [peer_ID 2].
	public void logTcpConnectionTo(String peerFrom, String peerTo) {
		System.out.println(getTime() + "Peer " + peerFrom + " makes a connection to Peer " + peerTo
				+ ".");
	}

	// [Time]: Peer [peer_ID 1] is connected from Peer [peer_ID 2].
	public void logTcpConnectionFrom(String peerFrom, String peerTo) {
		System.out.println(
				getTime() + "Peer " + peerFrom + " is connected from Peer " + peerTo + ".");
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
		System.out.println(log.toString() + ".");
	}

	// [Time]: Peer [peer_ID] has the optimistically unchoked neighbor
	// [optimistically unchoked neighbor ID].
	public void logOptimisticallyUnchokeNeighbor(String source, String unchokedNeighbor) {
		System.out.println(getTime() + "Peer " + source + " has the optimistically unchoked neighbor "
				+ unchokedNeighbor + ".");
	}

	// [Time]: Peer [peer_ID 1] is unchoked by [peer_ID 2].
	public void logUnchokingNeighbor(String peerId1, String peerId2) {
		System.out.println(getTime() + "Peer " + peerId1 + " is unchoked by " + peerId2 + ".");
	}

	// [Time]: Peer [peer_ID 1] is choked by [peer_ID 2].
	public void logChokingNeighbor(String peerId1, String peerId2) {
		System.out.println(getTime() + "Peer " + peerId1 + " is choked by " + peerId2 + ".");
	}

	// [Time]: Peer [peer_ID 1] received the ‘have’ message from [peer_ID 2] for
	// the piece [piece index].
	public void logReceivedHaveMessage(String to, String from, int pieceIndex) {
		System.out.println(getTime() + "Peer " + to + " received the 'have' message from " + from
				+ " for the piece " + pieceIndex + ".");
	}

	// [Time]: Peer [peer_ID 1] received the ‘interested’ message from [peer_ID
	// 2].
	public void logReceivedInterestedMessage(String to, String from) {
		System.out.println(getTime() + "Peer " + to + " received the 'interested' message from "
				+ from + ".");
	}

	// [Time]: Peer [peer_ID 1] received the ‘not interested’ message from
	// [peer_ID 2].
	public void logReceivedNotInterestedMessage(String to, String from) {
		System.out.println(getTime() + "Peer " + to + " received the 'not interested' message from "
				+ from + ".");
	}

	// [Time]: Peer [peer_ID 1] has downloaded the piece [piece index] from
	// [peer_ID 2].
	public void logDownloadedPiece(String to, String from, int pieceIndex) {
		System.out.println(getTime() + "Peer " + to + " has downloaded the piece " + pieceIndex
				+ " from " + from);
	}

	// [Time]: Peer [peer_ID] has downloaded the complete file.
	public void logFinishedDownloading(String peerId) {
		System.out.println(getTime() + "Peer " + peerId + " has downloaded the complete file.");
	}

//	public void logDebug(String str) {
//		logger.debug(str);
//	}

	public String getTime() {
		return Calendar.getInstance().getTime() + ": ";
	}
}