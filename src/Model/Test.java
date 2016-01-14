package Model;

public class Test
{

	public static void main(String[] args)
	{
		
//		Server server = new Server();
//		server.openServerSockets(55555);
//		
//		System.out.println("test");
//		
//		server.receiveMessage();
//		System.out.println("test2");
//		server.receiveMessage();
		
		
		Client client = new Client();
		client.openClientSocket("172.18.10.24", 55555);
		
		client.sendMessage("Coucou me voilà!");
	
		
		try
		{
			Thread.sleep(5000);
		} catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		client.sendMessage("Me revoila");
		

	}

}
