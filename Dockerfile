FROM gcc:9
COPY ./Chord-DHT/* /urs/src/Chord_DHT/
WORKDIR /urs/src/Chord_DHT
#do not change
RUN make
#ok work, now expose the ports and then publish it while running via docker run -P my_app
EXPOSE 9911/udp
EXPOSE 9911/tcp
