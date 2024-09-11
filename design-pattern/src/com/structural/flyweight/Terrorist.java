package com.structural.flyweight;

public class Terrorist implements Player {

	private final String task;
    private String weapon;
    public Terrorist() {
        task = "Plant a bomb";
    }
	@Override
	public void assignWeapon(String weapon) {
		this.weapon = weapon;
	}

	@Override
	public void mission() {
		System.out.println("Terrorist with weapon " + weapon + " : Task is " + task);
	}

}
