LAN CHAT APPLICATION (Java, Sockets + Swing)

REQUIREMENTS: JDK 8 or higher. All computers on same Wi-Fi / LAN.

COMPILE:
    javac ChatServer.java ChatClient.java

RUN:
 1. On the server computer:   java ChatServer
    (it prints the server IP, e.g. 192.168.1.5)
 2. On each client computer:  java ChatClient
    -> enter the server IP and your name
    (to test on one PC, use "localhost" and open several clients)

COMMANDS:  type /users to see who is online.

If clients cannot connect: allow port 5000 in the server PC's firewall.
