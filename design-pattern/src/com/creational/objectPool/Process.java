package com.creational.objectPool;

public class Process implements Runnable{
	private ObjectPool<Connection> pool;
	private int threadNo;

	public Process(ObjectPool<Connection> pool, int threadNo) {
		this.pool = pool;
		this.threadNo = threadNo;
	}
	public void run() {
		Connection connection = pool.borrowObject();
		System.out.println("Thread " + threadNo + ": Object with process no. " + connection.getProcessNo() + " was borrowed");
		
		connection.connect();
		
		pool.returnObject(connection);
		System.out.println("Thread " + threadNo +": Object with process no. " + connection.getProcessNo() + " was returned");
	}
}
