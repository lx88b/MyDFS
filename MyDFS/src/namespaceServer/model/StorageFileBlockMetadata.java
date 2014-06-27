package namespaceServer.model;

import java.util.ArrayList;
import java.util.UUID;

public class StorageFileBlockMetadata {
	private UUID blockID=null;
	private ArrayList<Integer> nodePorts=new ArrayList<Integer>();
	private int blockSize=0;
	private String fullFileName;
	public StorageFileBlockMetadata(String fileName) {
		fullFileName = fileName;
	}
	public String getFullFileName() {
		return this.fullFileName;
	}
	public void setBlockID(UUID id) {
		blockID = id;
	}
	public UUID getBlockID() {
		return blockID;
	}
	public void addNodePort(int port) {
		nodePorts.add(port);
	}
	public ArrayList<Integer> getNodePort() {
		return this.nodePorts;
	}
	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}
	public int getBlockSize() {
		return this.blockSize;
	}
}
