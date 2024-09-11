package com.structural.proxy;

public interface Internet {
	void grantInternetAccess();
	void connectTo(String serverhost) throws Exception;
}
