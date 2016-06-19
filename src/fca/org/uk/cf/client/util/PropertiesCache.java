package fca.org.uk.cf.client.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

/**
 * This class load all the Clourflare properties from resourcesproperties file from root(/) folder
 * of this binary jar
 * @author CK
 */
public class PropertiesCache {

	private final Properties configProp = new Properties();

	private PropertiesCache() {

		try {
            InputStream in = new FileInputStream("resources.properties");
			configProp.load(in);
        } catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static class LazyHolder {
		private static final PropertiesCache INSTANCE = new PropertiesCache();
	}

	public static PropertiesCache getInstance() {
		return LazyHolder.INSTANCE;
	}

	public String getProperty(String key) {
		return configProp.getProperty(key);
	}

	public Set<String> getAllPropertyNames() {
		return configProp.stringPropertyNames();
	}

	public boolean containsKey(String key) {
		return configProp.containsKey(key);
	}
	
	public static void main(String[] args) {
		//Get individual properties
		  System.out.println(PropertiesCache.getInstance().getProperty("cloudflare.baseurl"));
		  System.out.println(PropertiesCache.getInstance().getProperty("registered.emailId"));
	}
}
