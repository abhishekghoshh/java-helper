package com.creational.objectPool;

public abstract class Connection {
	
	protected long processNo;
    public Connection(long processNo) {
      this.processNo = processNo;  
      System.out.println("Object with process no. " + processNo + " was created");
    }
    public long getProcessNo() {
        return processNo;
    }
	public abstract void connect();
}
