package com.creational.objectPool;

public class JDBCConnection extends Connection {
	
	public JDBCConnection(long processNo) {
		super(processNo);
	}

	@Override
	public void connect() {
		System.out.println("Connected to the process no : "+this.getProcessNo());
	}

}
