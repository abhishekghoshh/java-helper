package com.behavioral.command;

public class CloseCommand implements Command {

	private FileSystem fileSystem;

	public CloseCommand(FileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}

	@Override
	public void execute() {
		this.fileSystem.closeFile();
	}

}
