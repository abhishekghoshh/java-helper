package com.creational.abstractFactory;

public class FactoryProducer {
	
    public static AbstractFactory getFactory(String factory){
    	
    	switch (factory.toUpperCase()) {
			case "SHAPE":
				return new ShapeFactory();
			case "COLOR":
				return new ColorFactory();
			default:
				throw new RuntimeException("Factory "+factory+" is unknown");
		}
    }

}