package com.slavi.net;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class MyNioServer {

	static final int port = 1234;
	
	void doIt() throws Exception {
		Selector selector = Selector.open();
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		InetSocketAddress inetSocketAddress = new InetSocketAddress("localhost", port);
		serverChannel.socket().bind(inetSocketAddress);
		serverChannel.register(selector, SelectionKey.OP_ACCEPT | SelectionKey.OP_READ);
	}

	public static void main(String[] args) throws Exception {
		new MyNioServer().doIt();
		System.out.println("Done.");
	}
}
