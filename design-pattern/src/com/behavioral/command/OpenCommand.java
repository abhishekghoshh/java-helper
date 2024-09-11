package com.behavioral.command;

public class OpenCommand implements Command {

	private FileSystem fileSystem;

    public OpenCommand(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Override
    public void execute() {
        this.fileSystem.openFile();
    }

}
