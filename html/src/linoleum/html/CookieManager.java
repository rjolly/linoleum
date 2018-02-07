package linoleum.html;

import java.net.CookieHandler;

public class CookieManager extends java.net.CookieManager {
	public CookieManager() {
		CookieHandler.setDefault(this);
	}
}
