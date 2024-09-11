package com.structural.proxy;

public class RealInternet implements Internet {

	 private String employeeName;  
	    public RealInternet(String employeeName) {  
	        this.employeeName = employeeName;  
	    }

	@Override
	public void connectTo(String serverhost) throws Exception {
		System.out.println("Connecting to " + serverhost);
	}

	@Override
	public void grantInternetAccess() {
		 System.out.println("Internet Access granted for employee: "+ employeeName);  
	}

}
