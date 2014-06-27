package namespaceServer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;

public class NamespaceServer {
	//节点端口：节点对象
	HashMap<Integer,StorageNode> nodeList = new HashMap<Integer,StorageNode>();
	//文件名：文件对象
	HashMap<String,StorageFileMetadata> fileMetadataList = new HashMap<String,StorageFileMetadata>();
	//目录名:<类型：目录文件对象>
	private Object sysNodeObject = new Object();
	private Object sysfileObject = new Object();
	private static NamespaceServer ns = null;
	public static NamespaceServer getNamespaceServer() {
		if(ns==null)
			ns = new NamespaceServer();
		return ns;
	}
	
	/**
	 * 
	 * @param fileName
	 * @param blockSize
	 * @return 当前块的id以及被放置的节点集合
	 */
	public synchronized HashMap<UUID,ArrayList<Integer>> addFile(String fileName, int blockSize) {
		if(this.fileMetadataList.containsKey(fileName))
			return null;
		if(nodeList.isEmpty())
			return null;
		HashMap<UUID,ArrayList<Integer>> temp = new HashMap<UUID,ArrayList<Integer>>();
		ArrayList<Integer> tempNodes = new ArrayList<Integer>();
		//生成该块编号
		UUID blockID = UUID.randomUUID();
		//获得所有存储节点并按存储数据大小排序
		ArrayList<StorageNode> nodes = (ArrayList<StorageNode>)nodeList.values();
		Collections.sort(nodes, new Comparator<StorageNode>(){
			@Override
			public int compare(StorageNode arg0, StorageNode arg1) {
				// TODO Auto-generated method stub
				return arg0.getNodeSize()-arg1.getNodeSize();
			}
		});
		//获得最小的节点，
		StorageNode node1 = nodes.get(0);
		//创建新文件和新块
		StorageFileMetadata newFile = new StorageFileMetadata();
		StorageFileBlockMetadata newBlock = new StorageFileBlockMetadata();
		//给该新块添加第一个存储节点的信息
		newBlock.addNodePort(node1.getPort());
		newBlock.setBlockID(blockID);
		newBlock.setBlockSize(blockSize);
		//如果存在多个存储节点，给该块添加第二个存储节点的信息
		if(nodes.size()>1) {
			StorageNode node2 = nodes.get(1);
			newBlock.addNodePort(node2.getPort());
		}
		//给新文件添加块和文件名
		newFile.addBlock(blockID, newBlock);
		newFile.setFileName(fileName);
		//添加新文件信息
		this.fileMetadataList.put(fileName, newFile);
		//在最小节点中添加该块并更新节点信息
		node1.addBlock(blockID, newBlock);
		this.nodeList.put(node1.getPort(), node1);
		tempNodes.add(node1.getPort());
		//如果存在，在次小的节点中添加该块信息并更新节点信息
		if(nodes.size()>1) {
			StorageNode node2 = nodes.get(1);
			node2.addBlock(blockID, newBlock);
			this.nodeList.put(node2.getPort(), node2);
			tempNodes.add(node2.getPort());
		}
		temp.put(blockID, tempNodes);
		return temp;
	}

	/**
	 * 
	 * @param fileName
	 * @return 该文件的所有块编号以及对应的节点编号
	 */
	public synchronized HashMap<UUID,ArrayList<Integer>> getFile(String fileName) {
		if(!fileMetadataList.containsKey(fileName))
			return null;
		StorageFileMetadata sfm = this.fileMetadataList.get(fileName);
		HashMap<UUID,StorageFileBlockMetadata> blocks = sfm.getBlocks();
		ArrayList<StorageFileBlockMetadata> bs = (ArrayList<StorageFileBlockMetadata>) blocks.values();
		HashMap<UUID,ArrayList<Integer>> temp = new HashMap<UUID,ArrayList<Integer>>();
		int size = bs.size();
		for(int i=0; i<size;i++){
			StorageFileBlockMetadata tempBlock = bs.get(i);
			temp.put(tempBlock.getBlockID(), tempBlock.getNodePort());
		}
		return temp;
	}

