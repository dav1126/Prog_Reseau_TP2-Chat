package Model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javafx.application.Platform;

public class Client
{
	Socket clientSocket;
	private static final int MAX_TRANSMISSION_BYTE_SIZE = 10000000;
	private static final String FILE_TRANSMISSION_ALERT_MSG = 
			"NHRTYFHAPWLM*?DYXN!848145489WJD23243212owahAwfligLOP)(* ALPHA";

	public void openClientSocket(String remoteIpAddress, int remotePort)
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
			clientSocket = new Socket(adress, remotePort);
		} catch (IOException e)
		{
			System.out.println("Cannot open client socket");
			e.printStackTrace();
		}
	}

	public void sendMessage(String message)
	{
		try
		{
			OutputStream output = clientSocket.getOutputStream();
			output.write(message.getBytes());
		} catch (IOException e)
		{
			System.out.println("Message could not be sent");
			e.printStackTrace();
		}
	}

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
			System.out
					.println("Could not close client socket: clientSocket is null");
		}
	}

	/**
	 * Calls the appropriate functions to send a file to the server
	 * @param fileToSend 
	 */
	public void startSendFile(File fileToSend)
	{
		try
		{
			sendFileTransmissionAlert();
			sendFileName(fileToSend);
			sendFile(fileToSend);	
		} 
		catch (IOException e)
		{
			System.out.println("File could not be sent");
			e.printStackTrace();
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
			byte[] fileTransmissionCode = new String(
					FILE_TRANSMISSION_ALERT_MSG).getBytes();
			output.write(fileTransmissionCode);
			
			//Wait for a receipt confirmation from the server
			InputStream bIStream = clientSocket.getInputStream();
			
			byte[] byteBuffer = new byte[MAX_TRANSMISSION_BYTE_SIZE];
			int count = bIStream.read(byteBuffer);
			String msgInput = "";
			
			
			for (int i = 0; i < count ; i++)
				{
					msgInput += (char)byteBuffer[i];
				}
			
			System.out.println("Received Message from Server: " + msgInput);
	}
	
	/**
	 * Send the name of the file to the server, then wait for a confirmation
	 * from the server
	 */
	private void sendFileName(File fileToSend) throws IOException
	{
		//Send the name of the file
		OutputStream output = clientSocket.getOutputStream();
		byte[] nameOfFile = new String(fileToSend.getName()).getBytes();
		output.write(nameOfFile);
	
		//Wait for a receipt confirmation from the server
		InputStream bIStream = clientSocket.getInputStream();
		
		byte[] byteBuffer = new byte[MAX_TRANSMISSION_BYTE_SIZE];
		int countName = bIStream.read(byteBuffer);
		String msgInput = "";
		
		
		for (int i = 0; i < countName ; i++)
			{
				msgInput += (char)byteBuffer[i];
			}
		
		System.out.println("Received Message from Server: " 
							+ msgInput);
	}
	//L'ERREUR EST ICIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII!!!!!!!!!!!!!!JE RENVOIE LE filetosent.getname()!!!!!
	/**
	 * Send the file to the server
	 */
	private void sendFile(File fileToSend) throws IOException
	{
		//Send the file
		OutputStream output = clientSocket.getOutputStream();
		InputStream input = new FileInputStream(fileToSend);
		byte[] fileByteArray = new byte[MAX_TRANSMISSION_BYTE_SIZE];
		
		int count;
		while((count = input.read(fileByteArray)) > 0)
		{
			output.write(fileByteArray, 0, count);
		}
	
		//Wait for a receipt confirmation from the server
		InputStream bIStream = clientSocket.getInputStream();
		
		byte[] byteBuffer = new byte[MAX_TRANSMISSION_BYTE_SIZE];
		int countName = bIStream.read(byteBuffer);
		String msgInput = "";
		
		
		for (int i = 0; i < countName ; i++)
			{
				msgInput += (char)byteBuffer[i];
			}
		
		System.out.println("Received Message from Server: " 
							+ msgInput);
		input.close();
	}
}
