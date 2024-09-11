package com.behavioral.template;

public abstract class HouseTemplate {

	public abstract void buildWalls();

	public abstract void buildPillars();

	public final void buildHouse() {
		buildFoundation();
		buildPillars();
		buildWalls();
		buildWindows();
		buildComplete();
	}

	private void buildComplete() {
		System.out.println("House is built");
	}

	private void buildWindows() {
		System.out.println("Building Glass Windows");
	}

	private void buildFoundation() {
		System.out.println("Building foundation with cement, iron rods and sand");
	}
}
