package cise.ufl.edu.p2p.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import cise.ufl.edu.p2p.peer.NetworkInfo;

public class LoadPeerList {

	private static HashMap<String, NetworkInfo> peerList = new HashMap<>();

	static {
		int id = 1;
		try {
			Scanner sc = new Scanner(new File(Constants.PEER_PROPERTIES_CONFIG_PATH));
			while (sc.hasNextLine()) {
				String str[] = sc.nextLine().split(" ");
				NetworkInfo peer = new NetworkInfo();
				peer.setNumber(id++);
				peer.setPeerId(str[0]);
				peer.setHostName(str[1]);
				peer.setPort(Integer.parseInt(str[2]));
				peer.setHasSharedFile(str[3].equals("1") ? true : false);
				peerList.put(str[0], peer);
			}
			sc.close();
		} catch (IOException e) {
			System.out.println("PeerInfo.cfg not found/corrupt");
		}
	}

	public static NetworkInfo getPeer(String id) {
		return peerList.get(id);
	}

	public static HashMap<String, NetworkInfo> getPeerList() {
		return peerList;
	}
}
