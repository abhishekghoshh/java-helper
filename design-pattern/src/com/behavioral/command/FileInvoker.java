package com.behavioral.command;

import java.util.ArrayList;
import java.util.List;

public class FileInvoker {
	private Command command;
	private List<Command> commandList = null;

    public FileInvoker(Command command) {
        this.command = command;
    }
    public FileInvoker() {
    	commandList = new ArrayList<>();
    }

    public void execute() {
    	if(null!=command) {
    		command.execute();    		
    	}
    }
    public void add(Command command) {
    	this.commandList.add(command);
    }
    public void executeAll() {
    	if(null!=commandList) {
    		for(Command command:commandList) {
        		command.execute();
        	}	
    	}
    }
}
