package com.structural.composite;

public class Developer implements Employee {
	private int id;
	private String name;
	private double salary;
	
	public Developer(int id, String name, double salary) {
		this.id = id;
		this.name = name;
		this.salary = salary;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public double getSalary() {
		return salary;
	}

	@Override
	public void print() {
		System.out.println("Id ="+getId());  
        System.out.println("Name ="+getName());  
        System.out.println("Salary ="+getSalary()); 

	}

	@Override
	public void add(Employee employee) {
		throw new RuntimeException("this is not allowed");
	}

	@Override
	public void remove(Employee employee) {
		throw new RuntimeException("this is not allowed");

	}

	@Override
	public Employee getChild(int i) {
		throw new RuntimeException("this is not allowed");
	}

}
