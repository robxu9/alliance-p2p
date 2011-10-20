package org.alliance.core.node;

import com.stendahls.util.TextUtils;

import java.util.Collection;
import java.util.HashMap;

import org.alliance.core.Language;

/**
 * Created by IntelliJ IDEA.
 * User: maciek
 * Date: 2005-dec-29
 * Time: 13:50:55
 */
public abstract class Node {
	
    protected String nickname;
    protected String status = "";
    protected int guid, adminCode = 0;
    protected HashMap<Integer, UntrustedNode> friendsFriends;

    protected Node() {
    }

    protected Node(String nickname, int guid) {
        this.nickname = nickname;
        this.guid = guid;
    }

    public abstract boolean isConnected();

    public abstract int getNumberOfInvitedFriends();

    public abstract boolean hasNotBeenOnlineForLongTime();

    public abstract long getLastSeenOnlineAt();

    public boolean isAway() {
        return false;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public void setStatus(String status) {
    	status = filterHTML(status);
    	this.status = status;
    }

    public int getGuid() {
        return guid;
    }

    public void setGuid(int guid) {
        this.guid = guid;
    }

    @Override
    public String toString() {
        return getNickname() + "[" + guid + ", " + TextUtils.simplifyClassName(getClass()) + "]";
    }

    public void addFriendsFriend(UntrustedNode untrustedNode) {
        if (friendsFriends == null) {
            friendsFriends = new HashMap<Integer, UntrustedNode>();
        }
        friendsFriends.put(untrustedNode.getGuid(), untrustedNode);
    }

    public void removeAllFriendsOfFriend() {
        if (friendsFriends != null) {
            friendsFriends.clear();
        }
    }

    public boolean friendsFriendsLoaded() {
        return friendsFriends != null;
    }

    public Collection<UntrustedNode> friendsFriends() {
        if (friendsFriends == null) {
            return null;
        }
        return friendsFriends.values();
    }

    public UntrustedNode getFriendsFriend(int guid) {
        if (friendsFriends == null) {
            return null;
        }
        return friendsFriends.get(guid);
    }

    public HashMap<Integer, UntrustedNode> getFriendsFriends() {
        return friendsFriends;
    }

    public void setFriendsFriends(HashMap<Integer, UntrustedNode> friendsFriends) {
        this.friendsFriends = friendsFriends;
    }   

    public long getShareSize() {
        return 0;
    }

    public String getNickname() {
        return nickname;
    }
    
    public String getStatus() {
    	return status;
    }

    public int getAllianceBuildNumber() {
        return 0;
    }  

    public int getNumberOfFilesShared() {
        return 0;
    }

    public double getHighestOutgoingCPS() {
        return 0;
    }

    public double getHighestIncomingCPS() {
        return 0;
    }

    public long getTotalBytesSent() {
        return 0;
    }

    public long getTotalBytesReceived() {
        return 0;
    }

    public String calculateRatio() {
        long upload = getTotalBytesSent();
        long download = getTotalBytesReceived();
        if (upload == 0 || download == 0) {
            return "?";
        }
        if (upload > download) {
            return ((double) Math.round(((double) upload) / download * 10)) / 10 + ":1";
        }
        return "1:" + ((double) Math.round(((double) download) / upload * 10)) / 10;
    }
    
    public String getInfoString(){
    	StringBuilder sb = new StringBuilder("<html>");

    	 sb.append("<b>" + getNickname() + ": </b>");
    	 if(hasNotBeenOnlineForLongTime() && getLastSeenOnlineAt() != 0){
    		 sb.append(Language.getLocalizedString(getClass(), "seen", Long.toString((System.currentTimeMillis() - getLastSeenOnlineAt()) / 1000 / 60 / 60 / 24))).append("<br>");
    	 }
    	 else {
    		 sb.append("<br>");
    	 }
    	 if(getStatus() != ""){
    	 sb.append("<i><p>" + getStatus() + "</p></i><br>");
    	 }
    	 sb.append(Language.getLocalizedString(getClass(), "share", TextUtils.formatByteSize(getShareSize()),
                 Integer.toString(getNumberOfFilesShared()))).append("<br>");
         sb.append(Language.getLocalizedString(getClass(), "ratio", calculateRatio())).append("<br>");
         sb.append(Language.getLocalizedString(getClass(), "invites", Integer.toString(getNumberOfInvitedFriends()))).append("<br>");
         sb.append(Language.getLocalizedString(getClass(), "upspeed", TextUtils.formatByteSize((long) getHighestOutgoingCPS()))).append("<br>");
         sb.append(Language.getLocalizedString(getClass(), "downspeed", TextUtils.formatByteSize((long) getHighestIncomingCPS()))).append("<br>");
         sb.append(Language.getLocalizedString(getClass(), "uptotal", TextUtils.formatByteSize(getTotalBytesSent()))).append("<br>");
         sb.append(Language.getLocalizedString(getClass(), "downtotal", TextUtils.formatByteSize(getTotalBytesReceived()))).append("<br>");
         return sb.toString() + "</html>";
    }
    
    private String filterHTML(String html) {
        if (html != null) {
        	html = html.replaceAll("<", "").replaceAll(">", "");
        }
        return html;
    }
}
