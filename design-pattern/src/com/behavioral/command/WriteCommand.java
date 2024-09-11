package com.behavioral.command;

public class WriteCommand implements Command{
	private FileSystem fileSystem;

	public WriteCommand(FileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}

	@Override
	public void execute() {
		this.fileSystem.writeFile();
	}
}
