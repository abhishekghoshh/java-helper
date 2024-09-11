package com.behavioral.template;

public class Client {

	public static void main(String[] args) {
		HouseTemplate houseType = null;

		houseType = new WoodenHouse();
		houseType.buildHouse();

		System.out.println("************");

		houseType = new GlassHouse();
		houseType.buildHouse();

	}

}
