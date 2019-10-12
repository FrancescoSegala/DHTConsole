#include "headers.h"
#include "M.h"
#include "dhtAPI.h"
#include "helperClass.h"


/*
In this file there is the implementation of the api for a dht node

The one that has to make public via a specific file are the ones in capital letter in the dotask function
The do task function is triggered when a new message is received by a client connection in this node

In order to access this API set you have to establish a connection with this node and send a message in UDP fashion with the
function call required. Then the doTask stub will parse the message and call the required function.

Callable API are:
 	GET k -> v
	FINDNODE k -> ip:port:hash(id)
	PUT k v -> hash(k)
	LIST -> #values:v1:v2:...:vn (of the contacting node)


*/


typedef long long int lli;

using namespace std;

HelperFunctions help = HelperFunctions();


/* put the entered key to the proper node */
lli put(string key,string value,NodeInformation &nodeInfo){
	if(key == "" || value == ""){
		cout<<"Key or value field empty\n";
		return -1;
	}

	else{

        lli keyHash = help.getHash(key);
		if ( DEBUG ) cout<<"Key is "<<key<<" and hash : "<<keyHash<<endl;

        pair< pair<string,int> ,  lli > node = nodeInfo.findSuccessor(keyHash);

        help.sendKeyToNode(node,keyHash,value);

        cout<<"key entered successfully, hash is " << keyHash << "\n" ;
        return keyHash;
	}
}

/* get key from the desired node */
string get(string key,NodeInformation nodeInfo){

    if(key == ""){
        cout<<"Key field empty\n";
        return "";
    }
    else{

        lli keyHash = help.getHash(key);

        pair< pair<string,int> , lli > node = nodeInfo.findSuccessor(keyHash);
				string val = help.getKeyFromNode(node,to_string(keyHash));

        if(val == "")
            return "Key Not found";
        else
            return val ;
    }
}

/* create a new ring */
void create(NodeInformation &nodeInfo){

    string ip = nodeInfo.sp.getIpAddress();
    int port = nodeInfo.sp.getPortNumber();

    /* key to be hashed for a node is ip:port */
    string key = ip+":"+(to_string(port));

    lli hash = help.getHash(key);

    /* setting id, successor , successor list , predecessor ,finger table and status of node */
    nodeInfo.setId(hash);
    nodeInfo.setSuccessor(ip,port,hash);
    nodeInfo.setSuccessorList(ip,port,hash);
    nodeInfo.setPredecessor("",-1,-1);
    nodeInfo.setFingerTable(ip,port,hash);
    nodeInfo.setStatus();

    /* launch threads,one thread will listen to request from other nodes,one will do stabilization */
    thread second(listenTo,ref(nodeInfo));
    second.detach();

    thread fifth(doStabilize,ref(nodeInfo));
    fifth.detach();

}

