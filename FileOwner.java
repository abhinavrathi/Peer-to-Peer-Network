import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.lang.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.swing.JOptionPane;
import java.util.Arrays;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileOwner
{
	private Socket requestSocket;           //socket connect to the server
	private ObjectOutputStream out;         //stream write to the socket
 	private ObjectInputStream in;
	private String message;                //message send to the server
	private String MESSAGE;
	private int myPort;
	private String path="";
	private int splitSizeInKB=0;
	private long size;
	private int n=0,peerNum;
	private int chunks[];
	private BufferedReader bufferedReader;
	private ServerSocket fileOwner;
	private static final int ServerPort=9999;

 	public void FileOwner() {}

	void run()
	{
		try
		{
			//Create a socket to connect to Bootstrap Server
			requestSocket = new Socket("localhost",ServerPort);
			System.out.println("Connected to Bootstrap Server in port 9999");
			
			//Initialize InputStream, OutputStream and User Input
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
			bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			
			while(true)
			{
				MESSAGE = (String)in.readObject();
				myPort=Integer.parseInt(MESSAGE);
				MESSAGE = (String)in.readObject();
				System.out.print("\nMessage from Bootstrap Server:\n" + MESSAGE);
				message = bufferedReader.readLine();
				sendMessageToBootstrap(message);
				MESSAGE = (String)in.readObject();
				if(MESSAGE.equals("exit"))
				{
					System.out.println("Exiting Connection from Bootstrap Server!");
					break;
				}
				else
				{
					System.out.println("Message from Bootstrap Server: "+MESSAGE);

					//File Splitting and other details
		
					bufferedReader = new BufferedReader(new InputStreamReader(System.in));
					System.out.print("Enter file name to upload: ");
					try{path = bufferedReader.readLine();}
					catch(Exception e){;}
					File source=new File(path);
			    	System.out.println("Size of given file is: "+source.length()+" bytes");
    	
			    	System.out.print("Enter split size (in KBs) of given file: ");
					try{splitSizeInKB = Integer.parseInt(bufferedReader.readLine());}
					catch(Exception e){;}
					size=source.length();
					n=0;
					//Splitting File
					String[] destination;
			    	try
			    	{
			    		n=(int)Math.ceil((float)source.length()/1024/splitSizeInKB);
			    		String folder=new String("temp");
			    		new File(folder).mkdir();
			    		destination=new String[n];
			    		String fname=path.substring(0,path.lastIndexOf("."));
						FileInputStream src=new FileInputStream(source);
			    		File[] destinationFile=new File[n];
			    		for(int i=0;i<n;++i)
			    		{
			    			destination[i]=folder+"/"+fname+String.valueOf(i+1)+".random";
			    			destinationFile[i]=new File(destination[i]);
				    		FileOutputStream dst=new FileOutputStream(destinationFile[i]);
			    			byte[] data;
			    			if(i<n-1)
			    			{
			    				data = new byte[1024*splitSizeInKB];
			    			}
			    			else
			    			{
			    				data = new byte[(int)(source.length()-(n-1)*1024*splitSizeInKB)];
			    			}
			    			src.read(data);
			    			dst.write(data);
			    			dst.close();
			    		}
			            src.close();
			    	}
			    	catch (Exception e)
			    	{
			        	System.out.println("Error in Splitting"+e);
			            JOptionPane.showMessageDialog(null, "Error in Splitting File \n"+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			        }
			        chunks=new int[n];
			        System.out.print("File Split into following chunks: ");
			        for(int i=1;i<=n;++i)
			        {
			        	chunks[i-1]=i;
			        	System.out.print(chunks[i-1]+" ");
			        }
			        System.out.println();
				}
			}
		}
		catch (ConnectException e)
		{
    		System.err.println("Connection refused. You need to initiate a server first.");
		} 
		catch ( ClassNotFoundException e )
		{
            System.err.println("Class not found");
        } 
		catch(UnknownHostException unknownHost)
		{
			System.err.println("You are trying to connect to an unknown host!");
		}
		catch(IOException ioException)
		{
			ioException.printStackTrace();
		}
		finally
		{
			//Close connections
			try
			{
				in.close();
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException)
			{
				ioException.printStackTrace();
			}
		}

		//Starting Listening on My Port Number for Peers
		System.out.println("\nNow, my Port Number is: "+myPort+". Starting Listening on this for Peers!");
		try
		{
			fileOwner=new ServerSocket(myPort);
			peerNum=1;
			try
			{
            	while(true)
            	{
            		new PeerHandler(fileOwner.accept(),peerNum,path,size,chunks).start();
					System.out.println("\nPeer "  + peerNum + " is Connected!");
					peerNum++;
        	    }
        	}
        	finally
        	{
        	    fileOwner.close();
        	}
		}
		catch(Exception e)
		{

		}
	}

	//PeerHandler Class Starts
	private static class PeerHandler extends Thread
	{
		private Socket connection;
		private ObjectInputStream in;		//stream read from the socket
        private ObjectOutputStream out;   	//stream write to the socket
		private int no,n,peerchunks,i,j;
		private String temp;
		private String path;
		private int[] chunks;
		private int[] peerchunklist;
		private long size;
		private BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(System.in));
		private OutputStream os;
		public PeerHandler(Socket connection, int no, String path, long size, int[] chunks)
		{
            this.connection = connection;
	    	this.no = no;
	    	this.path=path;
	    	this.size=size;
	    	this.chunks=chunks;
        }
        public void run()
        {
			try
			{
				//Initialize Input and Output streams
				out = new ObjectOutputStream(connection.getOutputStream());
				out.flush();
				in = new ObjectInputStream(connection.getInputStream());
				
				try
				{
					sendMessageToPeer("Hello Peer "+no);
					System.out.println("Message to Peer "+no+": Hello, I am here!");	
					temp = (String)in.readObject();
					System.out.println("Message from Peer "+no+": "+temp);
					System.out.println("Message to Peer "+no+":\n"+"File Name: "+path+"\nFile Size: "+size+"\nFile Chunk List: "+Arrays.toString(chunks));
					sendMessageToPeer(path);
					sendMessageToPeer(""+size);
					sendMessageToPeer(Arrays.toString(chunks));
					temp = (String)in.readObject();
					System.out.println("Message from Peer "+no+": "+temp);
					n=chunks.length;					//Total Chunks
					peerchunks=n/5+(n%5>=no?1:0);		//Total Peer Chunks
					peerchunklist=new int[peerchunks];
					j=0;
					System.out.println("Message to Peer "+no+": Your Total Chunks are "+peerchunks+" nos. having Chunk IDs: ");
					for(i=no-1;i<peerchunks*5;i+=5)
					{
						System.out.print(chunks[i]+" ");
						peerchunklist[j++]=chunks[i];
					}
					System.out.print("\n");
					sendMessageToPeer(""+peerchunks);
					sendMessageToPeer(Arrays.toString(peerchunklist));
					String[] chunkpaths=new String[peerchunks];
					for(j=0;j<peerchunks;++j)
					{
						chunkpaths[j]=path.substring(0,path.lastIndexOf("."))+peerchunklist[j]+".random";
    					sendMessageToPeer(chunkpaths[j]);
    					chunkpaths[j]="temp/"+chunkpaths[j];
    					File source=new File(chunkpaths[j]);
    					sendMessageToPeer(""+source.length());
    				}
    				
    				for(j=0;j<peerchunks;++j)
					{
						FileInputStream fis = null;
   						BufferedInputStream bis = null;
    					String FILE_TO_SEND = chunkpaths[j];
						try
						{
          					File myFile = new File (FILE_TO_SEND);
     			     		byte [] mybytearray  = new byte [(int)myFile.length()];
     			     		fis = new FileInputStream(myFile);
     			     		bis = new BufferedInputStream(fis);
     			     		bis.read(mybytearray,0,mybytearray.length);
     			     		os = connection.getOutputStream();
     			     		System.out.println("Sending " + FILE_TO_SEND + "(" + mybytearray.length + " bytes)");
     			     		os.write(mybytearray,0,mybytearray.length);
     			     		os.flush();
     			     		System.out.println("Done.");
     			   		}
     			   		finally
     			   		{
     			     		if (bis != null) bis.close();
  			      		}
  			      	}
				}
				catch(Exception e)
				{
					System.err.println("Data received in unknown format");
				}
				System.out.println("Peer "+no+" Disconnected!\n");
			}
			catch(IOException ioException)
			{
				System.out.println("Disconnect with Client " + no);
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
					System.out.println("Disconnect with Client " + no);
				}
			}
		}
		public void sendMessageToPeer(String msg)
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
	}//End of PeerHandler Class

	//Send a message to the output stream
	void sendMessageToBootstrap(String msg)
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

	//Main method
	public static void main(String args[])
	{
		FileOwner client = new FileOwner();
		client.run();
	}
}//End of FileOwner CLass