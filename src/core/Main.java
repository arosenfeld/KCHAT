package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import operations.commands.InvalidCommandException;
import operations.commands.PresenceCommand;
import operations.commands.RoomMessageCommand;
import operations.commands.UserMessageCommand;
import packets.ChatPacket;
import packets.ChatPacket.PacketType;
import packets.messages.ChatMessage;
import packets.messages.UserPresenceMessage;
import packets.messages.UserPresenceMessage.PresenceStatus;
import transport.UdpMulticast;
import util.Configuration;
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
        System.out.println("Type 'help' for information or Ctrl-C to exit.");
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
                    public void receivePacket(ChatPacket packet) {
                        if (packet.getType() == PacketType.CHAT_MESSAGE) {
                            ChatMessage msg = (ChatMessage) packet.getPayload();
                            System.out.println("Got message from " + packet.getSrc() + ": "
                                    + new String(msg.getMessage()));
                        } else if (packet.getType() == PacketType.USER_PRESENCE) {
                            UserPresenceMessage pres = (UserPresenceMessage) packet.getPayload();
                            String action = pres.getPresenceStatus() == PresenceStatus.JOIN ? "joined" : "left";
                            System.out.println("User " + packet.getSrc().toString() + " " + action + " room "
                                    + pres.getRoomName());
                        }
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
        } else if (input.startsWith("msg-room")) {
            if (split.length >= 2) {
                int msgOffset = split[0].length() + split[1].length() + 1;
                sock.executeCommand(new RoomMessageCommand(new LongInteger(split[1]), input.substring(msgOffset)
                        .getBytes()));
            }
        } else if (input.startsWith("msg-user")) {
            if (split.length >= 3) {
                boolean persist = split[2].toUpperCase().equals("Y");
                int msgOffset = split[0].length() + split[1].length() + split[2].length() + 3;
                sock.executeCommand(new UserMessageCommand(new LongInteger(split[1]), input.substring(msgOffset)
                        .getBytes(), persist));
            }
        } else {
            System.out.println("Commands:");
            System.out.println("\tstatus <room> <join|leave>      : Joins/leaves <room>");
            System.out
                    .println("\tmsg-user <user> <persist> <msg> : Sends <user> the message <msg>.  If [persist] equals 'Y', it will be sent persistently");
            System.out.println("\tmsg-room <room> <msg>           : Sends <room> the message <msg>");
            System.out.println("\trooms                           : Lists rooms and members");
            System.out.println("\thelp                            : Prints this help message");
        }
    }
}