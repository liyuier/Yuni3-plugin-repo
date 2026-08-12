package test;

import utils.MCPinger;

public class McPingTest {
    public static void main(String[] args) {
        System.out.println("pinging p79.ytonidc.com:12239...");
        long t = System.currentTimeMillis();
        MCPinger.McServerStatus status = MCPinger.ping("p79.ytonidc.com", 12239);
        if (status == null) {
            System.out.println("FAIL " + (System.currentTimeMillis() - t) + "ms");
        } else {
            System.out.println("OK " + (System.currentTimeMillis() - t) + "ms");
            System.out.println("motd=" + status.getMotd());
            System.out.println("version=" + status.getVersionName() + " protocol=" + status.getProtocol());
            System.out.println("players=" + status.getOnline() + "/" + status.getMax());
        }

        System.out.println("\npinging mc.hypixel.net:25565...");
        t = System.currentTimeMillis();
        status = MCPinger.ping("mc.hypixel.net", 25565);
        if (status == null) {
            System.out.println("FAIL " + (System.currentTimeMillis() - t) + "ms");
        } else {
            System.out.println("OK " + (System.currentTimeMillis() - t) + "ms");
            System.out.println("motd=" + status.getMotd());
            System.out.println("players=" + status.getOnline() + "/" + status.getMax());
        }
    }
}
