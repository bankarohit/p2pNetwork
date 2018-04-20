package cise.ufl.edu.p2p.peer;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.BitSet;
import java.util.concurrent.ConcurrentHashMap;

import cise.ufl.edu.p2p.utils.Constants;

public class SharedFile {
	private static ConcurrentHashMap<Integer, byte[]> file;
	public static BitSet filePieces;

	public static void splitFile() {
		file = new ConcurrentHashMap<Integer, byte[]>();
		filePieces = new BitSet(CommonProperties.getNumberOfPieces());
		File filePtr = new File(Constants.COMMON_PROPERTIES_CONFIG_PATH);
		FileInputStream fis = null;
		DataInputStream dis = null;
		try {
			fis = new FileInputStream(filePtr);
			dis = new DataInputStream(fis);
			int pieceSize = CommonProperties.getPieceSize();
			int pieceIndex = 0;
			// TODO: will fileInputStream always read pieceSize amount of data?
			try {
				for (int i = 0; i < CommonProperties.getNumberOfPieces(); i++) {
					pieceSize = (int) (i == CommonProperties.getNumberOfPieces() - 1 ? CommonProperties.getPieceSize()
							: CommonProperties.getFileSize() % CommonProperties.getPieceSize());
					byte[] piece = new byte[pieceSize];
					dis.read(piece);
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
		System.out.println("SharedFile.splitFile() - Filepieces: " + filePieces.size());
		System.out.println("SharedFile.splitFile() - Filepieces cardinality: " + filePieces.length());
		System.out.println("SharedFile.splitFile() - Filepieces 0 index: " + filePieces.get(0));
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

	public static synchronized void receivedPiece(int index) {
		filePieces.set(index);
	}

	public static synchronized boolean isPieceAvailable(int index) {
		return filePieces.get(index);
	}

	public static synchronized BitSet getFilePieces() {
		// TODO Auto-generated method stub
		return filePieces;
	}

}
