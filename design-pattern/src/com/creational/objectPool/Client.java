package com.creational.objectPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Client {
	private static AtomicLong processNo=new AtomicLong(0);
	private final static int count =10;
	
	public static void main(String ags[]) {
		
		ObjectPool<Connection> pool = new ObjectPool<Connection>(4, 10, 5) {
			protected Connection createObject(){
				return new JDBCConnection(processNo.incrementAndGet());
			}
		};
		
		pool.shutdown();
		
		ExecutorService executor = Executors.newFixedThreadPool(8);
    	
    	List<Process> processList = new ArrayList<>(count);
    	for(int i=0;i<count;i++) {
    		processList.add(new Process(pool, i));
    	}
    	for(int i=0;i<count;i++) {
    		executor.execute(processList.get(i));
    	}
        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException ex){
            ex.printStackTrace();
        }
	}
}
