#!/bin/bash

. ./net.config

echo "" > 0.txt ;
./DHTconsole c 0 &

sleep 1 ;

for ((i=1 ; i<$NUM_NODES+1 ; i++))
do
  echo "" > $i.txt ;
  ./DHTconsole j 127.0.0.1 9911 $i &
  sleep 1;
done
