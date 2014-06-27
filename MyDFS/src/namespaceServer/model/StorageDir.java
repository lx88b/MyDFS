package namespaceServer.model;

import java.util.ArrayList;
import java.util.HashMap;

public class StorageDir {
	private String dirName;
	private HashMap<String,StorageDir> dirList = new HashMap<String,StorageDir>();
	private HashMap<String,StorageFileMetadata> fileList = new HashMap<String,StorageFileMetadata>();
	public StorageDir(String name) {
		dirName = name;
	}
	public String getName() {
		return this.dirName;
	}
	public void addNewDir(StorageDir dir) {
		dirList.put(dir.getName(), dir);
	}
	public boolean containThisDir(String thisDirName) {
		return this.dirList.containsKey(thisDirName);
	}
	public StorageDir getDir(String name) {
		return dirList.get(name);
	}
	public void removeDir(String name) {
		dirList.remove(name);
	}
	public StorageDir getFinalDir(ArrayList<String> dirNames) {
		StorageDir tempDir=this;
		for(String tempName:dirNames) {
			if(!tempDir.containThisDir(tempName)) {
				tempDir=null;
				break;
			}
			tempDir = tempDir.getDir(tempName);
		}
		return tempDir;
	}
	
	/**
	 * 
	 * @param dirNames
	 * @return 返回被删除的目录，继续执行删除目录内文件
	 */
	public StorageDir deleteDir(ArrayList<String> dirNames) {
		//获得要删除的目录
		StorageDir deletedDir = this.getFinalDir(dirNames);
		if(deletedDir!=null) {
			int dirNum = dirNames.size();
			dirNames.remove(dirNum-1);
			//获得要删除的目录的上级目录
			StorageDir secondDir = this.getFinalDir(dirNames);
			//删除目录记录
			secondDir.removeDir(deletedDir.getName());
		}
		return deletedDir;
	}
	
	public boolean hasFile(String fileName) {
		return this.fileList.containsKey(fileName);
	}
	
	public boolean deleteFile(String fileName) {
		if(this.hasFile(fileName)) {
			this.fileList.remove(fileName);
			return true;
		}
		return false;
	}
	
	public boolean deleteFileWithPath(ArrayList<String> names, String fileName) {
		StorageDir dir = this.getFinalDir(names);
		if(dir==null)
			return false;
		return dir.deleteFile(fileName);
	}
	
	public boolean addDirWithPath(ArrayList<String> paths, String dirName) {
		StorageDir dir = this.getFinalDir(paths);
		if(dir==null)
			return false;
		if(dir.containThisDir(dirName))
			return false;
		dir.addNewDir(new StorageDir(dirName));
		return true;
	}
	
	public HashMap<String,ArrayList<String>> list(ArrayList<String> path) {
		StorageDir dir = this.getFinalDir(path);
		if(dir==null)
			return null;
		HashMap<String,ArrayList<String>> temp = new HashMap<String,ArrayList<String>>();
		ArrayList<String> files = new ArrayList<String>(dir.fileList.keySet());
		temp.put("file", files);
		ArrayList<String> dirs = new ArrayList<String>(dir.dirList.keySet());
		temp.put("dir", dirs);
		return temp;
	}

}
