package Model;

/**
 * G�re la partie client du programme
 */
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javafx.application.Platform;

public class Client
{
	/**
	 * Socket client utilis� pour les envoie de fichiers et message
	 */
	public Socket clientSocket;

	/**
	 * Socket UDP utilis� pour scanner le LAN pour detecter les utilisateurs
	 * online
	 */
	DatagramSocket UDPsocket;

	/**
	 * Taille amximale de transmission (10mo)
	 */
	private static final int MAX_TRANSMISSION_BYTE_SIZE = 10000000;

	/**
	 * Code indicateur de transmission de fichier
	 */
	private static final String FILE_TRANSMISSION_ALERT_MSG = 
			"NHRTYFHAPWLM*?DYXN!848145489WJD23243212owahAwfligLOP)(*ALPHA";

	/**
	 * Numero de socket UDP
	 */
	private static final int UDP_SOCKET_NUMBER = 5556;

	/**
	 * Numero de port du socket server distant
	 */
	private static final int SERVER_SOCKET_NUMBER = 5555;

	/**
	 * Code renvoy� par le serveur lorsqu'il ignore les broadcast de son propre
	 * client
	 */
	private static final String BROADCAST_ANSWER_IGNORE_CODE = 
			"OWIAJ*(&wa708hWAH(wauiwA&()8979790jdwOA!?";

	/**
	 * Code envoy� pour demander de d�marrer le chat a un utilisateur distant
	 */
	private static final String CHAT_REQUEST_CODE = 
			"Yhwa6WY6ywiob8W*0!90aw9898awWAJm(7(";

	/**
	 * Booleen indiquant si la demande de chat est accept�
	 */
	private boolean chatRequestAccepted = false;

	/**
	 * Pointeur vers le singleton ChatModel
	 */
	private ChatModel chatModel = ChatModel.getInstance();

	/**
	 * Objet servant a locker la liste observable de Ip atteignable
	 */
	Object lock = new Object();

	/**
	 * Open the client socket with the given remote ip address
	 * 
	 * @param remoteIpAddress
	 */
	public void openClientSocket(String remoteIpAddress)
	{
		Thread thread = new Thread(() ->
		{
			InetAddress adress = null;
			try
			{
				adress = InetAddress.getByName(remoteIpAddress);
			} catch (UnknownHostException e)
			{
				e.printStackTrace();
			}

			clientSocket = null;
			try
			{
				clientSocket = new Socket(adress, SERVER_SOCKET_NUMBER);
			} catch (IOException e)
			{
				System.out.println("Cannot open client socket");
				e.printStackTrace();
			}
		});
		thread.start();
	}

	/**
	 * Returns true if the chat request is accepted
	 * 
	 * @return
	 */
	public boolean isChatRequestAccepted()
	{
		return chatRequestAccepted;
	}

	/**
	 * Send the given message to the remote user
	 * 
	 * @param message
	 * @param remote
	 *            user username
	 */
	public void sendMessage(String message, String username)
	{
		Thread thread = new Thread(() ->
		{
			// Wait for the clientSocket to be openend
			// (this is done in another thread)
				while (clientSocket == null)
				{
					try
					{
						Thread.sleep(10);
					} catch (Exception e)
					{
						System.out.println("Send message thread sleep error");
						e.printStackTrace();
					}
				}
				try
				{
					OutputStream output = clientSocket.getOutputStream();
					output.write(message.getBytes());
					Platform.runLater(() -> chatModel.getChatMessagesList()
							.add(username + ": " + message));
				} catch (IOException e)
				{
					System.out.println("Message could not be sent");
					e.printStackTrace();
				}
			});
		thread.start();
	}

	/**
	 * Close the client socket
	 */
	public void closeClientSocket()
	{
		if (clientSocket != null)
		{
			try
			{
				clientSocket.close();
			} catch (IOException e)
			{
				System.out.println("Client socket could not be closed");
				clientSocket = null;
				e.printStackTrace();
			}
		} else
		{
			System.out.println("Could not close client socket");
		}
	}

