package com.structural.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProxyInternet implements Internet {
	private Random random = new Random();
	private Internet internet = null;
	private String employeeName;
	private static List<String> bannedSites;

	static {
		bannedSites = new ArrayList<>();
		bannedSites.add("whatever.com");
		bannedSites.add("nope.com");
		bannedSites.add("yup.com");
		bannedSites.add("hello.com");
	}

	public ProxyInternet(String employeeName) {  
        this.employeeName = employeeName;  
    }
	@Override
	public void grantInternetAccess() {
		if (getRole(employeeName) > 4) {
			internet = new RealInternet(employeeName);
			internet.grantInternetAccess();
		} else {
			System.out.println("No Internet access granted. Your job level is below 5");
			throw new RuntimeException("internet is not connected");
		}
	}

	@Override
	public void connectTo(String serverhost) throws Exception {
		if(null==internet) {
			this.grantInternetAccess();
		}
		if (bannedSites.contains(serverhost.toLowerCase())) {
			throw new Exception("Access Denied");
		}
		internet.connectTo(serverhost);
	}

	public int getRole(String emplName) {
		// Check role from the database based on Name and designation
		// return job level or job designation.
		return random.nextInt(10);
	}
}
