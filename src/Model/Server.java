package Model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;

/**
 * G�re la partie serveur du programme
 * @author 0345162
 *
 */
public class Server
{
	/**
	 * UDP socket used to receive broadcast message from clients
	 */
	DatagramSocket UDPSocket;
	
	/**
	 * Client socket
	 */
	Socket clientSocket = null;
	
	/**
	 * Server socket
	 */
	ServerSocket serverSocket = null;
	
	/**
	 * Taille max des transmissions (10mo)
	 */
	private static final int MAX_TRANSMISSION_BYTE_SIZE = 10000000;
	
	/**
	 * Code indicateur de transmission de fichier
	 */
	private static final String FILE_TRANSMISSION_ALERT_MSG = 
			"NHRTYFHAPWLM*?DYXN!848145489WJD23243212owahAwfligLOP)(*ALPHA";
	
	/**
	 * Chemin d'Acc�s au repertoire de sauvegarde de fichier
	 */
	private static final String PATH_TO_FILE_DIRECTORY = "C:\\temp\\destination\\";
	
	/**
	 * Numero de port du socket UDP
	 */
	private static final int UDP_SOCKET_NUMBER = 5556;
	
	/**
	 * Numero de port du socket server
	 */
	private static final int SERVER_SOCKET_NUMBER = 5555;
	
	/**
	 * Code renvoy� par le serveur lorsqu'il recoit un broadcast de son propre 
	 * client
	 */
	private static final String BROADCAST_ANSWER_IGNORE_CODE = 
			"OWIAJ*(&wa708hWAH(wauiwA&()8979790jdwOA!?";
	
	/**
	 * Code de demande de chat
	 */
	private static final String CHAT_REQUEST_CODE = 
			"Yhwa6WY6ywiob8W*0!90aw9898awWAJm(7(";
	
	/**
	 * Proprit� bool�enne de demande de chat. D�clenche un listener dans le control.
	 */
	private BooleanProperty chatRequestedBooleanProperty = new SimpleBooleanProperty();
	
	/**
	 * Username du client demandant de d�marrer un chat
	 */
	private String chatRequestApplicantUsername;
	
	/**
	 * Ip du client demandant de d�marrer un chat
	 */
	private String chatRequestApplicantIp;
	
	/**
	 * Objet utilis� pour locker le thread de reception de message
	 */
	Object receiveMessageThreadLock = new Object();

	/**
	 * Boolean de demande de chat accept�e
	 */
	private boolean chatRequestAccepted =  false;
	
	/**
	 * Pointeur vers le singleton ChatModel
	 */
	private ChatModel chatModel = ChatModel.getInstance();
	
	/**
	 * Username du client distant
	 */
	private String remoteUsername;

	/**
	 * Opens the server's sockets and establish a wait to establish a remote
	 * connection with a client
	 */
	public void openServerSockets()
	{
		Thread thread =  new Thread(() ->
		{
			
			try
			{
				serverSocket = new ServerSocket(SERVER_SOCKET_NUMBER);
				clientSocket = serverSocket.accept();
			} 
			catch (IOException e)
			{
				System.out.println("Could not open server's sockets");
				e.printStackTrace();
			}
		});
		thread.start();
	}
	
