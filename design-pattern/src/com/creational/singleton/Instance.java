package com.creational.singleton;

public interface Instance {
	public default void showHashCode() {
		System.out.println(this.getClass().getCanonicalName() + " : " + this.hashCode() + " is executing");
	}
}