/* join in a DHT ring */
bool join(NodeInformation &nodeInfo,string ip,string port){

    if(help.isNodeAlive(ip,atoi(port.c_str())) == false){
        cout<<"Sorry but no node is active on this ip or port\n";
        return false;
    }

    /* set server socket details */
    struct sockaddr_in server;

    socklen_t l = sizeof(server);

    help.setServerDetails(server,ip,stoi(port));

    int sock = socket(AF_INET,SOCK_DGRAM,0);

    if(sock < 0){
        perror("error");
        exit(-1);
    }

    string currIp = nodeInfo.sp.getIpAddress();
    string currPort = to_string(nodeInfo.sp.getPortNumber());

    /* generate id of current node */
    lli nodeId = help.getHash(currIp+":"+currPort);

    char charNodeId[41];
    strcpy(charNodeId,to_string(nodeId).c_str());


    /* node sends it's id to main node to find it's successor */
    if (sendto(sock, charNodeId, strlen(charNodeId), 0, (struct sockaddr*) &server, l) == -1){
        cout<<"yaha 1\n";
        perror("error");
        exit(-1);
    }

    /* node receives id and port of it's successor */
    char ipAndPort[40];
    int len;
    if ((len = recvfrom(sock, ipAndPort, 1024, 0, (struct sockaddr *) &server, &l)) == -1){
        cout<<"yaha 2\n";
        perror("error");
        exit(-1);
    }
    ipAndPort[len] = '\0';

    close(sock);

    cout<<"Successfully joined the ring\n";

    string key = ipAndPort;
    lli hash = help.getHash(key);
    pair<string,int> ipAndPortPair = help.getIpAndPort(key);

    /* setting id, successor , successor list , predecessor, finger table and status */
    nodeInfo.setId(nodeId);
    nodeInfo.setSuccessor(ipAndPortPair.first,ipAndPortPair.second,hash);
    nodeInfo.setSuccessorList(ipAndPortPair.first,ipAndPortPair.second,hash);
    nodeInfo.setPredecessor("",-1,-1);
    nodeInfo.setFingerTable(ipAndPortPair.first,ipAndPortPair.second,hash);
    nodeInfo.setStatus();

    /* get all keys from it's successor which belongs to it now */
    help.getKeysFromSuccessor(nodeInfo , ipAndPortPair.first , ipAndPortPair.second);

    /* launch threads,one thread will listen to request from other nodes,one will do stabilization */
    thread fourth(listenTo,ref(nodeInfo));
    fourth.detach();

    thread third(doStabilize,ref(nodeInfo));
    third.detach();
    return true;
}

/* print successor,predecessor,successor list and finger table of node */
void printState(NodeInformation nodeInfo){
    string ip = nodeInfo.sp.getIpAddress();
    lli id = nodeInfo.getId();
    int port = nodeInfo.sp.getPortNumber();
    vector< pair< pair<string,int> , lli > > fingerTable = nodeInfo.getFingerTable();
    cout<<"Self "<<ip<<" "<<port<<" "<<id<<endl;
    pair< pair<string,int> , lli > succ = nodeInfo.getSuccessor();
    pair< pair<string,int> , lli > pre = nodeInfo.getPredecessor();
    vector < pair< pair<string,int> , lli > > succList = nodeInfo.getSuccessorList();
    cout<<"Succ "<<succ.first.first<<" "<<succ.first.second<<" "<<succ.second<<endl;
    cout<<"Pred "<<pre.first.first<<" "<<pre.first.second<<" "<<pre.second<<endl;
    for(int i=1;i<=M;i++){
        ip = fingerTable[i].first.first;
        port = fingerTable[i].first.second;
        id = fingerTable[i].second;
        cout<<"Finger["<<i<<"] "<<id<<" "<<ip<<" "<<port<<endl;
    }
    for(int i=1;i<=R;i++){
        ip = succList[i].first.first;
        port = succList[i].first.second;
        id = succList[i].second;
        cout<<"Successor["<<i<<"] "<<id<<" "<<ip<<" "<<port<<endl;
    }
}

/* node leaves the DHT ring */
void leave(NodeInformation &nodeInfo){
    pair< pair<string,int> , lli > succ = nodeInfo.getSuccessor();
    lli id = nodeInfo.getId();

    if(id == succ.second)
        return;

    /* transfer all keys to successor before leaving the ring */

    vector< pair<lli , string> > keysAndValuesVector = nodeInfo.getAllKeysForSuccessor();

    if(keysAndValuesVector.size() == 0)
        return;

    string keysAndValues = "";

    /* will arrange all keys and val in form of key1:val1;key2:val2; */
    for(int i=0;i<keysAndValuesVector.size();i++){
        keysAndValues += to_string(keysAndValuesVector[i].first) + ":" + keysAndValuesVector[i].second;
        keysAndValues += ";";
    }

    keysAndValues += "storeKeys";

    struct sockaddr_in serverToConnectTo;
    socklen_t l = sizeof(serverToConnectTo);

    help.setServerDetails(serverToConnectTo,succ.first.first,succ.first.second);

    int sock = socket(AF_INET,SOCK_DGRAM,0);

    if(sock < 0){
        perror("error");
        exit(-1);
    }

    char keysAndValuesChar[2000];
    strcpy(keysAndValuesChar,keysAndValues.c_str());

    sendto(sock,keysAndValuesChar,strlen(keysAndValuesChar),0,(struct sockaddr *)&serverToConnectTo,l);

    close(sock);
}

