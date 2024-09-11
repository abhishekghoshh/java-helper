package com.structural.flyweight;

public class CounterTerrorist implements Player {

	private final String task;
    private String weapon;
    public CounterTerrorist() {
        task = "Diffuse a bomb";
    }
	@Override
	public void assignWeapon(String weapon) {
		this.weapon = weapon;
	}

	@Override
	public void mission() {
		System.out.println("Counter terrorist with weapon " + weapon + " : Task is " + task);
	}

}
