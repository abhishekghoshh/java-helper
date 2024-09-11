package com.creational.prototype;

public abstract class Shape implements Cloneable {
	
	protected String id;
	protected String description;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public abstract void draw();
	
	public Shape clone() {
		Shape clone=null;
		try {
			clone= (Shape) super.clone();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return clone;
	}
}
