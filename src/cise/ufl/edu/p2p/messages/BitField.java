package cise.ufl.edu.p2p.messages;

import java.nio.ByteBuffer;
import cise.ufl.edu.p2p.peer.FileHandler;
import java.util.BitSet;

public class BitField extends Message {
	
	BitField(){
		this.type = (byte) 5;
	}
	
	public static byte[] getMessage() {
		BitSet b = FileHandler.getFilePieces();
		byte [] bitset = b.toByteArray();
		int messageSize = bitset.length +5;
		byte[] message = new byte[messageSize];
		byte[] messageLength = ByteBuffer.allocate(4).putInt((int) bitset.length).array();
		System.arraycopy(messageLength, 0, message, 0, messageLength.length); 
		message[4] = 5;
		System.arraycopy(bitset, 0, message, 5, bitset.length);
		return message;
	}
}
