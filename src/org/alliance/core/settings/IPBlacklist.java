package org.alliance.core.settings;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class IPBlacklist extends ArrayList<Routerule> {

    private static final long serialVersionUID = 917458421801053394L;

    public IPBlacklist() {
    }

    public boolean checkConnection(String hostname) throws UnknownHostException {
        InetAddress out = InetAddress.getByName(hostname);
        return checkConnection(makeArrayInt(out.getAddress()));
    }

    public boolean checkConnection(byte[] addr) {
        return checkConnection(makeArrayInt(addr));
    }

    public boolean checkConnection(int ipaddr) {
        for (Routerule rule : this) {
            if (isValid(ipaddr, rule.getAddress(), rule.getMask())) {
                if (rule.getRuleType().equals(Routerule.ALLOW)) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private int makeArrayInt(byte[] addr) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (addr[i]) << shift;
        }
        return value;
    }

    private boolean isValid(int address, int rule, int mask) {
    	if ((address >> (32 - mask)) == (rule >> (32 - mask))) {
            return true;
        } else {
            return false;
        }
    }

    public boolean add(String human) throws Exception {
    	String temp = human;
    	int divider = human.indexOf(':');
    	int maskDiv = human.indexOf('/');
        if(divider != -1 && maskDiv != -1) {
        	temp = human.substring(0, divider);
        	human = temp + human.substring(maskDiv);	
        }
        for(int i = 0; i < this.size(); i++) {
        	if(this.get(i).toString().equals(human)){
        		return false;
        	}
        }
    	return this.add(new Routerule(human));
    }

    public boolean checkConnection(SocketAddress socketAddress) {
        InetSocketAddress temp = (InetSocketAddress) socketAddress;
        return checkConnection(makeArrayInt(temp.getAddress().getAddress()));
    }
}