/* perform different tasks according to received msg */
void doTask(NodeInformation &nodeInfo,int newSock,struct sockaddr_in client,string nodeIdString){

    /* predecessor of this node has left the ring and has sent all it's keys to this node(it's successor) */
    if(nodeIdString.find("storeKeys") != -1){
        help.storeAllKeys(nodeInfo,nodeIdString);
    }

    /* check if the sent msg is in form of key:val, if yes then store it in current node (for put ) */
    else if(help.isKeyValue(nodeIdString)){
        pair< lli , string > keyAndVal = help.getKeyAndVal(nodeIdString);
        nodeInfo.storeKey(keyAndVal.first , keyAndVal.second);
    }

    else if(nodeIdString.find("alive") != -1){
        help.sendAcknowledgement(newSock,client);
    }

    /* contacting node wants successor list of this node */
    else if(nodeIdString.find("sendSuccList") != -1){
        help.sendSuccessorList(nodeInfo,newSock,client);
    }

    /* contacting node has just joined the ring and is asking for keys that belongs to it now */
    else if(nodeIdString.find("getKeys") != -1){
        help.sendNeccessaryKeys(nodeInfo,newSock,client,nodeIdString);
    }

    /* contacting node has run get command so send value of key it requires */
    else if(nodeIdString.find("k") != -1){
        help.sendValToNode(nodeInfo,newSock,client,nodeIdString);
    }

    /* contacting node wants the predecessor of this node MODIFIED TO USE PUT*/
    else if(nodeIdString.find("p1") != -1 || nodeIdString.find("p2") != -1){
        help.sendPredecessor(nodeInfo,newSock,client);

        /* p1 in msg means that notify the current node about this contacting node */
        if(nodeIdString.find("p1") != -1){
            callNotify(nodeInfo,nodeIdString);
        }
    }

    /* contacting node wants successor Id of this node for help in finger table */
    else if(nodeIdString.find("finger") != -1){
        help.sendSuccessorId(nodeInfo,newSock,client);
    }
		/*ADDED API*/
		/* entrypoint for the get API, so there is a socket that asked fot get:key and is waiting for the value*/
    else if(nodeIdString.find("GET") != -1){
			vector<string> args;
			string key, value;
			char msg[256];
			memset( msg, 0, sizeof(msg) );

			args = help.splitCommand(nodeIdString);
			if ( args.size() < 2 ){
				strcpy(msg, "Too Few args, usage : GET <key>");
			}
			else {
				key = args[1];
				value = get(key, nodeInfo) ;
				strcpy(msg,value.c_str());
			}
			sendto(newSock, msg, strlen(msg), 0, (struct sockaddr *) &client, sizeof(client) );
    }

		else if(nodeIdString.find("FINDNODE") != -1){
			vector<string> args;
			string key, value;
            //more than enough for a fixed  length string
			char msg[512];
			memset( msg, 0, sizeof(msg) );

			args = help.splitCommand(nodeIdString);
			if ( args.size() < 2 ){
				strcpy(msg, "Too Few args, usage : FINDNODE <key>");
			}
			else {
				key = args[1];
				pair< pair<string,int> , lli > node = nodeInfo.findSuccessor( help.getHash(key) );
				//value is of the form "ipAddr:port:hash"
				value = "" + node.first.first + ":" + std::to_string(node.first.second) +":"+ std::to_string(node.second)  ;
				strcpy(msg,value.c_str());
			}
			sendto(newSock, msg, strlen(msg), 0, (struct sockaddr *) &client, sizeof(client) );
    }

		/* entrypoint for the put API, so there is a socket that asking for put key value and is waiting for the hash(key)*/
    else if(nodeIdString.find("PUT") != -1){
			vector<string> args;
			string key, value, hashString;
			lli hash;
			char msg[256];
			memset( msg, 0, sizeof(msg) );

			args = help.splitCommand(nodeIdString);
			if ( args.size() < 3 ){
				strcpy(msg, "Too Few args, usage : PUT <key> <value>");
			}
			else {
				key = args[1];
				value = args[2];
				hash = put(key, value, nodeInfo);
				hashString = to_string(hash);
				strcpy(msg,hashString.c_str());
				// TODO if you want PUT k v --> hash(k):nodeID modify here to concatenate the node ID to the resulted hash and send value instead of hashString
				//pair< pair<string,int> , lli > node = nodeInfo.findSuccessor( hashString );
				//value = hashString+ ":" + node.first.first + ":" + std::to_string(node.first.second) +":"+ std::to_string(node.second)  ;
				//strcpy(msg,value.c_str());

			}
			sendto(newSock, msg, strlen(msg), 0, (struct sockaddr *) &client, sizeof(client) );
    }

		else if ( nodeIdString.find("LIST") != -1 ){

			vector<string> list;
			string marshalledRes;
			char msg[1024];
			memset( msg, 0, sizeof(msg) );

			list = nodeInfo.getValueList();
			marshalledRes = to_string(list.size()) ;

			for (string key : list){
				std::cout << "key: " << key << '\n';
				marshalledRes+=":"+key;
			}
			strcpy(msg, marshalledRes.c_str());
			sendto(newSock, msg, strlen(msg), 0, (struct sockaddr *) &client, sizeof(client) );
		}

    /* contacting node wants current node to find successor for it, in the join function he send its key hash without any other modifier*/
    else{
        help.sendSuccessor(nodeInfo,nodeIdString,newSock,client);
    }

}