	/**
	 * Calls the appropriate functions to send a file to the server
	 * 
	 * @param fileToSend
	 *            Path to the file to send
	 */
	public void startSendFile(String fileToSendPath)
	{
		File fileToSend = new File(fileToSendPath);
		if (fileToSend.length() <= MAX_TRANSMISSION_BYTE_SIZE)
		{
			Thread thread = new Thread(() ->
			{
				// Wait for the clientSocket to be openend
				// (this is done in another thread)
					while (clientSocket == null)
					{
						try
						{
							Thread.sleep(10);
						} catch (Exception e)
						{
							System.out.println("Send file thread sleep error");
							e.printStackTrace();
						}
					}
					try
					{
						sendFileTransmissionAlert();
						sendFileName(fileToSend);
						sendFile(fileToSend);
						Platform.runLater(() -> chatModel
								.getStatusMessagesList().add(
										"The file has been sent"));
					} catch (IOException e)
					{
						System.out.println("File could not be sent");
						e.printStackTrace();
					}
				});
			thread.start();
		} else
		{
			System.out.println("File is too big. 10 Mo maximum.");
		}
	}

	/**
	 * Send a message to the server that tells that the next transmission is
	 * gonna be a file, the wait for a confirmation form the server
	 */
	private void sendFileTransmissionAlert() throws IOException
	{
		// Send the message
		OutputStream output = clientSocket.getOutputStream();
		byte[] fileTransmissionCode = new String(FILE_TRANSMISSION_ALERT_MSG)
				.getBytes();
		output.write(fileTransmissionCode);

		// Wait for a receipt confirmation from the server
		InputStream bIStream = clientSocket.getInputStream();

		byte[] byteBuffer = new byte[MAX_TRANSMISSION_BYTE_SIZE];
		int count = bIStream.read(byteBuffer);
		String msgInput = "";

		for (int i = 0; i < count; i++)
		{
			msgInput += (char) byteBuffer[i];
		}

		System.out.println("Received Message from Server: " + msgInput);
	}

	/**
	 * Send the name of the file to the server, then wait for a confirmation
	 * from the server
	 * 
	 * @param fileToSend
	 *            File to send
	 */
	private void sendFileName(File fileToSend) throws IOException
	{
		// Send the name of the file
		OutputStream output = clientSocket.getOutputStream();
		byte[] nameOfFile = new String(fileToSend.getName()).getBytes();
		output.write(nameOfFile);

		// Wait for a receipt confirmation from the server
		InputStream bIStream = clientSocket.getInputStream();

		byte[] byteBuffer = new byte[MAX_TRANSMISSION_BYTE_SIZE];
		int countName = bIStream.read(byteBuffer);
		String msgInput = "";

		for (int i = 0; i < countName; i++)
		{
			msgInput += (char) byteBuffer[i];
		}

		System.out.println("Received Message from Server: " + msgInput);
	}

	/**
	 * Send the file to the server
	 * 
	 * @param fileToSend
	 *            File to send
	 */
	private void sendFile(File fileToSend) throws IOException
	{
		// Send the file
		OutputStream output = clientSocket.getOutputStream();
		InputStream input = new FileInputStream(fileToSend);
		byte[] fileByteArray = new byte[MAX_TRANSMISSION_BYTE_SIZE];

		int count;
		while ((count = input.read(fileByteArray)) > 0)
		{
			output.write(fileByteArray, 0, count);
		}

		// Wait for a receipt confirmation from the server
		InputStream bIStream = clientSocket.getInputStream();

		byte[] byteBuffer = new byte[MAX_TRANSMISSION_BYTE_SIZE];
		int countName = bIStream.read(byteBuffer);
		String msgInput = "";

		for (int i = 0; i < countName; i++)
		{
			msgInput += (char) byteBuffer[i];
		}

		System.out.println("Received Message from Server: " + msgInput);
		input.close();
	}

	/**
	 * Open the UDP socket in a thread
	 */
	public void openUDPSocket()
	{
		Thread thread = new Thread(() ->
		{
			try
			{
				UDPsocket = new DatagramSocket();
			} catch (SocketException e)
			{
				System.out.println("Client could not open UDP socket");
				e.printStackTrace();
			}
		});
		thread.start();
	}

