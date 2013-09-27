package a.obsolete;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   VA_ServerThread.java

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;


class VA_ServerThread extends Thread {

	public String toString() {
		return "VA_ServerThread(" + key + "): socket = " + socket + "; is = " + is + "; os = " + os;
	}

	VA_ServerThread(Socket socket1, VA_Server va_server) throws IOException {
		super("VA_Server");
		is = new DataInputStream(socket1.getInputStream());
		os = new DataOutputStream(socket1.getOutputStream());
		if (is == null) {
			System.err.println("VA_ServerThread: Input stream seemed to be created successfully, but it's null.");
			throw new IOException();
		}
		if (os == null) {
			System.err.println("VA_ServerThread: Output stream seemed to be created successfully, but it's null.");
			throw new IOException();
		} else {
			socket = socket1;
			server = va_server;
			return;
		}
	}

	public void run() {
		while (socket != null)
			try {
				is.readUTF();
			} catch (EOFException _ex) {
				cleanup();
				server.remove_thread(key);
				return;
			} catch (NullPointerException _ex) {
				cleanup();
				server.remove_thread(key);
				return;
			} catch (IOException _ex) {
				cleanup();
				server.remove_thread(key);
				return;
			} catch (Exception exception) {
				System.err.println("Exception on is.readUTF():");
				exception.printStackTrace();
				cleanup();
				server.remove_thread(key);
				return;
			}
	}

	protected void finalize() throws Throwable {
		cleanup();
		super.finalize();
	}

	void cleanup() {
		try {
			if (is != null) {
				is.close();
				is = null;
			}
		} catch (Exception _ex) {
		}
		try {
			if (os != null) {
				os.close();
				os = null;
			}
		} catch (Exception _ex) {
		}
		try {
			if (socket != null) {
				socket.close();
				socket = null;
				return;
			}
		} catch (Exception _ex) {
		}
	}

	public Socket socket;
	public DataInputStream is;
	public DataOutputStream os;
	VA_Server server;
	public String key;
}
