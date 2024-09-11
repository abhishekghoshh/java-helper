package com.structural.composite;

import java.util.ArrayList;
import java.util.List;

public class Manager implements Employee {
	private int id;
	private String name;
	private double salary;
	private List<Employee> employees = new ArrayList<>();
	
	public Manager(int id, String name, double salary) {
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
        
		employees.forEach((employee)->{
			System.out.println("Id ="+employee.getId());  
	        System.out.println("Name ="+employee.getName());  
	        System.out.println("Salary ="+employee.getSalary());
		});
	}

	@Override
	public void add(Employee employee) {
		employees.add(employee);
	}

	@Override
	public void remove(Employee employee) {
		employees.remove(employee);

	}

	@Override
	public Employee getChild(int i) {
		return employees.get(i);
	}
}
