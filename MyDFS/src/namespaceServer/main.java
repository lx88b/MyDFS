package namespaceServer;

import namespaceServer.mySocket.DataOperationsThread;
import namespaceServer.mySocket.ProcessAccessThread;

public class main {

	public static void main(String[] args) {
		Thread dataOperationThread = new DataOperationsThread();
		Thread nodeOperationThread = new ProcessAccessThread();
		try {
			dataOperationThread.join();
			nodeOperationThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
