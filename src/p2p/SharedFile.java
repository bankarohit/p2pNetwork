package p2p;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class SharedFile extends Thread {
	private static ConcurrentHashMap<Integer, byte[]> file;
	private static BitSet filePieces;
	private static FileChannel writeFileChannel;
	private static int receivedFileSize;
	private LinkedBlockingQueue<byte[]> fileQueue;
	private static SharedFile sharedFile;
	// private ConnectionManager connectionManager;

	private SharedFile() {
		fileQueue = new LinkedBlockingQueue<>();
		receivedFileSize = 0;
		// connectionManager = ConnectionManager.getInstance();
	}

	public static synchronized SharedFile getInstance() {
		if (null == sharedFile) {
			sharedFile = new SharedFile();
			sharedFile.start();
		}
		return sharedFile;
	}

	static {
		file = new ConcurrentHashMap<Integer, byte[]>();
		filePieces = new BitSet(CommonProperties.getNumberOfPieces());
		try {
			File createdFile = new File(Constants.COMMON_PROPERTIES_CREATED_FILE_PATH + peerProcessMain.getId()
					+ File.separatorChar + CommonProperties.getFileName());
			createdFile.getParentFile().mkdirs(); // Will create parent directories if not exists
			createdFile.createNewFile();
			writeFileChannel = FileChannel.open(createdFile.toPath(), StandardOpenOption.WRITE);
		} catch (IOException e) {
			System.out.println("Failed to create new file while receiving the file from host peer");
			e.printStackTrace();
		}
	}

	public void splitFile() {
		File filePtr = new File(Constants.COMMON_PROPERTIES_FILE_PATH + CommonProperties.getFileName());
		FileInputStream fis = null;
		DataInputStream dis = null;
		int fileSize = (int) CommonProperties.getFileSize();
		int numberOfPieces = CommonProperties.getNumberOfPieces();
		// System.out.println("Filesize: " + fileSize);
		try {
			fis = new FileInputStream(filePtr);
			dis = new DataInputStream(fis);
			int pieceSize = CommonProperties.getPieceSize();
			int pieceIndex = 0;
			// TODO: will fileInputStream always read pieceSize amount of data?
			try {
				for (int i = 0; i < CommonProperties.getNumberOfPieces(); i++) {
					pieceSize = i != numberOfPieces - 1 ? CommonProperties.getPieceSize()
							: fileSize % CommonProperties.getPieceSize();
					byte[] piece = new byte[pieceSize];
					// System.out.println("Reading piece of size: " + pieceSize);
					dis.readFully(piece);
					file.put(pieceIndex, piece);
					filePieces.set(pieceIndex++);
				}
			} catch (IOException fileReadError) {
				fileReadError.printStackTrace();
				System.out.println("Error while splitting file");
			}

		} catch (FileNotFoundException e) {
			System.out.println("Error reading common.cfg file");
			e.printStackTrace();
		} finally {
			try {
				fis.close();
				dis.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Error while closing fileinputstream after reading file");
			}
		}
	}

	public synchronized byte[] getPiece(int index) {
		return file.get(index);
	}

	@Override
	public void run() {
		while (true) {
			try {
				byte[] payload = fileQueue.take();
				int pieceIndex = ByteBuffer.wrap(payload, 0, 4).getInt();
				writeFileChannel.position(pieceIndex * 16384);
				System.out.println("Writing piece: " + pieceIndex);
				System.out.println(
						"Bytes written: " + writeFileChannel.write(ByteBuffer.wrap(payload, 4, payload.length - 4)));
				if (isCompleteFile()) {
					System.exit(0);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public synchronized void setPiece(byte[] payload) {
		filePieces.set(ByteBuffer.wrap(payload, 0, 4).getInt());
		file.put(ByteBuffer.wrap(payload, 0, 4).getInt(), Arrays.copyOfRange(payload, 4, payload.length - 4));
		setReceivedFileSize();
		try {
			fileQueue.put(payload);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized boolean isPieceAvailable(int index) {
		return filePieces.get(index);
	}

	public synchronized boolean isCompleteFile() {
		return getReceivedFileSize() == CommonProperties.getNumberOfPieces();
	}

	protected synchronized int getReceivedFileSize() {
		return receivedFileSize;
	}

	private synchronized int setReceivedFileSize() {
		return receivedFileSize++;
	}

	public synchronized boolean isSubset(BitSet peerBitSet) {
		for (int i = 0; i < CommonProperties.getNumberOfPieces(); i++) {
			if (!peerBitSet.get(i) && isPieceAvailable(i)) {
				return true;
			}
		}
		return false;
	}

	protected synchronized int getRequestPieceIndex(Connection conn) {
		ConnectionManager connectionManager = ConnectionManager.getInstance();
		BitSet peerBitset = conn.getPeerBitSet();
		int requestPieceIndex = 0;
		int numberOfPieces = CommonProperties.getNumberOfPieces();
		// for (int i = 1; i < peerBitset.length(); i++) {
		// // System.out.print(peerBitset.get(i) ? 1 : 0 + " ");
		// if (peerBitset.get(i) && "1002".equals(conn.getRemotePeerId()))
		// LoggerUtil.getInstance().logDebug(conn.getRemotePeerId() + ": I have piece
		// index: " + i);
		// }
		do {
			if (sharedFile.isCompleteFile()) {
				System.out.println("File received");
				return Integer.MIN_VALUE;
			}
			requestPieceIndex = (int) (Math.random() * numberOfPieces);
			// if ("1002".equals(conn.getRemotePeerId())) {
			// LoggerUtil.getInstance().logDebug(requestPieceIndex + " ? " +
			// peerBitset.get(requestPieceIndex));
			// }
			// LoggerUtil.getInstance().logDebug("" + requestPieceIndex);
			// System.out.println("peerbitset: " + peerBitset + " connectionManager" +
			// connectionManager);
			// LoggerUtil.getInstance().logDebug("Requested piece index" +
			// requestPieceIndex);
		} while (!peerBitset.get(requestPieceIndex) || connectionManager.isRequested(requestPieceIndex)
				|| isPieceAvailable(requestPieceIndex));
		conn.addRequestedPiece(requestPieceIndex);
		return requestPieceIndex;
	}

	protected BitSet getFilePieces() {
		return filePieces;
	}

	protected synchronized boolean hasAnyPieces() {
		return filePieces.nextSetBit(0) != -1;
	}

}
