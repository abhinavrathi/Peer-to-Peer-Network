# Peer to Peer Network

Creation of peer to peer network for file downloading using bootstrap server; resembles some features of Bit-torrent, but much simplified.

## Source Code Descripstion

1. BootstrapServer.java: Keeps track of all the clients (may be File Owner or peers) joining the network topology.<br>
   Maintains their listening port numbers and overall topology of their corresponding upload/download neighbours.  

2. FileOwner.java: Registers with the Bootstrap server and let the latter know it’s listening port.<br>
   Splits the file into chunks as specified and distributes the initial set of chunks among individual peers.

3. Peer.java: Registers with the Bootstrap server and let the latter know it's listening port and asks for it's upload/download neighbour's port numbers.<br>
   After receiving initial set of chunks from file owner, communication starts with it's corresponding upload/download neighbour.<br>
   Repetitively sends out the missing chunks to it's download neighbour and receives chunks from its upload neighbour.<br>
   Once all the chunks are available, it rebuilds the file from these chunks to create the original file.

## Installation

Download Source Code files into a diectory. Compile individual .java files using javac.

## Execution

1. Start the Bootstrap Server
2. Start the File Owner: Let Bootstrap Server know that you are the file owner.<br>
   Enter filename to be shared and the chunk size into which it needs to be split, thereafter terminate your connection with the Bootstrap Server.
3. Start the 5 peers one after the other: Let Bootstrap Server know that you are a peer.

## Results

1. The file owner splits the files into chunks and sends corresponding chunks to all the peers. (Each peer receives unique chunks)
2. Peers after receiving this initial set of chunks, communicate with each other to get complete set of chunks of the original file.
3. Each peer rebuilds the original file from the received chunks to create the original file.
4. All peers end up having the original file.

## Contributors

Univeristy: University of Florida<br>
Course: Computer Networks - CNT5106C<br>
Members: Abhinav Rathi<br>
&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;Suhas Tumkur Chandrashekhara<br>
Submisstion Date: 30 November 2015
