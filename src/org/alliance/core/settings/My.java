package org.alliance.core.settings;

import java.util.Random;
import java.util.TreeSet;



/**
 * Created by IntelliJ IDEA.
 * User: maciek
 * Date: 2005-dec-28
 * Time: 15:02:20
 */
public class My extends SettingClass {

    private Integer guid = new Random().nextInt();
    private String nickname = "Rookie";
    private Integer cguid = 0; //this is a checksum of the invitations property, disguised so that script kiddies won't find it
    private Integer invitations = 0; //every time this user completes an invitation successfully he gets a point
    private String status = "";
    private TreeSet<Integer> ignoreList = new TreeSet<Integer>();

    public My() {
    }

    public My(Integer guid, String nickname) {
        this.guid = guid;
        this.nickname = nickname;
    }

    public String getNickname() {
        nickname = filterHTML(nickname);
        return nickname;
    }

    public void setNickname(String nickname) {
        nickname = filterHTML(nickname);
        this.nickname = nickname;
    }
    
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		status = filterHTML(status);
		this.status = status;
	}

    private String filterHTML(String html) {
        if (html != null) {
        	html = html.replaceAll("<", "").replaceAll(">", "");
        }
        return html;
    }

    public Integer getGuid() {
        return guid;
    }

    public void setGuid(Integer guid) {
        this.guid = guid;
    }

    public Integer getCguid() {
        return cguid;
    }

    public void setCguid(Integer cguid) {
        this.cguid = cguid;
    }

    public Integer getInvitations() {
        if (cguid != null && cguid != 0) {
            if ((invitations ^ 234427) * 13 != cguid) {
                invitations = 0;
            }
        } else {
            createChecksumAndSetInvitations(invitations);
        }
        return invitations;
    }

    public void setInvitations(Integer invitations) {
        this.invitations = invitations;
    }

    public void createChecksumAndSetInvitations(Integer invitations) {
        this.invitations = invitations;
        cguid = (invitations ^ 234427) * 13;
    }
    
	public void addIgnore(int guid) {
		ignoreList.add(guid);
	}
	
	public boolean removeIgnore(int guid) {
		return ignoreList.remove(guid);
	}
	
	public TreeSet<Integer> getIgnoreList() {
		return ignoreList;
	}
	
	public void setIgnoreList(TreeSet<Integer> ignoreList) {
		this.ignoreList = ignoreList;
	}
}
