package p2p;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.ConcurrentHashMap;

public class SharedFile {
	private static ConcurrentHashMap<Integer, byte[]> file;
	public static BitSet filePieces;

	static {
		file = new ConcurrentHashMap<Integer, byte[]>();
		filePieces = new BitSet(CommonProperties.getNumberOfPieces());
	}

	public static void splitFile() {

		File filePtr = new File(Constants.COMMON_PROPERTIES_FILE_PATH);
		FileInputStream fis = null;
		DataInputStream dis = null;
		int fileSize = (int) CommonProperties.getFileSize();
		int numberOfPieces = CommonProperties.getNumberOfPieces();
		System.out.println("Filesize: " + fileSize);
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
		// System.out.println("SharedFile.splitFile() - Filepieces cardinality: " +
		// filePieces.length());
		// System.out.println("SharedFile.splitFile() - Filepieces 0 index: " +
		// filePieces.get(0));
	}

	public static void writeToFile() {

		// TODO: Need to give correct filename.
		String filename = Constants.COMMON_PROPERTIES_MY_FILE_PATH;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(filename);
			for (int i = 0; i < file.size(); i++) {
				try {
					fos.write(file.get(i));
					// System.out.println(file.get(i).hashCode());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static byte[] getPiece(int index) {
		return file.get(index);
	}

	public static synchronized int getNextMissingPiece() {
		return filePieces.nextClearBit(0);
	}

	public static synchronized void hasFile() {
		filePieces.flip(0, filePieces.size());
	}

	public static synchronized void setPiece(byte[] payload) {
		int pieceIndex = ByteBuffer.wrap(payload, 0, 4).getInt();
		System.out.println("Setting pieceIndex: " + pieceIndex);
		filePieces.set(pieceIndex);
		file.put(pieceIndex, Arrays.copyOfRange(payload, 4, payload.length));

	}

	public static synchronized boolean isPieceAvailable(int index) {
		return filePieces.get(index);
	}

	public static synchronized int getFileSize() {
		return file.size();
	}

	public static synchronized BitSet getFilePieces() {
		// TODO Auto-generated method stub
		return filePieces;
	}

}
