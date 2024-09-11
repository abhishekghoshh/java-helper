package com.structural.adapter;

public class Client {

	public static void main(String[] args) {

		Port usbPort = new UsbPort();
		Cable usbCable = new UsbCable();
		Cable microUsbCable = new MicroUsbCable();

		usbCable.connectToPort(usbPort);

		new PlugAdapter().connect(microUsbCable, usbCable);

		usbCable.transmit("a movie");
		System.out.println(usbCable.getMessage()); // it will be printed null
		System.out.println(microUsbCable.getMessage()); // transmitted message from usbport will be coming to this

	}

}
