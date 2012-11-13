client:	Client.java
	javac *.java
	@ echo ""
	java Client < run.txt

server:	MainServer.java
	javac *.java
	@ echo ""
	java MainServer

clean:	Client.java MainServer.java
	rm local_test.txt

kill:	Client.java MainServer.java
	rm *.class