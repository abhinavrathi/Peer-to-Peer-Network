/******************************************************************/
/*                                                                */
/* CNT5106C - COMPUTER NETWORKS                                   */
/* Group Project - P2P network for file downloading                */
/* Group Members: Abhinav Rathi (UFID: 55935636)                  */
/*                Suhas Tumkur Chandrashekhara (UFID: 49497535)   */
/* Submission Date: 30 November 2015                              */
/*                                                                */ 
/******************************************************************/

Submission includes:
--------------------

1. BootstrapServer.java --> Keeps track of all the clients (may be File Owner or peers) joining the network topology.
                            Maintains their listening port numbers and overall topology of their corresponding upload/download neighbours.  

2. FileOwner.java --> Registers with the Bootstrap server and let the latter know which it’s listening port.
                      Splits the file into as many chunks as specified and sends the initial set of chunks to individual peers.

3. Peer.java --> Registers with the Bootstrap server and let the latter know it's listening port and asks for it's upload/download neighbour's port numbers.
                 After receiving initial set of chunks from file owner, communication starts with it's corresponding upload/download neighbour.
                 Sends out the chunks it has to it's download neighbour and receives chunks from its upload neighbour.
                 Once all the chunks are available, it rebuilds the file from these chunks to create the original file.  

4. BootstrapServer.class
5. BootstrapServer$ClientHandler.class
6. FileOwner.class
7. FileOwner$PeerHandler.class
8. Peer.class
9. Peer$Upload.class
10. Peer$Download.class
11. README.txt

+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

Execution Steps:
----------------

1. Start the Bootstrap Server
2. Start the File Owner --> Let Bootstrap Server know that you are the file owner.
                            Enter filename to be shared and the chunk size into which it needs to be split into
                            and, then terminate your connection with the Bootstrap Server.
3. Start the 5 peers one after the other --> Let Bootstrap Server know that you are a peer.

+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

Result of the execution:
------------------------

1. The file owner splits the files into chunks and sends corresponding chunks to all the peers. (Each peer receives unique chunks)
2. Peers after receiving this initial set of chunks, communicate with each other to get complete set of chunks of the original file.
3. Each peer rebuilds the original file from the received chunks to create the original file.
4. All peers end up having the original file. 

+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++