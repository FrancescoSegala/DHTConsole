#ifndef dhtAPI_h
#define dhtAPI_h

#include <iostream>

#include "port.h"
#include "nodeInformation.h"

using namespace std;

typedef long long int lli;



lli put(string key,string value,NodeInformation &nodeInfo);
string get(string key,NodeInformation nodeInfo);
void create(NodeInformation &nodeInfo);
bool join(NodeInformation &nodeInfo,string ip,string port);
void printState(NodeInformation nodeInfo);
void listenTo(NodeInformation &nodeInfo);
void doStabilize(NodeInformation &nodeInfo);
void callNotify(NodeInformation &nodeInfo,string ipAndPort);
void callFixFingers(NodeInformation &nodeInfo);
void doTask(NodeInformation &nodeInfo,int newSock,struct sockaddr_in client,string nodeIdString);
void leave(NodeInformation &nodeInfo);

#endif
