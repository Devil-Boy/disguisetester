package pgDev.bukkit.DisguiseTester;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Properties;

public class DTConfig {
	private Properties properties;
	private final DisguiseTester plugin;
	public boolean upToDate = true;
	
	// List of Config Options
	boolean packetDebug;
	
	public DTConfig(Properties p, final DisguiseTester plugin) {
		properties = p;
        this.plugin = plugin;
        
        // Grab values here.
        packetDebug = getBoolean("packetDebug", false);
	}
	
	// Value obtaining functions down below
	public int getInt(String label, int thedefault) {
		String value;
        try {
        	value = getString(label);
        	return Integer.parseInt(value);
        } catch (NoSuchElementException e) {
        	return thedefault;
        }
    }
    
    public double getDouble(String label) throws NoSuchElementException {
        String value = getString(label);
        return Double.parseDouble(value);
    }
    
    public File getFile(String label) throws NoSuchElementException {
        String value = getString(label);
        return new File(value);
    }

    public boolean getBoolean(String label, boolean thedefault) {
    	String values;
        try {
        	values = getString(label);
        	return Boolean.valueOf(values).booleanValue();
        } catch (NoSuchElementException e) {
        	return thedefault;
        }
    }
    
    public Color getColor(String label) {
        String value = getString(label);
        Color color = Color.decode(value);
        return color;
    }
    
    public HashSet<String> getSet(String label, String thedefault) {
        String values;
        try {
        	values = getString(label);
        } catch (NoSuchElementException e) {
        	values = thedefault;
        }
        String[] tokens = values.split(",");
        HashSet<String> set = new HashSet<String>();
        for (int i = 0; i < tokens.length; i++) {
            set.add(tokens[i].trim().toLowerCase());
        }
        return set;
    }
    
    public LinkedList<String> getList(String label, String thedefault) {
    	String values;
        try {
        	values = getString(label);
        } catch (NoSuchElementException e) {
        	values = thedefault;
        }
        if(!values.equals("")) {
            String[] tokens = values.split(",");
            LinkedList<String> set = new LinkedList<String>();
            for (int i = 0; i < tokens.length; i++) {
                set.add(tokens[i].trim().toLowerCase());
            }
            return set;
        }else {
        	return new LinkedList<String>();
        }
    }
    
    public String getString(String label) throws NoSuchElementException {
        String value = properties.getProperty(label);
        if (value == null) {
        	upToDate = false;
            throw new NoSuchElementException("Config did not contain: " + label);
        }
        return value;
    }
    
    public String getString(String label, String thedefault) {
    	String value;
    	try {
        	value = getString(label);
        } catch (NoSuchElementException e) {
        	value = thedefault;
        }
        return value;
    }
    
    public String linkedListToString(LinkedList<String> list) {
    	if(list.size() > 0) {
    		String compounded = "";
    		boolean first = true;
        	for (String value : list) {
        		if (first) {
        			compounded = value;
        			first = false;
        		} else {
        			compounded = compounded + "," + value;
        		}
        	}
        	return compounded;
    	}
    	return "";
    }
    
    
    // Config creation method
    public void createConfig() {
    	try {
    		@SuppressWarnings("static-access")
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(plugin.pluginConfigLocation)));
    		out.write("#\r\n");
    		out.write("# DisguiseTester Configuration\r\n");
    		out.write("#\r\n");
    		out.write("\r\n");
    		out.write("# Packet Monitoring\r\n");
    		out.write("#	If you have SpoutPlugin on your server, you can\r\n");
    		out.write("#	monitor the following packets using this option:\r\n");
    		out.write("#		-24 = entity spawn\r\n");
    		out.write("#		-28 = entity velocity\r\n");
    		out.write("#		-29 = destroy entity\r\n");
    		out.write("#		-30 = just an entity packet\r\n");
    		out.write("#		-31 = entity relative move\r\n");
    		out.write("#		-32 = entity look\r\n");
    		out.write("#		-33 = entity look and relative move\r\n");
    		out.write("#		-34 = entity teleport\r\n");
    		out.write("#	Information about these outgoing packets will\r\n");
    		out.write("#	be output to the console.\r\n");
    		out.write("packetDebug=" + packetDebug + "\r\n");
    		out.close();
    	} catch (Exception e) {
    		System.out.println(e);
    		// Not sure what to do? O.o
    	}
    }
}
