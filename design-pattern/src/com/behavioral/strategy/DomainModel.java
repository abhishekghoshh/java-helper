package com.behavioral.strategy;

public abstract class DomainModel implements Partner {
	private String data;

	public void setData(String data) {
		this.data = data;
	}

	public void printThread(String output) {
		System.out.println("Executing in thread " + Thread.currentThread().getName() + " " + output + " " + data);
	}
}
