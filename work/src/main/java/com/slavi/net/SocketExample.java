package com.slavi.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

// https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
public class SocketExample {

	static class KnockKnockProtocol {
		private static final int WAITING = 0;
		private static final int SENTKNOCKKNOCK = 1;
		private static final int SENTCLUE = 2;
		private static final int ANOTHER = 3;

		private static final int NUMJOKES = 5;

		private int state = WAITING;
		private int currentJoke = 0;

		private String[] clues = { "Turnip", "Little Old Lady", "Atch", "Who", "Who" };
		private String[] answers = { "Turnip the heat, it's cold in here!", "I didn't know you could yodel!",
				"Bless you!", "Is there an owl in here?", "Is there an echo in here?" };

		public String processInput(String theInput) {
			String theOutput = null;

			if (state == WAITING) {
				theOutput = "Knock! Knock!";
				state = SENTKNOCKKNOCK;
			} else if (state == SENTKNOCKKNOCK) {
				if (theInput.equalsIgnoreCase("Who's there?")) {
					theOutput = clues[currentJoke];
					state = SENTCLUE;
				} else {
					theOutput = "You're supposed to say \"Who's there?\"! " + "Try again. Knock! Knock!";
				}
			} else if (state == SENTCLUE) {
				if (theInput.equalsIgnoreCase(clues[currentJoke] + " who?")) {
					theOutput = answers[currentJoke] + " Want another? (y/n)";
					state = ANOTHER;
				} else {
					theOutput = "You're supposed to say \"" + clues[currentJoke] + " who?\""
							+ "! Try again. Knock! Knock!";
					state = SENTKNOCKKNOCK;
				}
			} else if (state == ANOTHER) {
				if (theInput.equalsIgnoreCase("y")) {
					theOutput = "Knock! Knock!";
					if (currentJoke == (NUMJOKES - 1))
						currentJoke = 0;
					else
						currentJoke++;
					state = SENTKNOCKKNOCK;
				} else {
					theOutput = "Bye.";
					state = WAITING;
				}
			}
			return theOutput;
		}
	}

	public static void knockKnockClient(String hostName, String port) throws IOException {
		int portNumber = Integer.parseInt(port);

		try (Socket kkSocket = new Socket(hostName, portNumber);
				PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));) {
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			String fromServer;
			String fromUser;

			while ((fromServer = in.readLine()) != null) {
				System.out.println("Server: " + fromServer);
				if (fromServer.equals("Bye."))
					break;

				fromUser = stdIn.readLine();
				if (fromUser != null) {
					System.out.println("Client: " + fromUser);
					out.println(fromUser);
				}
			}
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + hostName);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + hostName);
			System.exit(1);
		}
	}

	static void knockKnockMultiServer(String port) throws IOException {
		int portNumber = Integer.parseInt(port);
		boolean listening = true;

		try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
			while (listening) {
				new KKMultiServerThread(serverSocket.accept()).start();
			}
		} catch (IOException e) {
			System.err.println("Could not listen on port " + portNumber);
			throw e;
		}
	}

	static class KKMultiServerThread extends Thread {
		private Socket socket = null;

		public KKMultiServerThread(Socket socket) {
			super("KKMultiServerThread");
			this.socket = socket;
		}

		public void run() {

			try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {
				String inputLine, outputLine;
				KnockKnockProtocol kkp = new KnockKnockProtocol();
				outputLine = kkp.processInput(null);
				out.println(outputLine);

				while ((inputLine = in.readLine()) != null) {
					outputLine = kkp.processInput(inputLine);
					out.println(outputLine);
					if (outputLine.equals("Bye"))
						break;
				}
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		switch (args.length) {
		case 1:
			knockKnockMultiServer(args[0]);
			break;
		case 2:
			knockKnockClient(args[0], args[1]);
			break;
		default:
			System.out.println("Usage:");
			System.out.println("run server: SocketExample <port number>");
			System.out.println("run client: SocketExample <server address> <port number>");
		}
	}
}
