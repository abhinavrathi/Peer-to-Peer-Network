import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FileWriter;
import java.io.Writer;
import java.util.concurrent.TimeUnit;

public class BootstrapServer
{
	private static final String config="Bootstrap Configuration.txt";
	private static final int myPort = 9999; //The Bootstrap Server will listen on this port number
	public static void main (String[] args) throws Exception
	{
		//Variable Declarations
		Path newFile;
		ServerSocket bootstrap;

		System.out.println("Bootstrap Server is Running; Listening for Clients on Port: "+myPort);

		//Creating new Configuration file
		newFile = Paths.get(config);
		try
		{
			Files.deleteIfExists(newFile);
			newFile = Files.createFile(newFile);
		}
		catch (IOException ex)
		{
			System.out.println("Error creating file");
		}
		
		//Listening for Incoming Clients
		bootstrap=new ServerSocket(myPort);
		int clientNum=0;
		try
		{
            while(true)
            {
            	new ClientHandler(bootstrap.accept(),clientNum).start();
				System.out.println("Client "+clientNum+" is connected!");
				clientNum++;
            }
        }
        finally
        {
            bootstrap.close();
        }
	}

	private static class ClientHandler extends Thread
	{
		private Socket connection;
		private ObjectInputStream in;	   //stream read from the socket
        private ObjectOutputStream out;    //stream write to the socket
		private int no,i,j,k,l,flag,flagup,flagdown,upp,downn;
		char up,down,ch;
		private String temp,choice,menu,lineFromFile;
		Path newFile;
		public ClientHandler(Socket connection, int no)
		{
            this.connection = connection;
	    	this.no = no;
        }
        public void run()
        {
			try
			{
				//initialize Input and Output streams
				out = new ObjectOutputStream(connection.getOutputStream());
				out.flush();
				in = new ObjectInputStream(connection.getInputStream());
				menu="Enter 1 for File Owner\nEnter 2 for Peer\nEnter 3 for Exit\nEnter Your Choice : ";
				newFile = Paths.get(config);
				try
				{
					while(true)
					{
						sendMessage(Integer.toString(connection.getPort()));
						sendMessage(menu);
						choice=(String)in.readObject();
						ch=choice.charAt(0);
						
						if(ch=='1')			//Case if Client is File Owner
						{
							System.out.println("\nClient "+no+" is File Owner.");
							temp="Ok You are file owner";
							sendMessage(temp);
							System.out.println("Message Sent to Client " + no+": "+temp);
							
							//Writing to Configuration file
							try(Writer writer = new BufferedWriter(new FileWriter(config,true)))
							{
								writer.append(no+" fileowner "+connection.getPort()+"\n");
								writer.flush();
							}
							catch(IOException exception)
							{
								System.out.println("Error writing to file");
							}
						}
						else if(ch=='2')	//Case if Client is Peer
						{
							System.out.println("\nClient "+no+" is Peer.");
							temp="Ok You are peer";
							sendMessage(temp);
							System.out.println("Message Sent to Client " + no+": "+temp);
							
							//Assigning Ring Topology to Peers
							if(no==1)         
								temp=" 5 2";
							else if(no==2)
								temp=" 1 3";
							else if(no==3)
								temp=" 2 4";
							else if(no==4)
								temp=" 3 5";
							else if(no==5)
								temp=" 4 1";
							
							//Writing to Configuration file
							try(Writer writer = new BufferedWriter(new FileWriter(config,true)))
							{
								writer.append(no+" peer "+connection.getPort()+temp+"\n");
								writer.flush();
							}
							catch(IOException exception)
							{
								System.out.println("Error writing to file");
							}

							//Peer Requesting for File Owner's Port Number
							temp=(String)in.readObject();
							if(temp.equals("fileowner"))
							{
								System.out.println("Message Received from Client " + no+": Request for File Owner's Port Number.");
								try(BufferedReader reader = Files.newBufferedReader(newFile,Charset.defaultCharset()))
								{
									lineFromFile="";
									System.out.println("\nThe contents of Configuration file as of now are: ");
									while((lineFromFile = reader.readLine()) != null)
									{
										System.out.println(lineFromFile);
										if(lineFromFile.charAt(0)=='0')	//ID 0 is for File Owner
										{
											temp=lineFromFile.substring(lineFromFile.lastIndexOf(' ')+1,lineFromFile.length());
										}
									}
									sendMessage(temp);
									System.out.println("\nMessage Sent to Client " + no+": "+temp+" (File Owner's Listening Port)");
								}
								catch(IOException exception)
								{
									System.out.println("Error while reading file");
								}
							}

							//Peer Requesting for Upload and Download Neighbour's Port Numbers
							temp=(String)in.readObject();
							i=0;
							if(temp.equals("updown"))
							{
								System.out.println("Message Received from Client " + no+": Request for Upload & Download Neighbour ports.");
								try(BufferedReader reader1 = Files.newBufferedReader(newFile,Charset.defaultCharset()))
								{
									String lineFromFile="";
									System.out.println("\nThe contents of Configuration file as of now are: ");
									while((lineFromFile = reader1.readLine()) != null)
									{
										System.out.println(lineFromFile);
										j=(int)(lineFromFile.charAt(0))-48;
										if(j==no)
										{
											i=lineFromFile.lastIndexOf(' ');
											temp=lineFromFile.substring(0,i-1);
											i=temp.lastIndexOf(' ');
											up=lineFromFile.charAt(i+1);
											down=lineFromFile.charAt(i+3);
											flag=1;
										}
									}
									if(flag==1)
									{
										System.out.println("\nClient "+no+"'s Upload Neighbour ID: "+up+"; Download Neighbour ID: "+down);
										temp=new StringBuilder().append(up).toString();
										sendMessage(temp);
										temp=new StringBuilder().append(down).toString();
										sendMessage(temp);
									}
									else
									{
										System.out.println("No Entry found yet!");
									}
								}
								catch(IOException exception)
								{
									System.out.println("Error while reading file");
								}

								flagup=0;
								flagdown=0;
								flag=0;
								while(flagup==0||flagdown==0)
								{
									TimeUnit.SECONDS.sleep(1);
									try(BufferedReader reader2 = Files.newBufferedReader(newFile,Charset.defaultCharset()))
									{
										String lineFromFile = "";
										while((lineFromFile = reader2.readLine()) != null)
										{
											j=(int)(lineFromFile.charAt(0))-48;
											k=up-48;
											l=down-48;
											if(j==k)
											{
												i=lineFromFile.lastIndexOf(' ');
												temp=lineFromFile.substring(0,i-1);
												i=temp.lastIndexOf(' ');
												temp=lineFromFile.substring(0,i);
												i=temp.lastIndexOf(' ');
												temp=lineFromFile.substring(temp.lastIndexOf(' ')+1,temp.length());
												upp=Integer.parseInt(temp);
												System.out.println("Upload Neighbour's Port for Client "+no+": "+upp);
												flagup=1;
											}
											else if(j==l)
											{
												i=lineFromFile.lastIndexOf(' ');
												temp=lineFromFile.substring(0,i-1);
												i=temp.lastIndexOf(' ');
												temp=lineFromFile.substring(0,i);
												i=temp.lastIndexOf(' ');
												temp=lineFromFile.substring(temp.lastIndexOf(' ')+1,temp.length());
												downn=Integer.parseInt(temp);
												System.out.println("Download Neighbour's Port for Client "+no+": "+downn);
												flagdown=1;
											}
										}
									}
									catch(IOException exception)
									{
										System.out.println("Error while reading file");
									}
									if(flagup==1||flagdown==1)
									{
										System.out.println("\nClient "+no+"'s Upload Neighbour Port: "+upp+"; Download Neighbour Port: "+downn+"\n");
										temp=new StringBuilder().append(upp).toString();
										sendMessage(temp);
										temp=new StringBuilder().append(downn).toString();
										sendMessage(temp);
									}	
								}
							}
							System.out.println("Client "+no+" Disconnected!\n");
						}
						else if(ch=='3')	//Case for Exit
						{
							System.out.println("Client "+no+" Disconnected!\n");
							temp="exit";
							sendMessage(temp);
							break;
						}
						else				//Case for Wrong Input
						{
							System.out.println("Invalid input from Client "+no);
							temp="Not OK, Invalid Input";
							sendMessage(temp);
							continue;
						}
					}
				}
				catch(Exception e){;}	
			}
			catch(IOException ioException)
			{
				System.out.println("Disconnect with Client "+no);
			}
			finally
			{
				//Close connections
				try
				{
					in.close();
					out.close();
					connection.close();
				}
				catch(IOException ioException)
				{
					System.out.println("Disconnect with Client "+no);
				}
			}
		}

		//Function to Send Messages to Output Socket Stream
		public void sendMessage(String msg)
		{
			try
			{
				out.writeObject(msg);
				out.flush();
			}
			catch(IOException ioException)
			{
				ioException.printStackTrace();
			}
		}
	}//End of ClientHandler CLass
}//End of BootstrapServer Class