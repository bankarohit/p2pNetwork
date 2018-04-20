package cise.ufl.edu.p2p.messages;

import java.util.BitSet;

import cise.ufl.edu.p2p.peer.CommonProperties;
import cise.ufl.edu.p2p.peer.SharedFile;

public class BitField extends Message {

	private static BitField bitfield = new BitField();

	private BitField() {
		type = 5;
		BitSet filePieces = SharedFile.getFilePieces();
		payload = new byte[CommonProperties.getNumberOfPieces() + 1];
		payload[0] = type;
		content = new byte[CommonProperties.getNumberOfPieces()];
		for (int i = 0; i < CommonProperties.getNumberOfPieces(); i++) {
			if (filePieces.get(i)) {
				content[i] = 1;
				payload[i + 1] = 1;
			}
		}
	}

	public synchronized static BitField getInstance() {
		return bitfield;
	}

	@Override
	protected synchronized int getMessageLength() {
		return payload.length;
	}

	@Override
	protected synchronized byte[] getPayload() {
		return payload;
	}

}