	/**
	 * Broadcast a message for a specific port on the subnetwork, in a thread
	 *
	 */
	public void getRemoteUserAvailableForChat()
	{
		Thread thread = new Thread(
				() ->
				{
					// Wait for the UDPsocket to be openend (done by another
					// thread)
					while (UDPsocket == null)
					{
						try
						{
							Thread.sleep(10);
						} catch (Exception e)
						{
							System.out
									.println("Get remote ips thread sleep error");
							e.printStackTrace();
						}
					}

					// Get the LAN broadcast address
					InetAddress brodcastInetAddress = null;
					try
					{
						brodcastInetAddress = getLANBroadcastAddress();
					} catch (Exception e1)
					{
						e1.printStackTrace();
					}

					// Keep checking for new remote users
					while (true)
					{
						// Send a broadcast on the lan
						byte[] buffer = new byte[1];
						DatagramPacket brodcastPacket = new DatagramPacket(
								buffer, buffer.length, brodcastInetAddress,
								UDP_SOCKET_NUMBER);
						try
						{
							UDPsocket.send(brodcastPacket);
						} catch (IOException e)
						{
							System.out
									.println("Client could not send broadcast packet");
							e.printStackTrace();
						}
						System.out
								.println("Brodcast message sent from client.");

						// Wait for a response from a server, the server
						// responds with
						// the username specified by the user or an ignore code
						// if the
						// answer is from the program own server
						byte[] bufferResponse = new byte[1024];
						DatagramPacket responsePacket = new DatagramPacket(
								bufferResponse, bufferResponse.length);
						try
						{
							UDPsocket.receive(responsePacket);
						} catch (IOException e)
						{
							e.printStackTrace();
						}

						String responseFromServer = new String(responsePacket
								.getData()).trim();
						// If the server's response if not the ignore code
						if (!responseFromServer
								.equals(BROADCAST_ANSWER_IGNORE_CODE))
						{
							// Get the username sent by the server
							String username = new String(responsePacket
									.getData()).trim();

							// Get the ip address of the responding machine
							String remoteMachineIp = responsePacket
									.getAddress().getHostAddress();

							// if the ip of the server is not already in the
							// available for chat list
							if (!chatModel.getUserAvailableToChatMap()
									.containsKey(username))
							{
								// Add the remote machine ip address to the
								// available for chat list
								synchronized (lock)
								{
									Platform.runLater(() -> chatModel
											.getAvailableForChatUsersList()
											.add(username));
									Platform.runLater(() -> chatModel
											.getStatusMessagesList().add(
													"New online user detected: "
															+ username));
									chatModel.getUserAvailableToChatMap().put(
											username, remoteMachineIp);
								}
								System.out.println("Chat available with: "
										+ username);
							}
						}

						// Put the thread to sleep for 2 seconds
						try
						{
							Thread.sleep(2000);
						} catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				});
		thread.start();
	}

	/**
	 * Find and returns the DNS suffix of the. Source:
	 * http://stackoverflow.com/questions
	 * /6134790/how-do-i-access-the-connection-
	 * specific-dns-suffix-for-each-networkinterface-in
	 * 
	 * @return the dns suffix
	 */
	public String findDnsSuffix()
	{

		// First I get the hosts name
		// This one never contains the DNS suffix (Don't know if that is the
		// case for all VM vendors)
		String hostName = null;
		try
		{
			hostName = InetAddress.getLocalHost().getHostName().toLowerCase();
		} catch (UnknownHostException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Alsways convert host names to lower case. Host names are
		// case insensitive and I want to simplify comparison.

		// Then I iterate over all network adapters that might be of interest
		Enumeration<NetworkInterface> ifs = null;
		try
		{
			ifs = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (ifs == null)
			return ""; // Might be null

		for (NetworkInterface iF : Collections.list(ifs))
		{ // convert enumeration to list.
			try
			{
				if (!iF.isUp())
					continue;
			} catch (SocketException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (InetAddress address : Collections.list(iF.getInetAddresses()))
			{
				if (address.isMulticastAddress())
					continue;

				// This name typically contains the DNS suffix. Again, at least
				// on Oracle JDK
				String name = address.getHostName().toLowerCase();
				System.out.println(name);
				if (name.startsWith(hostName))
				{
					String dnsSuffix = name.substring(hostName.length());
					if (dnsSuffix.startsWith("."))
						return dnsSuffix;
				}
			}
		}

		return "";
	}

	/**
	 * Finds and returns the LAN Broadcast Address
	 * 
	 * @return lan broadcast address
	 * @throws SocketException
	 */
	public InetAddress getLANBroadcastAddress() throws SocketException
	{
		InetAddress broadcastAddress = null;

		mainloop: for (final Enumeration<NetworkInterface> interfaces = NetworkInterface
				.getNetworkInterfaces(); interfaces.hasMoreElements();)
		{
			final NetworkInterface cur = interfaces.nextElement();

			if (cur.isLoopback())
			{
				continue;
			}

			for (final InterfaceAddress addr : cur.getInterfaceAddresses())
			{
				final InetAddress inet_addr = addr.getAddress();

				if (!(inet_addr instanceof Inet4Address))
				{
					continue;
				}

				broadcastAddress = addr.getBroadcast();
				System.out.println("Broadcast address found: "
						+ broadcastAddress.getHostAddress());
				break mainloop;
			}
		}
		return broadcastAddress;
	}

	/**
	 * Sends a chat resquet to the remote user
	 * 
	 * @param username
	 *            this program's user username
	 * @param remoteUsername
	 *            remote user's username
	 */
	public void sendChatRequest(String username, String remoteUsername)
	{
		// Make sure the chatResquestAccepted flag is false
		chatRequestAccepted = false;

		Thread sendChatRequestThread = new Thread(() ->
		{
			// Wait for the clientSocket to be openend
			// (this is done in another thread)
				while (clientSocket == null)
				{
					try
					{
						Thread.sleep(10);
					} catch (Exception e)
					{
						System.out.println("Chat request thread sleep error");
						e.printStackTrace();
					}
				}

				try
				{
					// Send a chat request to the remote user
					OutputStream output = clientSocket.getOutputStream();
					String message = CHAT_REQUEST_CODE + username;
					output.write(message.getBytes());

					// Get an answer in another thread
					Platform.runLater(() -> chatModel.getStatusMessagesList()
							.add("Waiting 10 sec for answer..."));
					getAnswerToChatRequest(remoteUsername);

				} catch (IOException e)
				{
					System.out.println("Chat request could not be sent");
					e.printStackTrace();
				}
			});
		sendChatRequestThread.start();
	}

	/**
	 * Wait 10 seconds for an answer to the chat request from the remote user.
	 * Changes the boolean chatRequestAccepted depending on the answer
	 */
	private void getAnswerToChatRequest(String remoteUsername)
	{
		ExecutorService service = Executors.newSingleThreadExecutor();

		try
		{
			Runnable r = new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						InputStream bIStream = clientSocket.getInputStream();

						byte[] byteBuffer = new byte[MAX_TRANSMISSION_BYTE_SIZE];
						int count = bIStream.read(byteBuffer);
						String responseFromServer = "";

						for (int i = 0; i < count; i++)
						{
							responseFromServer += (char) byteBuffer[i];
						}

						if ((responseFromServer.trim()).equals("yes"))
						{
							chatRequestAccepted = true;
							Platform.runLater(() -> chatModel
									.getStatusMessagesList()
									.add("Chat resquest accepted by remote user."));
							chatModel.setConnectionEstablished(true);
							chatModel.setRemoteUsername(remoteUsername);
						} else
						{
							chatRequestAccepted = false;
							Platform.runLater(() -> chatModel
									.getStatusMessagesList()
									.add("Chat resquest refused by remote user."));
							closeClientSocket();
						}
					} catch (IOException e)
					{
						System.out
								.println("Error, client could not receive answer to chat request.");
						e.printStackTrace();
					}
				}
			};

			Future<?> f = service.submit(r);

			f.get(10, TimeUnit.SECONDS); // attempt the task for 10 seconds
		} catch (final InterruptedException e)
		{
			// The thread was interrupted during sleep, wait or join
			e.printStackTrace();
		} catch (final TimeoutException e)
		{
			// Took too long!
			System.out.println("Remote user didn't answer to the chat request");
			Platform.runLater(() -> chatModel.getStatusMessagesList().add(
					"No answer from remote user."));
			closeClientSocket();
		} catch (final ExecutionException e)
		{
			// An exception from within the Runnable task
			e.printStackTrace();
		} finally
		{
			service.shutdown();
		}
	}
}
