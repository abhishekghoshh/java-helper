package com.behavioral.command;

public class Client {

	public static void main(String[] args) {
		FileInvoker file=null;
		FileSystem fileSystem = FileSystemUtil.getFileSystem();

        Command openCommand = new OpenCommand(fileSystem);
        Command writeCommand = new WriteCommand(fileSystem);
        Command closeCommand = new CloseCommand(fileSystem);
        
        file = new FileInvoker(openCommand);
        file.execute();

        file = new FileInvoker(writeCommand);
        file.execute();
        
        file = new FileInvoker(closeCommand);
        file.execute();

        file = new FileInvoker();
        file.add(openCommand);
        file.add(writeCommand);
        file.add(closeCommand);
        file.executeAll();
        
	}

}
