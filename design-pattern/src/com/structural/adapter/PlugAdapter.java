package com.structural.adapter;

public class PlugAdapter {

	private Cable inBound;
	private Cable outBound;

	public void connect(Cable inBound, Cable outBound) {
		this.inBound = inBound;
		this.outBound = outBound;
		outBound.plugCable(inBound);
	}

}