/* listen to any contacting node */
void listenTo(NodeInformation &nodeInfo){
    struct sockaddr_in client;
    socklen_t l = sizeof(client);

    /* wait for any client to connect and create a new thread as soon as one connects */
    while(1){
        char charNodeId[40];
        int sock = nodeInfo.sp.getSocketFd();
        int len = recvfrom(sock, charNodeId, 1024, 0, (struct sockaddr *) &client, &l);
        charNodeId[len] = '\0';
        string nodeIdString = charNodeId;

        /* launch a thread that will perform diff tasks acc to received msg */
        thread f(doTask,ref(nodeInfo),sock,client,nodeIdString);
        f.detach();
    }
}

void doStabilize(NodeInformation &nodeInfo){

    /* do stabilize tasks */
    while(1){

        nodeInfo.checkPredecessor();

        nodeInfo.checkSuccessor();

        nodeInfo.stabilize();

        nodeInfo.updateSuccessorList();

        nodeInfo.fixFingers();

        this_thread::sleep_for(chrono::milliseconds(300));
    }
}

/* call notify of current node which will notify curr node of contacting node */
void callNotify(NodeInformation &nodeInfo,string ipAndPort){

    ipAndPort.pop_back();
    ipAndPort.pop_back();

    /* get ip and port of client node */
    pair< string , int > ipAndPortPair = help.getIpAndPort(ipAndPort);
    string ip = ipAndPortPair.first;
    int port = ipAndPortPair.second;
    lli hash = help.getHash(ipAndPort);

    pair< pair<string,int> , lli > node;
    node.first.first = ip;
    node.first.second = port;
    node.second = hash;

    /* notify current node about this node */
    nodeInfo.notify(node);
}
