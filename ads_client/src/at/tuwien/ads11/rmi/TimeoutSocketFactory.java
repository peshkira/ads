package at.tuwien.ads11.rmi;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

public class TimeoutSocketFactory extends RMISocketFactory {

	private int timeout;
	
	public TimeoutSocketFactory(int timeout) {
		this.timeout = timeout;
	}
	
	@Override
	public ServerSocket createServerSocket(int port) throws IOException {
		return getDefaultSocketFactory().createServerSocket(port);
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException {
		Socket socket = getDefaultSocketFactory().createSocket(host, port);
		socket.setSoTimeout(timeout * 1000);
		return socket;
	}

}
