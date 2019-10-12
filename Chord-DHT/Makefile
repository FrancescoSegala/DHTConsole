all: do

do: DHTconsole.o nodeInformation.o helperClass.o  port.o dhtAPI.o
				g++ DHTconsole.o  dhtAPI.o port.o nodeInformation.o helperClass.o -o DHTconsole -lcrypto -lpthread

DHTconsole.o: DHTconsole.cpp
				g++ -std=c++11 -c DHTconsole.cpp

port.o: port.cpp
				g++ -std=c++11 -c port.cpp

dhtAPI.o: dhtAPI.cpp
				g++ -std=c++11 -c dhtAPI.cpp

nodeInformation.o: nodeInformation.cpp
				g++ -std=c++11 -c nodeInformation.cpp

helperClass.o: helperClass.cpp
				g++ -std=c++11 -c helperClass.cpp

clean:
				rm *.o DHTconsole *.txt
