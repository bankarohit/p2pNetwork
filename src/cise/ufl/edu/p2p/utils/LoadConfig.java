package cise.ufl.edu.p2p.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import cise.ufl.edu.p2p.peer.CommonProperties;

public class LoadConfig {

	public LoadConfig() {
		loadProperties();
	}

	private void loadProperties() {

		// TODO: Implement read locking scenario that doesn't cause an issue if
		// multiple peers try to read from the same file
		Properties properties = new Properties();
		try {
			FileInputStream in = new FileInputStream(Constants.COMMON_PROPERTIES_CONFIG_PATH);
			properties.load(in);
		} catch (IOException e) {
			System.out.println("Config file not found");
		}

		CommonProperties.setFileName(properties.get(Constants.CPROP_FILENAME).toString());
		CommonProperties.setFileSize(
				Long.parseLong(properties.get(Constants.CPROP_FILESIZE).toString()));
		CommonProperties.setNumberOfPreferredNeighbors(Integer.parseInt(
				properties.get(Constants.CPROP_NUMBER_OF_PREFERRED_NEIGHBORS).toString()));
		CommonProperties.setOptimisticUnchokingInterval(Integer.parseInt(
				properties.get(Constants.CPROP_OPTIMISTIC_UNCHOKING_INTERVAL).toString()));
		CommonProperties.setPieceSize(Integer
				.parseInt(properties.getProperty(Constants.CPROP_PIECESIZE).toString()));
		CommonProperties.setUnchokingInterval(Integer.parseInt(
				properties.getProperty(Constants.CPROP_UNCHOKING_INTERVAL).toString()));
		CommonProperties.calculateNumberOfPieces();

	}

}