	/**
	 * 
	 * @param fileName
	 * @return 被删除的文件的所有块号以及所在节点
	 */
	public synchronized HashMap<UUID,ArrayList<Integer>> deleteFile(String fileName) {
		if(!fileMetadataList.containsKey(fileName))
			return null;
		StorageFileMetadata sfm = this.fileMetadataList.get(fileName);
		HashMap<UUID,StorageFileBlockMetadata> blocks = sfm.getBlocks();
		//获取了该文件的所有block元数据
		ArrayList<StorageFileBlockMetadata> bs = (ArrayList<StorageFileBlockMetadata>) blocks.values();
		HashMap<UUID,ArrayList<Integer>> temp = new HashMap<UUID,ArrayList<Integer>>();
		int size = bs.size();
		for(int i=0; i<size;i++){
			StorageFileBlockMetadata tempBlock = bs.get(i);
			//获得每一个block的id
			UUID blockID = tempBlock.getBlockID();
			//该block所在的存储节点的端口
			ArrayList<Integer> tempNodePorts = tempBlock.getNodePort();
			//记录被删除数据的块号及所在节点端口
			temp.put(blockID, tempNodePorts);
			//删除记录该块的所有节点的相关元数据
			for(int tempPort : tempNodePorts){
				StorageNode tempNode = this.nodeList.get(tempPort);
				tempNode.deleteBlock(blockID);
				this.nodeList.put(tempPort, tempNode);
			}
		}
		//删除该文件的元数据
		this.fileMetadataList.remove(fileName);
		return temp;
	}

	public synchronized boolean existFile(String fileName) {
		return this.fileMetadataList.containsKey(fileName);
	}

	public synchronized int sizeFile(String fileName) {
		StorageFileMetadata tempFile = this.fileMetadataList.get(fileName);
		if(tempFile==null)
			return 0;
		return tempFile.getFileSize();
	}

	public void mkdir() {

	}

	public void delDir() {

	}

	public void list() {

	}

	public synchronized HashMap<UUID,ArrayList<Integer>> append(String fileName,int blockSize) {
		if(!this.fileMetadataList.containsKey(fileName))
			return null;
		if(nodeList.isEmpty())
			return null;
		HashMap<UUID,ArrayList<Integer>> temp = new HashMap<UUID,ArrayList<Integer>>();
		ArrayList<Integer> tempNodes = new ArrayList<Integer>();
		//生成该块编号
		UUID blockID = UUID.randomUUID();
		//获得所有存储节点并按存储数据大小排序
		ArrayList<StorageNode> nodes = (ArrayList<StorageNode>)nodeList.values();
		Collections.sort(nodes, new Comparator<StorageNode>(){
			@Override
			public int compare(StorageNode arg0, StorageNode arg1) {
				// TODO Auto-generated method stub
				return arg0.getNodeSize()-arg1.getNodeSize();
			}
		});
		//获得最小的节点，
		StorageNode node1 = nodes.get(0);
		//创建新文件和新块
		StorageFileMetadata newFile = this.fileMetadataList.get(fileName);
		StorageFileBlockMetadata newBlock = new StorageFileBlockMetadata();
		//给该新块添加第一个存储节点的信息
		newBlock.addNodePort(node1.getPort());
		newBlock.setBlockID(blockID);
		newBlock.setBlockSize(blockSize);
		//如果存在多个存储节点，给该块添加第二个存储节点的信息
		if(nodes.size()>1) {
			StorageNode node2 = nodes.get(1);
			newBlock.addNodePort(node2.getPort());
		}
		//给新文件添加块
		newFile.addBlock(blockID, newBlock);
		//更新文件信息
		this.fileMetadataList.put(fileName, newFile);
		//在最小节点中添加该块并更新节点信息
		node1.addBlock(blockID, newBlock);
		this.nodeList.put(node1.getPort(), node1);
		tempNodes.add(node1.getPort());
		//如果存在，在次小的节点中添加该块信息并更新节点信息
		if(nodes.size()>1) {
			StorageNode node2 = nodes.get(1);
			node2.addBlock(blockID, newBlock);
			this.nodeList.put(node2.getPort(), node2);
			tempNodes.add(node2.getPort());
		}
		temp.put(blockID, tempNodes);
		return temp;
	}
}
