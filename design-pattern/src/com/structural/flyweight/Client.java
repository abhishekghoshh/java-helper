package com.structural.flyweight;

import java.util.Random;

public class Client {
	
	/* Assume that we have a total of 10 players in the game. 
     * getPlayer() is called simply using the class name since the method is a static one
     * Assign a weapon chosen randomly uniformly from the weapon array
     * Send this player on a mission */
	
	private static String[] playerType =  {"Terrorist", "CounterTerrorist"};
    private static String[] weapons = {"AK-47", "Maverick", "Gut Knife", "Desert Eagle"};
    private static Random random = new Random();
	
	public static void main(String args[]){
        for (int i = 0; i < 10; i++)  {
            Player p = PlayerFactory.getPlayer(getRandPlayerType());
            p.assignWeapon(getRandWeapon());
            p.mission();
        }
    }

	private static String getRandWeapon() {
        return weapons[random.nextInt(weapons.length)];
	}

	private static String getRandPlayerType() {
		return playerType[random.nextInt(playerType.length)];
	}
}
