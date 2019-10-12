#include <iostream>
#include <fstream>
#include <vector>
#include <cstdlib>
#include <cstring>
#include <thread>
#include <signal.h>
#include <unistd.h>
#include "port.h"
#include "dhtAPI.h"
#include "helperClass.h"
#include "nodeInformation.h"

using namespace std;


NodeInformation nodeInfoRef ;

/* tell about all commands */
void showHelp(){
    cout<<"1) create : will create a DHT ring\n\n";
    cout<<"2) join <ip> <port> : will join ring by connecting to main node having ip and port\n\n";
    cout<<"3) printstate : will print successor, predecessor, fingerTable and Successor list\n\n";
    cout<<"4) print : will print all keys and values present in that node\n\n";
    cout<<"5) port : will display port number on which node is listening\n\n";
    cout<<"6) port <number> : will change port number to mentioned number if that port is free\n\n";
    cout<<"7) put <key> <value> : will put key and value to the node it belongs to\n\n";
    cout<<"8) get <key> : will get value of mentioned key\n\n";
    cout<<"9) leave : will startrWithPort the shutdown routine and leave the ring\n\n";
    cout<<"10) id : will output the nodeId \n\n";
}



void my_handler(int signal){
	printf("signal %d received, leaving the ring...\n", signal);
	leave(nodeInfoRef);
	exit(1);
}


void initRing( vector<string> args ){
  NodeInformation nodeInfo = NodeInformation();
  /* open a socket to listen to other nodes */


  nodeInfo.sp.specifyPortServer();
  /*
  signal catching routine

  handled signal for leaving and termination:

  SIGHUP
  SIGINT
  SIGQUIT
  SIGSEGV
  SIGTERM
  */
  nodeInfoRef = ref(nodeInfo);
  struct sigaction sigIntHandler;

   sigIntHandler.sa_handler = my_handler;
   sigemptyset(&sigIntHandler.sa_mask);
   sigIntHandler.sa_flags = 0;

   sigaction(SIGINT, &sigIntHandler, NULL);
   sigaction(SIGHUP, &sigIntHandler, NULL);
   sigaction(SIGQUIT, &sigIntHandler, NULL);
   sigaction(SIGSEGV, &sigIntHandler, NULL);
   sigaction(SIGTERM, &sigIntHandler, NULL);
   // till here

    cout<<"Now listening at port number "<<nodeInfo.sp.getPortNumber()<<endl;
	

    string command ;

    if ( args[0] == "c" ){
      //create a new ring with ./exec c
      nodeInfo.sp.changePortNumber(9911);
      cout<<"Now listening at port number "<<nodeInfo.sp.getPortNumber()<<endl;
      thread first(create,ref(nodeInfo));
      first.detach();
    }
    if (args[0] == "j" ){
      //join an existing ring with ./exec j <ip> <addr>
      bool b = join(nodeInfo,args[1],args[2]);
      if ( b ) std::cout << "ring joined!" <<'\n';
      else std::cout << "join failed!" << '\n';
    }

	bool set = false ; 

    while (1){
		if ( !set && nodeInfo.getId() != -1 ) {
			ofstream outfile;
			if (args[0] == "c") {
				cout << "id for " << args[1] << " is " << nodeInfo.getId() << endl;
				outfile.open("0.txt" , ios::out | ios::trunc);
				outfile << "0:" <<nodeInfo.getId() ; 
			}	
			else {
				cout << "id for "<< args[3] <<" is "<< nodeInfo.getId() << endl;
				outfile.open(args[3]+".txt", ios::out | ios::trunc );
				outfile << args[3] << ":" <<nodeInfo.getId() ; 
			
			}
			set = true; 
		}
      	usleep(500);
    }
}


