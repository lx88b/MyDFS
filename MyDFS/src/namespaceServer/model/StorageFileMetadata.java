package namespaceServer.model;

import java.util.HashMap;
import java.util.UUID;

public class StorageFileMetadata {
	private String fileName=null;
	private int fileSize = 0;
	private HashMap<UUID,StorageFileBlockMetadata> blocks = new HashMap<UUID,StorageFileBlockMetadata>();
	public void setFileName(String name) {
		fileName = name;
	}
	public String getFileName() {
		return this.fileName;
	}
	public void addBlock(UUID blockID,StorageFileBlockMetadata block) {
		blocks.put(blockID, block);
		fileSize+=block.getBlockSize();
	}
	public int getFileSize() {
		return fileSize;
	}
	public StorageFileBlockMetadata getBlock(UUID id) {
		return blocks.get(id);
	}
	
	public HashMap<UUID,StorageFileBlockMetadata> getBlocks() {
		return blocks;
	}
}
