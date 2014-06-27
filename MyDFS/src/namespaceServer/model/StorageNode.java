package namespaceServer.model;

import java.util.HashMap;
import java.util.UUID;

public class StorageNode {
	private int port=0;
	private int nodeSize=0;
	private HashMap<UUID,StorageFileBlockMetadata> blocks = new HashMap<UUID,StorageFileBlockMetadata>();
	public void addBlock(UUID blockID,StorageFileBlockMetadata block) {
		blocks.put(blockID, block);
		nodeSize+=block.getBlockSize();
	}
	public StorageFileBlockMetadata getBlock(UUID id) {
		return blocks.get(id);
	}
	public int getNodeSize() {
		return this.nodeSize;
	}
	public void setPort(int port){
		this.port=port;
	}
	public int getPort() {
		return this.port;
	}
	
	public void deleteBlock(UUID blockID) {
		StorageFileBlockMetadata block = blocks.get(blockID);
		if(block!=null)
			nodeSize -= block.getBlockSize();
		blocks.remove(blockID);
	}
	
	public HashMap<UUID,StorageFileBlockMetadata> getBlocks() {
		return this.blocks;
	}
}