void initialize(){

	NodeInformation nodeInfo = NodeInformation();
	/* open a socket to listen to other nodes */
	nodeInfo.sp.specifyPortServer();
	/*
	signal catching routine

	handled signal for leaving and termination:

	SIGHUP
	SIGINT
	SIGQUIT
	SIGSEGV
	SIGTERM

	*/

	nodeInfoRef = ref(nodeInfo);
	struct sigaction sigIntHandler;

	 sigIntHandler.sa_handler = my_handler;
	 sigemptyset(&sigIntHandler.sa_mask);
	 sigIntHandler.sa_flags = 0;

	 sigaction(SIGINT, &sigIntHandler, NULL);
	 sigaction(SIGHUP, &sigIntHandler, NULL);
	 sigaction(SIGQUIT, &sigIntHandler, NULL);
	 sigaction(SIGSEGV, &sigIntHandler, NULL);
	 sigaction(SIGTERM, &sigIntHandler, NULL);
	 // till here

	  cout<<"Now listening at port number "<<nodeInfo.sp.getPortNumber()<<endl;
	  cout<<"Type help to display available console commands\n";

	string command;

	while(1){
		cout<<"> ";
		getline(cin,command);

		/* find space in command and seperate arguments*/
		HelperFunctions help = HelperFunctions();
		vector<string> arguments = help.splitCommand(command);
    vector<string> list;
    string s;
    bool b ;
    lli hash;
		string arg = arguments[0];
		if(arguments.size() == 1){

			/* creates */
			if(arg == "create"){
				if(nodeInfo.getStatus() == true){
					cout<<"Sorry but this node is already on the ring\n";
				}
				else{
					//this will start a new thread with the routine:create and data ref2nodeInfo
					thread first(create,ref(nodeInfo));
					first.detach();
				}
			}
      else if (arg == "id"){
        std::cout << "node Id : " << nodeInfo.getId() << '\n';
      }
			/* prints */
			else if(arg == "printstate"){
				if(nodeInfo.getStatus() == false){
					cout<<"Sorry this node is not in the ring\n";
				}
				else
					printState(nodeInfo);
			}

			/* leaves */
			else if(arg == "leave"){
				leave(nodeInfo);
				nodeInfo.sp.closeSocket();
				return;
			}

			/* print current port number */
			else if(arg == "port"){
				cout<<nodeInfo.sp.getPortNumber()<<endl;
			}

			/* print keys present in this node */
			else if(arg == "print"){
				if(nodeInfo.getStatus() == false){
					cout<<"Sorry this node is not in the ring\n";
				}
				else
					nodeInfo.printKeys();
			}

			else if(arg == "help"){
				showHelp();
			}

			else{
				cout<<"Invalid Command\n";
			}
		}

		else if(arguments.size() == 2){

			/* */
			if(arg == "port"){
				if(nodeInfo.getStatus() == true){
					cout<<"Sorry you can't change port number now\n";
				}
				else{
					int newPortNo = atoi(arguments[1].c_str());
					nodeInfo.sp.changePortNumber(newPortNo);
				}
			}

			/* */
			else if(arg == "get"){
				if(nodeInfo.getStatus() == false){
					cout<<"Sorry this node is not in the ring\n";
				}
				else
					s = get(arguments[1],nodeInfo);
          std::cout << "get()"<< arguments[1] << ") = " << s << '\n';
			}

			else{
				cout<<"Invalid Command\n";
			}
		}

		else if(arguments.size() == 3){

			/* */
			if(arg == "join"){
				if(nodeInfo.getStatus() == true){
					cout<<"Sorry but this node is already on the ring\n";
				}
				else
					b = join(nodeInfo,arguments[1],arguments[2]);
          if ( b ) std::cout << "ring joined!" <<'\n';
          else std::cout << "join failed!" << '\n';
			}

			/* puts the entered key and it's value to the necessary node*/
			else if(arg == "put"){
				if(nodeInfo.getStatus() == false){
					cout<<"Sorry this node is not in the ring\n";
				}
				else
					hash = put(arguments[1],arguments[2],nodeInfo);
          if ( DEBUG ) std::cout << "added smthg, hash is "<< hash << '\n';
			}

			else{
				cout<<"Invalid Command\n";
			}
		}

		else{
			cout<<"Invalid Command\n";
		}
	}

}



int main (int argc, char* argv[] ){

  if ( argc > 1  ){
    std::vector<string> v;
    for (int i = 1 ; i< argc  ;i++){
      v.push_back(argv[i]);
    }
    initRing(v);
  }
  else {
    initialize();
  }

}



















//need space
