package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import operations.commands.InvalidCommandException;
import operations.commands.PresenceCommand;
import operations.commands.UserMessageCommand;
import packets.ChatPacket;
import transport.UdpMulticast;
import util.Configuration;
import util.Logging;
import util.LongInteger;

public class Main {
    private static ChatSocket sock;
    private static BufferedReader cmdLine;

    public static void main(String[] args) throws Exception {
        String propertiesPath = System.getProperty("kchat.propertiesPath");
        if (propertiesPath == null) {
            propertiesPath = "kchat.properties";
        }
        Configuration.getInstance().setFile(propertiesPath);

        cmdLine = new BufferedReader(new InputStreamReader(System.in));
        init();

        String cmd = "";
        System.out.println("Type 'help' for information or 'exit' to exit.");
        while (!cmd.equals("exit")) {
            System.out.print("> ");
            cmd = cmdLine.readLine();
            handleCommand(cmd);
        }
        sock.stop();
        System.out.println("Socket closed; program exiting.");
    }

    private static void init() throws IOException {
        System.out.print("Username: ");
        sock = new ChatSocket(new UdpMulticast(Configuration.getInstance().getValueAsString("udp.iface"), Configuration
                .getInstance().getValueAsString("udp.host"), Configuration.getInstance().getValueAsInt("udp.port")),
                new ChatPacketCallback() {

                    @Override
                    public void receivePacket(ChatPacket message) {
                        System.out.println("! Received Packet " + message);
                        System.out.println("----------------------------");
                    }
                }, new LongInteger(cmdLine.readLine()));
        System.out.println("Starting socket with ID " + sock.getUUID());
        sock.start();
    }

    private static void handleCommand(String input) throws Exception {
        String[] split = input.split(" ");
        if (input.equals("rooms")) {
            System.out.println(sock.getPresenceManager());
        } else if (input.startsWith("status")) {
            if (split.length == 3 && (split[2].equals("join") || split[2].equals("leave"))) {
                try {
                    sock.executeCommand(new PresenceCommand(new LongInteger(input.split(" ")[1]), split[2]
                            .equals("join")));
                } catch (InvalidCommandException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            } else {
                System.out.println("Invalid: Must specify a room name and join/leave.");
            }
        } else if (input.startsWith("msg-user")) {
            if (split.length >= 3) {
                boolean persist = split[2].toUpperCase().equals("Y");
                int msgOffset = split[0].length() + split[1].length() + split[2].length() + 3;
                sock.executeCommand(new UserMessageCommand(new LongInteger(split[1]), input.substring(msgOffset)
                        .getBytes(), persist));
            }
        } else if (input.equals("exit")) {
        } else {
            System.out.println("Commands:");
            System.out.println("\tstatus <room> <join|leave>      : Joins/leaves <room>");
            System.out
                    .println("\tmsg-user <user> <persist> <msg> : Sends <user> the message <msg>.  If [persist] equals 'Y', it will be sent persistently");
            System.out.println("\trooms                           : Lists rooms and members");
            System.out.println("\thelp                            : Prints this help message");
            System.out.println("\texit                            : Closes the socket and exits the driver");
        }
    }
}