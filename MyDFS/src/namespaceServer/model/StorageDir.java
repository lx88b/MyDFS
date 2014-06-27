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
	 * @return ���ر�ɾ����Ŀ¼������ִ��ɾ��Ŀ¼���ļ�
	 */
	public StorageDir deleteDir(ArrayList<String> dirNames) {
		//���Ҫɾ����Ŀ¼
		StorageDir deletedDir = this.getFinalDir(dirNames);
		if(deletedDir!=null) {
			int dirNum = dirNames.size();
			dirNames.remove(dirNum-1);
			//���Ҫɾ����Ŀ¼���ϼ�Ŀ¼
			StorageDir secondDir = this.getFinalDir(dirNames);
			//ɾ��Ŀ¼��¼
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
