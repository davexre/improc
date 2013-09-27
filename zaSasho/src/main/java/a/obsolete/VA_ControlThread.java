package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   VA_ControlThread.java

import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


class VA_ControlThread extends Thread {

	public String toString() {
		return "VA_ControlThread: socket = " + socket;
	}

	VA_ControlThread(DatagramSocket datagramsocket, VA_Server va_server) throws IOException {
		super("VA_Server");
		socket = datagramsocket;
		server = va_server;
		buffer = new byte[1024];
	}

	public void run() {
		while (socket != null)
			try {
				DatagramPacket datagrampacket = new DatagramPacket(buffer, buffer.length);
				socket.receive(datagrampacket);
				String s = new String(buffer, 0, 0, datagrampacket.getLength());
				System.out.println("Received: <" + s + ">");
				String s1 = datagrampacket.getAddress().toString();
				System.out.println("key: " + s1);
				String s2 = s;
				System.out.println("Message: " + s2);
				if (s2 != null)
					if (s2.compareTo("TERM") == 0) {
						System.out.println("Terminating.");
						System.runFinalization();
						System.exit(0);
						System.out.println("???");
					} else {
						server.toVoronoi(s1, s2);
					}
			} catch (EOFException _ex) {
				cleanup();
				return;
			} catch (NullPointerException _ex) {
				cleanup();
				return;
			} catch (IOException _ex) {
				cleanup();
				return;
			} catch (Exception exception) {
				System.err.println("Exception on receive Packet:");
				exception.printStackTrace();
				cleanup();
				return;
			}
	}

	protected void finalize() throws Throwable {
		cleanup();
		super.finalize();
	}

	void cleanup() {
		try {
			if (socket != null) {
				socket.close();
				socket = null;
				return;
			}
		} catch (Exception _ex) {
		}
	}

	public DatagramSocket socket;
	public byte buffer[];
	VA_Server server;
}