	/**
	 * Wait for a message to come through the channel, then returns it.
	 * It is synchronized because it if the message received is the file 
	 * transmission code, it must wait for a second message to come through the 
	 * channel (the file) before to give access to another thread that would 
	 * wait for a normal message.
	 * @return message that came through the channel
	 */
	private synchronized void receiveMessage() throws IOException
	{
		String msgInput = null;
		
			InputStream bIStream = clientSocket.getInputStream();
			
			byte[] byteBuffer = new byte[MAX_TRANSMISSION_BYTE_SIZE];
			int count = bIStream.read(byteBuffer);
			msgInput = "";
			
			
			for (int i = 0; i < count ; i++)
				{
					msgInput += (char)byteBuffer[i];
					//System.out.println((char)byteBuffer[i]);
					//System.out.println(msgInput);
				}
			
			System.out.println("Received Message" + msgInput);
			
			//If the received message is the file transmission alert message
			if (msgInput.compareTo(FILE_TRANSMISSION_ALERT_MSG) == 0)
			{
				//Confirm the reception of the file transmission alert t
				//the client
				OutputStream bOStream = clientSocket.getOutputStream();
				String msg = "File transmission alert received. Waiting for file name...";
				bOStream.write(msg.getBytes());
				bOStream.flush();
				
				//Receive the file
				startReceiveFile();
				//Make this thread wait for the file reception to be over
				synchronized (receiveMessageThreadLock)
				{
					try
					{
						receiveMessageThreadLock.wait();
					} catch (InterruptedException e)
					{
						System.out.println("Server receive mesage thread wait error.");
						e.printStackTrace();
					}
				}
			}
			
			//If the received message is the start chat request code
			else if (msgInput.contains(CHAT_REQUEST_CODE))
			{
				chatRequestApplicantUsername = msgInput.substring(CHAT_REQUEST_CODE.length());
				chatRequestApplicantIp = clientSocket.getInetAddress().getHostAddress();
				System.out.println("Server detected a chat request from :" + chatRequestApplicantUsername + "  " + chatRequestApplicantIp);
				
				//This boolean property change triggers a listener that pops an alert asking the user to 
				//accept or refuse the chat request (see in ControllerFXMLApplication class).
				//The user choice changes the chatRequestAccepted attribute
				chatRequestedBooleanProperty.setValue(true);
				
				//Make this thread wait for an answer from the user
				synchronized (receiveMessageThreadLock)
				{
					try
					{
						receiveMessageThreadLock.wait();
					} catch (InterruptedException e)
					{
						System.out.println("Server receive mesage thread wait error.");
						e.printStackTrace();
					}
				}
				
				//Prepare the answer to the request base on the choice of the user
				String answer = "no";
				if (chatRequestAccepted)
				{
					answer = "yes";
					//Send the answer back to the request applicant
					OutputStream bOStream = clientSocket.getOutputStream();
					bOStream.write(answer.getBytes());
					bOStream.flush();
					chatModel.setConnectionEstablished(true);
				}
				else
				{
					//Send the answer back to the request applicant
					OutputStream bOStream = clientSocket.getOutputStream();
					bOStream.write(answer.getBytes());
					bOStream.flush();
					//Wait for another client connection
					clientSocket = null;
					clientSocket = serverSocket.accept();
				}
			}
			
			//if the message is empty, it means the remote host ended the connection
			else if(msgInput.equals(""))
			{
				chatModel.setConnectionEstablished(false);
				Platform.runLater(() -> ChatModel.getInstance().
						getStatusMessagesList().add
						("Chat ended: remote user disconnected."));
			}
			
			//else, it means that it is a normal chat message
			else
			{	
				final String chatMessage = msgInput;//final variable necessary to add to the ChatMessagesList
				Platform.runLater(() -> chatModel.getChatMessagesList().add(chatModel.getRemoteUsername() + ": " +chatMessage)); 
			}	
	}
	/**
	 * Creates and start a thread that wait for a message to come through the 
	 *  opened channel. 
	 */
	public void startReceiveMessageThread()
	{
		Thread thread =  new Thread(() ->
		{
			//Keep waiting for new messages
			try
			{
				while (true)
				{
					while (serverSocket == null || clientSocket == null || serverSocket.isClosed() || clientSocket.isClosed())
					{						
						try
						{
							Thread.sleep(10);
						} catch (Exception e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}				
					}
					receiveMessage();
				}
			}
			catch (IOException e)
			{
				//If the remote user closes it's socket
				if (e instanceof SocketException)
				{
					clientSocket = null;
					Platform.runLater(() -> ChatModel.getInstance().
							getStatusMessagesList().add
							("Chat ended: remote user disconnected."));
					chatModel.setConnectionEstablished(false);
				}
				else
				{
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}
	
	/**
	 * Create and start a thread that manage the reception of a file
	 */
	private void startReceiveFile()
	{
		Thread thread =  new Thread(() ->
		{
			//Wait for the sockets to be openend
			while (serverSocket == null || clientSocket == null)
			{
				try
				{
					Thread.sleep(10);
				} 
				catch (Exception e)
				{
					System.out.println("Server receive file thread sleep error");
					e.printStackTrace();
				}
			}
				String fileName = receiveFileName();
				receiveFile(fileName);
				
			//Notify the message thread that the file reception is done
			synchronized (receiveMessageThreadLock)
			{
					receiveMessageThreadLock.notify();
			}
		});
		thread.start();
	}
	
	/**
	 * Receive a file name from the client, the send a confirmation to the client
	 * @return the name of the file
	 */
	public String receiveFileName()
	{
		String fileName = null;
		
		try
		{
			InputStream bIStream = clientSocket.getInputStream();
			
			byte[] byteBuffer = new byte[MAX_TRANSMISSION_BYTE_SIZE];
			int count = bIStream.read(byteBuffer);
			fileName = "";
			
			
			for (int i = 0; i < count ; i++)
			{
				fileName += (char)byteBuffer[i];
			}
			
			//Confirm the reception of the file name to the client
			OutputStream bOStream = clientSocket.getOutputStream();
			String msg = "File name received. Waiting for file...";
			bOStream.write(msg.getBytes());
			bOStream.flush();
		}
			
		catch (IOException e)
		{
			System.out.println("Could not receive file.");
			e.printStackTrace();
		}
		
		return fileName;
	}
	
	/**
	 * Receive a file from the client, the send a confirmation to the client
	 */
	public void receiveFile(String fileName)
	{
		FileOutputStream fos = null;
		try
		{
			//Get the file
			InputStream bIStream = clientSocket.getInputStream();
			File filePath = new File(PATH_TO_FILE_DIRECTORY);
			filePath.mkdirs();
			File file = new File(PATH_TO_FILE_DIRECTORY, fileName);
			
			file.createNewFile();
			fos = new FileOutputStream(file);
			
			
			byte[] byteBuffer = new byte[MAX_TRANSMISSION_BYTE_SIZE];
			int count = bIStream.read(byteBuffer);
			
			for (int i = 0; i < count ; i++)
				{
					fos.write(byteBuffer[i]);
				}
			
			System.out.println("File received: " + file.getName());
			Platform.runLater(() -> 
			chatModel.getStatusMessagesList().add("File received: " + fileName));
			
			//Confirm the reception of the file to the client
			OutputStream bOStream = clientSocket.getOutputStream();
			String msg = "File transmission successful!";
			bOStream.write(msg.getBytes());
			bOStream.flush();
		}
		catch (IOException e)
		{
			System.out.println("Could not receive file.");
			e.printStackTrace();
		}
		finally
		{
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Opens a UDP socket that is used to wait for broadcast messages.
	 */
	public void openUDPSocket()
	{	
		Thread thread =  new Thread(() ->
		{
			try
			{
				UDPSocket = new DatagramSocket(UDP_SOCKET_NUMBER);
				UDPSocket.setBroadcast(true);
			} 
			catch (SocketException e)
			{
				System.out.println("UDP socket " + UDP_SOCKET_NUMBER +" could not be openend." );
				e.printStackTrace();
			}
		});
		thread.start();
	}
	
	/**
	 * Receive broadcast messages from remote clients, and send back a username
	 * Used to be be shown as online to other users. Repeated
	 * each for refresh. 
	 */
	public void receiveBroadcastRequests(String username)
	{
		
		Thread thread =  new Thread(() ->
		{
			//Wait for the UDPSocket to be opened
			while (UDPSocket == null)
			{
				try
				{
					Thread.sleep(10);
				} catch (Exception e)
				{
					System.out.println("Receive broadcast thread sleep error");
					e.printStackTrace();
				}
			}
			boolean broadcastRequestDetected = true;
			
			String lanIPAddress = null;
			try
			{
				lanIPAddress = getLANIPAddress();
			} catch (Exception e1)
			{
				e1.printStackTrace();
			}
			
			//Keep the server waiting for broadcast request
			while (true)
			{
				//Buffer to receive the broadcast request
				byte[] buffer = new byte[1];
				DatagramPacket remoteHostPacket = new DatagramPacket(buffer, buffer.length);
				try
				{
					UDPSocket.receive(remoteHostPacket);
					String remoteIpAddress = remoteHostPacket.getAddress().getHostAddress();
					System.out.println( "BroadCast request from: " + remoteIpAddress);
					//If the broadcast message is from a remote client
					if (!remoteIpAddress.equals(lanIPAddress))
					{
						//Send a positive response to the remote client
						byte[] bufferResponse = username.getBytes();
						DatagramPacket responsePacket = new DatagramPacket
								(bufferResponse, bufferResponse.length, 
										remoteHostPacket.getAddress(), 
										remoteHostPacket.getPort());
						UDPSocket.send(responsePacket);
						System.out.println( "BroadCast answer sent to: " + remoteIpAddress);
					}
					//If the broadcast message is from this program's own client
					else
					{	
						//Send an ignore response to this programs own  client
						byte[] bufferResponse = 
								BROADCAST_ANSWER_IGNORE_CODE.getBytes();
						DatagramPacket responsePacket = new DatagramPacket
								(bufferResponse, bufferResponse.length, 
										remoteHostPacket.getAddress(), 
										remoteHostPacket.getPort());
						UDPSocket.send(responsePacket);
						System.out.println
						("Broadcast answer ignore code sent to own client.");
					}
				}
				catch (IOException e)
				{
					System.out.println("UDP Packet reception failed");
					e.printStackTrace();
				}
			} 
		});
		
		thread.start();
		Platform.runLater(() -> ChatModel.getInstance().
				getStatusMessagesList().add
				("Server detecting online users..."));
	}
	
	/**
	 * Closes the server's sockets
	 */
	public void closeServerSockets()
	{
		if(serverSocket != null && clientSocket != null)
		{
			try
			{
				clientSocket.close();
				serverSocket.close();
			}
			catch (IOException e)
			{
				System.out.println("Could not close server sockets");
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Could not close servers sockets: server's sockets are null");
		}	
	}
	
	/**
	 * Closes the server's UDP socket
	 */
	public void closeUDPSocket()
	{
		if(UDPSocket != null)
		{		
				UDPSocket.close();
		}
		else
		{
			System.out.println("Could not close server UDP socket");
		}	
	}
	
	/**
	 * Creates and starts a thread that opens the server's sockets and wait
	 * for client to connect
	 */
	public void startOpenSocketThread()
	{
		Thread thread =  new Thread(() ->
		{
			openServerSockets();
		});
		thread.start();
	}
	
	/**
	 * Get this server LAN ip address
	 * @return
	 * @throws SocketException
	 */
	public String getLANIPAddress() throws SocketException
	{
		String lanAddress = null;
		
		mainloop:for (
			    final Enumeration< NetworkInterface > interfaces =
			        NetworkInterface.getNetworkInterfaces( );
			    interfaces.hasMoreElements( );
			)
			{
			    final NetworkInterface cur = interfaces.nextElement( );

			    if ( cur.isLoopback( ) )
			    {
			        continue;
			    }
			    
			    
			    for ( final InterfaceAddress addr : cur.getInterfaceAddresses( ) )
			    {
			        final InetAddress inet_addr = addr.getAddress( );

			        if ( !( inet_addr instanceof Inet4Address ) )
			        {
			            continue;
			        }
 
					lanAddress = inet_addr.getHostAddress();
			        System.out.println("Server LAN address found: " 
			        		+ lanAddress);
			        break mainloop;
			    }
			}
		return lanAddress;
	}
	
	/**
	 * Returns the chatRequestedBooleanProperty
	 * @return chatRequestedBooleanProperty
	 */
	public BooleanProperty getChatRequestedBooleanProperty()
	{
		return chatRequestedBooleanProperty;
	}

	/**
	 * Sets the chatRequestedBooleanProperty
	 * @param chatRequestedBooleanProperty
	 */
	public void setChatRequestedBooleanProperty(
			BooleanProperty chatRequestedBooleanProperty)
	{
		this.chatRequestedBooleanProperty = chatRequestedBooleanProperty;
	}
	
	/**
	 * Returns the chat applicant username
	 * @return chat applicant username
	 */
	public String getChatRequestApplicantUsername()
	{
		return chatRequestApplicantUsername;
	}
	
	/**
	 * Returns the chat applicant ip address
	 * @return chat applicant ip address
	 */
	public String getChatRequestApplicantIp()
	{
		return chatRequestApplicantIp;
	}
	
	/**
	 * Return true if the chat resquest is accepted by the server
	 * @return true or false
	 */
	public boolean isChatRequestAccepted()
	{
		return chatRequestAccepted;
	}

	/**
	 * Set the chatRequestAccepted boolean
	 * @param chatRequestAccepted
	 */
	public void setChatRequestAccepted(boolean chatRequestAccepted)
	{
		this.chatRequestAccepted = chatRequestAccepted;
	}
	
	/**
	 * Returns the Object used to lock the receive message thread
	 * @return receiveMessageThreadLock
	 */
	public Object getReceiveMessageThreadLock()
	{
		return receiveMessageThreadLock;
	}
}
