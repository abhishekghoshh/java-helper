package io.github.abhishekghoshh.basics;

import io.github.abhishekghoshh.runner.Run;

@Run(active = false)
public class ClientServerTest {
    @Run(active = true)
    private static void clientServer() {
        System.out.println("ClientServer");
    }

}
