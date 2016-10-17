package com.slavi.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// https://examples.javacodegeeks.com/core-java/nio/java-nio-socket-example/
public class NioSocketExample {

	static class SocketServerExample {
		private Selector selector;
		private Map<SocketChannel, List<byte[]>> dataMapper;
		private InetSocketAddress listenAddress;

		public SocketServerExample(String address, int port) throws IOException {
			listenAddress = new InetSocketAddress(address, port);
			dataMapper = new HashMap<SocketChannel, List<byte[]>>();
		}

		// create server channel
		private void startServer() throws IOException {
			this.selector = Selector.open();
			ServerSocketChannel serverChannel = ServerSocketChannel.open();
			serverChannel.configureBlocking(false);

			// retrieve server socket and bind to port
			serverChannel.socket().bind(listenAddress);
			serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);

			System.out.println("Server started...");

			while (true) {
				// wait for events
				this.selector.select();

				// work on selected keys
				Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();
				while (keys.hasNext()) {
					SelectionKey key = (SelectionKey) keys.next();

					// this is necessary to prevent the same key from coming up
					// again the next time around.
					keys.remove();

					if (!key.isValid()) {
						continue;
					}

					if (key.isAcceptable()) {
						this.accept(key);
					} else if (key.isReadable()) {
						this.read(key);
					}
				}
			}
		}

		// accept a connection made to this channel's socket
		private void accept(SelectionKey key) throws IOException {
			ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
			SocketChannel channel = serverChannel.accept();
			channel.configureBlocking(false);
			Socket socket = channel.socket();
			SocketAddress remoteAddr = socket.getRemoteSocketAddress();
			System.out.println("Connected to: " + remoteAddr);

			// register channel with selector for further IO
			dataMapper.put(channel, new ArrayList<byte[]>());
			channel.register(this.selector, SelectionKey.OP_READ);
		}

		// read from the socket channel
		private void read(SelectionKey key) throws IOException {
			SocketChannel channel = (SocketChannel) key.channel();
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			int numRead = -1;
			numRead = channel.read(buffer);

			if (numRead == -1) {
				this.dataMapper.remove(channel);
				Socket socket = channel.socket();
				SocketAddress remoteAddr = socket.getRemoteSocketAddress();
				System.out.println("Connection closed by client: " + remoteAddr);
				channel.close();
				key.cancel();
				return;
			}

			byte[] data = new byte[numRead];
			System.arraycopy(buffer.array(), 0, data, 0, numRead);
			System.out.println("Got: " + new String(data));
		}
	}

	static class SocketClientExample {
		public void startClient() throws IOException, InterruptedException {
			InetSocketAddress hostAddress = new InetSocketAddress("localhost", 8090);
			SocketChannel client = SocketChannel.open(hostAddress);

			System.out.println("Client... started");

			String threadName = Thread.currentThread().getName();

			// Send messages to server
			String[] messages = new String[] { threadName + ": test1", threadName + ": test2", threadName + ": test3" };

			for (int i = 0; i < messages.length; i++) {
				byte[] message = new String(messages[i]).getBytes();
				ByteBuffer buffer = ByteBuffer.wrap(message);
				client.write(buffer);
				System.out.println(messages[i]);
				buffer.clear();
				Thread.sleep(5000);
			}
			client.close();
		}
	}

	public static void main(String[] args) throws Exception {
		Runnable server = new Runnable() {
			@Override
			public void run() {
				try {
					new SocketServerExample("localhost", 8090).startServer();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		};

		Runnable client = new Runnable() {
			@Override
			public void run() {
				try {
					new SocketClientExample().startClient();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		};
		new Thread(server).start();
		new Thread(client, "client-A").start();
		new Thread(client, "client-B").start();
	}
}
