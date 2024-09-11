package com.structural.composite;

public class Client {
	public static void main(String args[]) {
		Employee emp1 = new Developer(101, "Sohan Kumar", 20000.0);
		Employee emp2 = new Developer(102, "Mohan Kumar", 25000.0);
		Employee emp3 = new Supervisor(103, "Seema Mahiwal", 30000.0);
		Employee manager = new Manager(100, "Ashwani Rajput", 100000.0);

		manager.add(emp1);
		manager.add(emp2);
		manager.add(emp3);
		manager.print();
		
		emp1.print();
		emp3.print();
	}
}
