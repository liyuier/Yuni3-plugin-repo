package test;

import utils.MCPinger;

public class McPingTest {
    public static void main(String[] args) {
        MCPinger.McServerStatus status = MCPinger.ping("p79.ytonidc.com", 12239);
        if (status == null) {
            System.out.println("FAIL");
            return;
        }
        System.out.println("online=" + status.getOnline());
        System.out.println("playerNames=" + status.getPlayerNames());
        System.out.println("playerNames size=" + status.getPlayerNames().size());
    }
}
