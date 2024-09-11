package com.structural.flyweight;

import java.util.HashMap;
import java.util.Map;

public class PlayerFactory {
	private static final String COUNTER_TERRORIST = "COUNTERTERRORIST";
	private static final String TERRORIST = "TERRORIST";
	private static Map<String, Player> playerMap = new HashMap<>();
	
	public int totalObjectsCreated() {
        return playerMap.size();
    }
	public static Player getPlayer(String type) {
        Player player = null;
        if (playerMap.containsKey(type.toUpperCase()))
        	player = playerMap.get(type.toUpperCase());
        else {
            switch (type.toUpperCase()) {
                case TERRORIST:
                    System.out.println("Terrorist Created");
                    player = new Terrorist();
                    break;
                case COUNTER_TERRORIST:
                    System.out.println("Counter Terrorist Created");
                    player = new CounterTerrorist();
                    break;
                default:
                    throw new RuntimeException("Unreachable code!");
            }
            playerMap.put(type.toUpperCase(), player);
        }
        return player;
    }
	
	
}
