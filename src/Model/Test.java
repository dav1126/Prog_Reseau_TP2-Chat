package Model;

public class Test
{

	public static void main(String[] args)
	{
		Server server = new Server();
		server.openServerSockets(55555);
		
		Client client = new Client();
		client.openClientSocket("172.18.10.24", 55555);
		
		client.sendMessage("Coucou me voilà!");

	}

}
