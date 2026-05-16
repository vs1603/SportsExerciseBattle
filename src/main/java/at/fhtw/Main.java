package at.fhtw;

import at.fhtw.seb.restserver.server.Server;

public class Main {
    public static void main(String[] args) {
        try {
            new Server().start();
            System.out.println("Server started on port 10001");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}