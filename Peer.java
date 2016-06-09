import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.lang.*;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.swing.JOptionPane;
import java.util.Arrays;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FileWriter;
import java.io.Writer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import javax.swing.JOptionPane;

public class Peer
{
	Socket requestSocket;           //socket connect to the server
	ObjectOutputStream out,fout;    //stream write to the socket
 	ObjectInputStream in,fin;
	String message;                 
	String MESSAGE;
	int myPort,fileownerPort,uploadPort,downloadPort,upload,download;
	int[] chunks;
	int[] mychunks;
	String fname,size;
	BufferedReader bufferedReader;
	private static final int ServerPort=9999;

 	public void Peer() {}

	void run()
	{
		
		try
		{
			//Create a socket to connect to the Bootstrap Server
			requestSocket = new Socket("localhost",ServerPort);
			System.out.println("Connected to Bootstrap Server in port 9999");
			
			//Initialize InputStream, OutputStream & User Input
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
			bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			while(true)
			{
				MESSAGE = (String)in.readObject();
				myPort=Integer.parseInt(MESSAGE);
				System.out.println("My Port Number is: "+myPort);
				MESSAGE = (String)in.readObject();
				System.out.print("\nMessage from Bootstrap Server:\n" + MESSAGE);
				message = bufferedReader.readLine();
				sendMessage(message);
				MESSAGE = (String)in.readObject();
				if(MESSAGE.equals("exit"))
				{
					System.out.println("Exiting Connection from Bootstrap Server!");
					break;
				}
				else
				{
					System.out.println("\nMessage from Bootstrap Server: "+MESSAGE);
				}

				//Requesting Bootstrap Server for File Owner's Port Number
				message="fileowner";
				System.out.println("Message to Bootstrap Server: Please send me file owner's listening port");
				sendMessage(message);
				MESSAGE = (String)in.readObject();
				System.out.println("Message from Bootstrap Server: File Owner's Listening Port is " + MESSAGE);
				fileownerPort=Integer.parseInt(MESSAGE);

				//Communication with File Owner to Receive My Chunks
				try
				{
					requestSocket = new Socket("localhost", fileownerPort);
					System.out.println("\nConnected to File Owner in Port Number "+fileownerPort);
					//initialize inputStream and outputStream
					fout = new ObjectOutputStream(requestSocket.getOutputStream());
					fout.flush();
					fin = new ObjectInputStream(requestSocket.getInputStream());
					MESSAGE = (String)fin.readObject();
					System.out.println("Message from File Owner: "+MESSAGE);
					message="Hello File Owner, Please send me file details";
					System.out.println("Message to File Owner: "+message);
					sendMessage2(message);
					fname = (String)fin.readObject();
					System.out.println("Message from File Owner:\n"+"File Name: "+fname);
					size = (String)fin.readObject();
					System.out.println("File Size: "+size+" bytes");
					MESSAGE = (String)fin.readObject();
					String[] items = MESSAGE.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ","").split(",");
					chunks = new int[items.length];
					for (int i = 0; i < chunks.length; i++)
					{
    					try
    					{
        					chunks[i] = Integer.parseInt(items[i]);
    					}
    					catch (NumberFormatException nfe) {};
					}
					System.out.println("File Chunk List: "+Arrays.toString(chunks));
					message="Details Received. Please Send my total chunks.";
					System.out.println("Message to File Owner: "+message);
					sendMessage2(message);
					MESSAGE=(String)fin.readObject();
					int myChunkno=Integer.parseInt(MESSAGE);
					MESSAGE=(String)fin.readObject();
					String[] myitems = MESSAGE.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ","").split(",");
					mychunks = new int[myitems.length];
					for (int i = 0; i < mychunks.length; i++)
					{
    					try
    					{
        					mychunks[i] = Integer.parseInt(myitems[i]);
    					}
    					catch (NumberFormatException nfe) {};
					}
					System.out.println("Your Total Chunks are "+myChunkno+" nos. having Chunk IDs: "+Arrays.toString(mychunks));
					
					//Creating a new file
					Path newFile = Paths.get("Summary.txt");
					try
					{
						Files.deleteIfExists(newFile);
						newFile = Files.createFile(newFile);
					}
					catch (IOException ex)
					{
						System.out.println("Error creating file");
					}
					for(int i=0;i<mychunks.length;++i)
					{
						try(Writer writer = new BufferedWriter(new FileWriter("Summary.txt",true)))
						{
							if(i==mychunks.length-1)
								writer.append(mychunks[i]+"");
							else
								writer.append(mychunks[i]+"\n");
							writer.flush();
						}
						catch(IOException exception)
						{
							System.out.println("Error writing to file");
						}
					}
					
					System.out.println("My Status: Ok! Ready to receive "+myChunkno+" chunks.");
					InputStream is;
					int j=0;
					String[] myChunkPath=new String[myChunkno];
					String[] myChunkSize=new String[myChunkno];
					for(j=0;j<myChunkno;++j)
					{
						try
						{
							myChunkPath[j]=(String)fin.readObject();
							myChunkSize[j]=(String)fin.readObject();
						}
						catch(Exception e){;}
						System.out.println("Chunk ID: "+mychunks[j]+"; Chunk Name: "+myChunkPath[j]+"; Chunk Size: "+myChunkSize[j]+" bytes.");
					}
      				j=0;		
					is=requestSocket.getInputStream();
					FileOutputStream fos = null;
					byte[] buf=new byte[1];
					int n;
					String folder=new String("temp");
					new File(folder).mkdir();
					for(int i = 0; i < myChunkno;i++)
					{
						String FILE_TO_RECEIVED=myChunkPath[i];
						int FILE_SIZE = Integer.parseInt(myChunkSize[i]);
						System.out.println("Receiving file: " + FILE_TO_RECEIVED);
						fos = new FileOutputStream(folder+"/"+FILE_TO_RECEIVED);
						//read file
						j=0;
						while(j!=FILE_SIZE )
						{
							n = is.read(buf);
							j++;
							fos.write(buf,0,n);
							fos.flush();
						}
					 	fos.close();
					}
				}
				catch(Exception e){;}
				break;
			}

			//Requesting Bootstrap Server for Upload & Download Neighbour's Port Number
			message="updown";
			System.out.println("\nMessage to Bootstrap Server: Please send me Upload & Download Neighbour ports.");
			sendMessage(message);
			upload=-1;
			download=-1;
			while(upload==-1||download==-1)
			{
				MESSAGE = (String)in.readObject();
				upload=Integer.parseInt(MESSAGE);
				MESSAGE = (String)in.readObject();
				download=Integer.parseInt(MESSAGE);
				System.out.println("\nMessage from Bootstrap Server:\nUpload Neighbour ID: "+upload+"\nDownload Neighbour ID: "+download);
			}
			uploadPort=0;
			downloadPort=0;
			int flagforup=0,flagfordown=0;
			while(uploadPort==0||downloadPort==0)
			{
				try{TimeUnit.SECONDS.sleep(1);}
				catch(Exception e){;}
				MESSAGE = (String)in.readObject();
				uploadPort=Integer.parseInt(MESSAGE);
				MESSAGE = (String)in.readObject();
				downloadPort=Integer.parseInt(MESSAGE);
				System.out.println("\nMessage from Bootstrap Server:\nUpload Neighbour Port Number: "+uploadPort+"\nDownload Neighbour Number: "+downloadPort);
				String temp;
				int flagg=1;
				if(downloadPort!=0&&flagfordown==0)
				{
					flagfordown=1;
					try
					{
						try
						{
            				do
            				{
            					try{TimeUnit.SECONDS.sleep(1);}
								catch(Exception e){;}
            					try
            					{
            						Socket downSocket = new Socket("localhost", downloadPort);
            						new Download(downSocket,chunks.length,fname).start();
            						flagg=1;
        	   					}
        	   					catch(Exception e)
        	   					{
        	   						System.out.println("\nTrying to Connect to Download Peer!");
        	   						flagg=0;
        	   					}
        	   				}while(flagg!=1);
        				}
        				finally
        				{
        				    ;
        				}
        				
					}
					catch(Exception e)
					{

					}
				}

				if(uploadPort!=0&&flagforup==0)
				{
					flagforup=1;
					try
					{
						ServerSocket uppeer=new ServerSocket(myPort);
						try
						{
            				try
            				{
            					new Upload(uppeer.accept(),chunks.length,fname).start();
								System.out.println("\nUpload Peer is Connected!");
							}
        	   				catch(Exception e)
        	   				{
        	   					System.out.println("\nWaiting for Download Peer!");
        	   				}
        				}
        				finally
        				{
        				    uppeer.close();
        				}
					}
					catch(Exception e)
					{

					}
				}


			}

			System.out.println("\nRing Topology Connected!");
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
	}

	//Upload Class Starts
	private static class Upload extends Thread
	{
		private Socket connection;
		private ObjectInputStream in;	   //stream read from the socket
        private ObjectOutputStream out;    //stream write to the socket
		private int no,mysum,ursum;
		private String temp;
		private int[] chunks;
		private int chunkno,sum;
		private File f1,f2;
		private String filename;
		public Upload(Socket connection,int chunkno,String filename)
		{
            this.connection = connection;
            this.chunkno=chunkno;
            this.chunks=new int[chunkno];
            this.filename=filename;
        }
        public void run()
        {
        	for(int i=0;i<chunkno;++i)
        	{
        		chunks[i]=0;
        	}

        	do
        	{
        		try{TimeUnit.SECONDS.sleep(2);}
				catch(Exception e){;}
        	
        		try
        		{
        			//Reading Self Summary File
        			Path file=Paths.get("Summary.txt");
        			try(BufferedReader reader = Files.newBufferedReader(file,Charset.defaultCharset()))
					{
						String lineFromFile = "";
						System.out.println("\nThe contents of Summary file as of now are: ");
						while((lineFromFile = reader.readLine()) != null)
						{
							System.out.println(lineFromFile);
							chunks[Integer.parseInt(lineFromFile)-1]=1;
						}
					}
					catch(IOException exception)
					{
						System.out.println("Error while reading file");
					}

					//Requesting for ChunkID List
        			out = new ObjectOutputStream(connection.getOutputStream());
					out.flush();
					in = new ObjectInputStream(connection.getInputStream());
					temp="Send Your Chunk ID List!";
					sendMessage(temp);
					System.out.println("Message to Upload Neighbour: "+temp);
					temp = (String)in.readObject();
        			System.out.println("Message from Upload Neighbour: "+temp);

        			//Receiving Chunk ID List
        			InputStream is;
        			is=connection.getInputStream();
      				FileOutputStream fos = null;
      				byte[] buf=new byte[1];
      				int n;
      				String size=(String)in.readObject();
      				String FILE_TO_RECEIVED="SummaryU.txt";
  					int FILE_SIZE = Integer.parseInt(size);
    				System.out.println("Receiving file: " + FILE_TO_RECEIVED);
 					fos = new FileOutputStream(FILE_TO_RECEIVED);
					//read file
					int j=0;
					while(j!=FILE_SIZE )
					{
						n = is.read(buf);
						j++;
						fos.write(buf,0,n);
    					fos.flush();
        			}
          			fos.close();
          			ursum=0;
          			//Reading Received Summary File
        			Path file1=Paths.get("SummaryU.txt");
        			try(BufferedReader reader = Files.newBufferedReader(file1,Charset.defaultCharset()))
					{
						String lineFromFile = "";
						System.out.println("\nThe contents of Received Summary file as of now are: ");
						while((lineFromFile = reader.readLine()) != null)
						{
							ursum+=1;
							System.out.println(lineFromFile);
							if(chunks[Integer.parseInt(lineFromFile)-1]==0)
							{
								chunks[Integer.parseInt(lineFromFile)-1]=1;
								sendMessage("REQUEST");
								//Need to request and then receive file and overwrite Summary
								sendMessage(lineFromFile);
								//Receiving Chunk
        						//InputStream is;
        						is=connection.getInputStream();
      							fos = null;
      							buf=new byte[1];FILE_TO_RECEIVED=(String)in.readObject();
      							size=(String)in.readObject();
  								FILE_SIZE = Integer.parseInt(size);
    							System.out.println("Receiving file: " + FILE_TO_RECEIVED);
 								fos = new FileOutputStream(FILE_TO_RECEIVED);
								//read file
								j=0;
								while(j!=FILE_SIZE )
								{
									n = is.read(buf);
									j++;
									fos.write(buf,0,n);
    								fos.flush();
        						}
          						fos.close();

          						//Overwriting Summary
          						//Creating a new file
								Path newFile = Paths.get("Summary.txt");
								try
								{
									Files.deleteIfExists(newFile);
									newFile = Files.createFile(newFile);
								}
								catch (IOException ex)
								{
									System.out.println("Error creating file");
								}
								for(int i=0;i<chunks.length;++i)
								{
									try(Writer writer = new BufferedWriter(new FileWriter("Summary.txt",true)))
									{
										if(chunks[i]==1)
										{
											if(i==chunks.length-1)

												writer.append(chunks.length+"");
											else
												writer.append((i+1)+"\n");
										}
										writer.flush();
									}
									catch(IOException exception)
									{
										System.out.println("Error writing to file");
									}
								}
							}
						}
					}
					catch(IOException exception)
					{
						System.out.println("Error while reading file");
					}
       			}
        		catch(Exception e){;}
        		
        		mysum=0;
        		for(int i=0;i<chunks.length;++i)
        			mysum+=chunks[i];
        		if(mysum==ursum&&mysum==chunks.length)
        		{
        			sendMessage("exit");
        		}
        		else
        		{
        			sendMessage("continue");
        		}
        		System.out.println("My Sum: "+mysum+"; Your Sum: "+ursum);
        	}while(ursum!=chunks.length||mysum!=chunks.length);
        	
			System.out.println("Finally Exiting!");

        	//Merging File
        	String[] path=new String[chunks.length];
	        for(int i=0;i<chunks.length;++i)
	        {
	        	path[i]="temp/"+filename.substring(0,filename.lastIndexOf("."))+(i+1)+".random";
	        }
	        String destination;
	    	try
	    	{
	    		int n=path.length;
	    		destination=new String(filename);
	    		File destinationFile=new File(destination);
	    		FileOutputStream dst=new FileOutputStream(destinationFile);
	    		for(int i=0;i<n;++i)
	    		{
	    			File source=new File(path[i]);
	    			FileInputStream src=new FileInputStream(source);
	    			byte[] data;
	    			data = new byte[(int)source.length()];
	    			src.read(data);
	    			dst.write(data);
	    			src.close();
	    		}
	    		dst.close();
	    	}
	    	catch (Exception e)
	    	{
	        	JOptionPane.showMessageDialog(null, "Error in Merge File \n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	        }

		}

		//send a message to the output stream
		void sendMessage(String msg)
		{
			try
			{
				//stream write the message
				out.writeObject(msg);
				out.flush();
			}
			catch(IOException ioException)
			{
				ioException.printStackTrace();
			}
		}
	}

	private static class Download extends Thread
	{
		private Socket connection;
		private ObjectInputStream in;	   //stream read from the socket
        private ObjectOutputStream out;    //stream write to the socket
		private int no,i;
		private String temp;
		private int[] chunks;
		private int chunkno,sum;
		private String filename;
		public Download(Socket connection,int chunkno,String filename)
		{
            this.connection = connection;
            this.chunkno=chunkno;
            this.chunks=new int[chunkno];
            this.filename=filename;
	    }
        public void run()
        {
        	for(i=0;i<chunkno;++i)
        	{
        		chunks[i]=0;
        	}
        	do
			{ 
				try{TimeUnit.SECONDS.sleep(1);}
				catch(Exception e){;}
        		try
        		{
        			//Reading Self Summary File
        			Path file=Paths.get("Summary.txt");
        			try(BufferedReader reader = Files.newBufferedReader(file,Charset.defaultCharset()))
					{
						String lineFromFile = "";
						System.out.println("\nThe contents of Summary file as of now are: ");
						while((lineFromFile = reader.readLine()) != null)
						{
							System.out.println(lineFromFile);
							chunks[Integer.parseInt(lineFromFile)-1]=1;
						}
					}
					catch(IOException exception)
					{
						System.out.println("Error while reading file");
					}

        			//Receiving Chunk ID Request
        			out = new ObjectOutputStream(connection.getOutputStream());
					out.flush();
					in = new ObjectInputStream(connection.getInputStream());
        			temp = (String)in.readObject();
        			System.out.println("Message from Download Neighbour: "+temp);
					sendMessage("This is the requested chunk ID List!");
					
					//Sending Chunk ID List
					FileInputStream fis = null;
   					BufferedInputStream bis = null;
					String FILE_TO_SEND = "Summary.txt";
					File source=new File(FILE_TO_SEND);
					OutputStream os;
    				sendMessage(""+source.length());
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
  			      		
  			      	temp = (String)in.readObject();
  			      	while(temp.equals("REQUEST"))
  			      	{
  			      		//Ask ChunkID
  			      		String temp1 = (String)in.readObject();
  			      		System.out.println("Incoming Request for Chunk ID "+temp1);
  			      		
  			      		String fname=filename.substring(0,filename.lastIndexOf("."));
  			      		fname="temp/"+fname+temp1+".random";
  			      		//Sending Chunk
						fis = null;
   						bis = null;
						FILE_TO_SEND = fname;
						source=new File(FILE_TO_SEND);
						sendMessage(""+FILE_TO_SEND);
    					sendMessage(""+source.length());
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
						temp = (String)in.readObject();
  			      	}	
				}
        		catch(Exception e){;}
        	}while(!temp.equals("exit"));
		}

		//send a message to the output stream
		void sendMessage(String msg)
		{
			try
			{
				//stream write the message
				out.writeObject(msg);
				out.flush();
			}
			catch(IOException ioException)
			{
				ioException.printStackTrace();
			}
		}
	}

	//To Bootstrap Server
	void sendMessage(String msg)
	{
		try
		{
			//stream write the message
			out.writeObject(msg);
			out.flush();
		}
		catch(IOException ioException)
		{
			ioException.printStackTrace();
		}
	}

	//To File Owner
	void sendMessage2(String msg)
	{
		try
		{
			//stream write the message
			fout.writeObject(msg);
			fout.flush();
		}
		catch(IOException ioException)
		{
			ioException.printStackTrace();
		}
	}

	//main method
	public static void main(String args[])
	{
		Peer peer = new Peer();
		peer.run();
	}

}