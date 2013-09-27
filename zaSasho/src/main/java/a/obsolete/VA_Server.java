package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   VA_Server.java

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;


public class VA_Server {

	public VA_Server(int i, int j) {
		ts_thread = new Hashtable();
		VoronoiPort = i;
		ControlPort = j;
	}

	public static void main(String args[]) {
		int j = DEFAULT_VPORT;
		int k = DEFAULT_CPORT;
		for (int i = 0; i < args.length - 1; i++)
			if (args[i].compareTo("-vport") == 0)
				try {
					j = Integer.parseInt(args[i + 1]);
				} catch (NumberFormatException _ex) {
					System.err.println("Wrong number format for Voronoi port.");
					return;
				}
			else if (args[i].compareTo("-cport") == 0)
				try {
					k = Integer.parseInt(args[i + 1]);
				} catch (NumberFormatException _ex) {
					System.err.println("Wrong number format for Control port.");
					return;
				}

		(new VA_Server(j, k)).start();
	}

	public void start() {
		try {
			serverVSocket = new ServerSocket(VoronoiPort);
		} catch (IOException _ex) {
			System.err.println("Server could not create server socket for VoronoiApplet.");
			return;
		}
		try {
			dWSocket = new DatagramSocket(ControlPort);
		} catch (IOException _ex) {
			System.err.println("Server could not create datagram socket for Whiteboard.");
			return;
		}
		tc_thread = connectToWB(dWSocket);
		do {
			if (num_connected < MAX_SERVER_THREADS) {
				VA_ServerThread va_serverthread = connectToVA(serverVSocket);
				if (va_serverthread != null) {
					String s = insert_thread(va_serverthread);
					num_connected++;
					String s1 = VA_Message.compose_cmd("REGISTER", s);
					toVoronoi(s, s1);
				}
			} else {
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException _ex) {
				}
			}
			check();
		} while (true);
	}

	protected void finalize() throws Throwable {
		cleanup();
		try {
			serverVSocket.close();
			dWSocket.close();
		} catch (IOException _ex) {
		}
		serverVSocket = null;
		super.finalize();
	}

	protected VA_ServerThread connectToVA(ServerSocket serversocket) {
		Socket socket = null;
		VA_ServerThread va_serverthread = null;
		System.out.println("Wait for " + serversocket);
		try {
			socket = serversocket.accept();
		} catch (IOException ioexception) {
			System.err.println("Accept failed.");
			ioexception.printStackTrace();
			return null;
		}
		System.out.println("Connected to VoronoiApplet at " + socket);
		try {
			va_serverthread = new VA_ServerThread(socket, this);
			va_serverthread.start();
		} catch (Exception exception) {
			System.err.println("Couldn't create VA_ServerThread:");
			exception.printStackTrace();
			return null;
		}
		return va_serverthread;
	}

	protected synchronized VA_ControlThread connectToWB(DatagramSocket datagramsocket) {
		VA_ControlThread va_controlthread;
		try {
			va_controlthread = new VA_ControlThread(datagramsocket, this);
			va_controlthread.start();
		} catch (Exception exception) {
			System.err.println("Couldn't create VA_ControlThread:");
			exception.printStackTrace();
			return null;
		}
		return va_controlthread;
	}

	synchronized void check() {
		for (Enumeration enumeration = ts_thread.elements(); enumeration.hasMoreElements();) {
			VA_ServerThread va_serverthread = (VA_ServerThread) enumeration.nextElement();
			if (va_serverthread.is == null || va_serverthread.os == null || va_serverthread.socket == null)
				remove_thread(va_serverthread.key);
		}

	}

	synchronized void cleanup() {
		VA_ServerThread va_serverthread;
		for (Enumeration enumeration = ts_thread.elements(); enumeration.hasMoreElements(); cleanup(va_serverthread))
			va_serverthread = (VA_ServerThread) enumeration.nextElement();

		ts_thread.clear();
	}

	synchronized void cleanup(VA_ServerThread va_serverthread) {
		System.out.println("cleanup thread " + va_serverthread);
		if (va_serverthread != null)
			va_serverthread.cleanup();
	}

	public void toVoronoi(String s, String s1) {
		VA_ServerThread va_serverthread = get_thread(s);
		if (va_serverthread == null) {
			System.err.println("No VA_ServerThread for " + s);
			return;
		}
		String s2 = VA_Message.compose_message(s1);
		synchronized (va_serverthread) {
			try {
				writeToStream(s2, va_serverthread.os);
			} catch (NullPointerException _ex) {
				System.err.println("No VA_ServerThread #" + s);
			}
		}
	}

	void writeToStream(String s, DataOutputStream dataoutputstream) {
		System.out.println("Write to stream: " + s);
		try {
			dataoutputstream.writeUTF(s);
			dataoutputstream.flush();
			return;
		} catch (IOException ioexception) {
			System.err.println("VA_Server failed to forward string:");
			ioexception.printStackTrace();
			return;
		} catch (NullPointerException _ex) {
			System.err.println("VA_Server can't forward string since output stream is null.");
		}
	}

	public synchronized String insert_thread(VA_ServerThread va_serverthread) {
		va_serverthread.key = va_serverthread.socket.getInetAddress().toString();
		ts_thread.put(va_serverthread.key, va_serverthread);
		return va_serverthread.key;
	}

	public synchronized VA_ServerThread get_thread(String s) {
		return (VA_ServerThread) ts_thread.get(s);
	}

	public synchronized void remove_thread(String s) {
		System.out.println("Remove Server Thread #" + s);
		ts_thread.remove(s);
		System.out.println("Active Threads:");
		for (Enumeration enumeration = ts_thread.elements(); enumeration.hasMoreElements(); System.out
				.println("Server Thread: " + (VA_ServerThread) enumeration.nextElement()))
			;
		num_connected--;
	}

	int VoronoiPort;
	static int DEFAULT_VPORT = 12987;
	int ControlPort;
	static int DEFAULT_CPORT = 12989;
	static int MAX_SERVER_THREADS = 10;
	ServerSocket serverVSocket;
	DatagramSocket dWSocket;
	byte buffer[];
	Hashtable ts_thread;
	int next_thread;
	int num_connected;
	VA_ControlThread tc_thread;

}
