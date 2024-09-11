package com.creational.builder;

public class Cake {

	private final double sugar;
	private final double butter;
	private final int eggs;
	private final int vanila;
	private final double flour;
	private final double bakingpowder;
	private final double milk;
	private final int cherry;

	// private constructor to enforce object creation through builder
	private Cake(CakeBuilder builder) {
		this.sugar = builder.sugar;
		this.butter = builder.butter;
		this.eggs = builder.eggs;
		this.vanila = builder.vanila;
		this.flour = builder.flour;
		this.bakingpowder = builder.bakingpowder;
		this.milk = builder.milk;
		this.cherry = builder.cherry;
	}

	public double getSugar() {
		return sugar;
	}

	public double getButter() {
		return butter;
	}

	public int getEggs() {
		return eggs;
	}

	public int getVanila() {
		return vanila;
	}

	public double getFlour() {
		return flour;
	}

	public double getBakingpowder() {
		return bakingpowder;
	}

	public double getMilk() {
		return milk;
	}

	public int getCherry() {
		return cherry;
	}

	@Override
	public String toString() {
		return "Cake{" + "sugar=" + sugar + ", butter=" + butter + ", eggs=" + eggs + ", vanila=" + vanila + ", flour="
				+ flour + ", bakingpowder=" + bakingpowder + ", milk=" + milk + ", cherry=" + cherry + '}';

	}

	public static class CakeBuilder {

		private double sugar;
		private double butter;
		private int eggs;
		private int vanila;
		private double flour;
		private double bakingpowder;
		private double milk;
		private int cherry;

		public CakeBuilder(double sugar, double butter, int eggs, int vanila, double flour, double bakingpowder,
				double milk, int cherry) {
			this.sugar = sugar;
			this.butter = butter;
			this.eggs = eggs;
			this.vanila = vanila;
			this.flour = flour;
			this.bakingpowder = bakingpowder;
			this.milk = milk;
			this.cherry = cherry;
		}

		public CakeBuilder() {
		}

		public CakeBuilder sugar(double cup) {
			this.sugar = cup;
			return this;
		}

		public CakeBuilder butter(double cup) {
			this.butter = cup;
			return this;
		}

		public CakeBuilder eggs(int number) {
			this.eggs = number;
			return this;
		}

		public CakeBuilder vanila(int spoon) {
			this.vanila = spoon;
			return this;
		}

		public CakeBuilder flour(double cup) {
			this.flour = cup;
			return this;
		}

		public CakeBuilder bakingpowder(double spoon) {
			this.sugar = spoon;
			return this;
		}

		public CakeBuilder milk(double cup) {
			this.milk = cup;
			return this;
		}

		public CakeBuilder cherry(int number) {
			this.cherry = number;
			return this;
		}

		// return fully build object
		public Cake build() {
			return new Cake(this);
		}
	}

}