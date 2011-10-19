package org.alliance.core.node;

import org.alliance.core.CoreSubsystem;
import org.alliance.Version;

import com.Updater.Admin.AdminChecker;

/**
 * Created by IntelliJ IDEA.
 * User: maciek
 * Date: 2006-jan-03
 * Time: 14:38:28
 * To change this template use File | Settings | File Templates.
 */
public class MyNode extends Node {
	
	public static final int MAX_NICKNAME_LENGTH = 24;

    private CoreSubsystem core;
    private String ADMIN_FILE; 

    public MyNode(String nickname, int guid, CoreSubsystem core) {
        super(nickname, guid);
        this.core = core;
        ADMIN_FILE = core.getSettings().getInternal().getUserDirectory()+("admin");;
        AdminChecker a = new AdminChecker(core.getSettings().getMy().getNickname(), ADMIN_FILE);
    	this.adminCode = a.generateCode();
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public int getNumberOfInvitedFriends() {
        return core.getSettings().getMy().getInvitations();
    }

    @Override
    public boolean hasNotBeenOnlineForLongTime() {
        return false;
    }

    @Override
    public long getLastSeenOnlineAt() {
        return System.currentTimeMillis();
    }

    @Override
    public int getAllianceBuildNumber() {
        return Version.BUILD_NUMBER;
    }

    @Override
    public int getNumberOfFilesShared() {
        return core.getFileManager().getFileDatabase().getNumberOfShares();
    }

    @Override
    public long getShareSize() {
        return core.getFileManager().getFileDatabase().getShareSize();
    }

    @Override
    public double getHighestOutgoingCPS() {
        return core.getSettings().getInternal().getRecordoutspeed();
    }

    @Override
    public double getHighestIncomingCPS() {
        return core.getSettings().getInternal().getRecordinspeed();
    }

    @Override
    public long getTotalBytesSent() {
        return core.getNetworkManager().getBandwidthOut().getTotalBytes();
    }

    @Override
    public long getTotalBytesReceived() {
        return core.getNetworkManager().getBandwidthIn().getTotalBytes();
    }
    
    @Override
    public void setNickname(String name) {
    	if (canNickname(name)) {
    		this.nickname = name;
    	}
    }
    
    public boolean canNickname(String name) {
    	return name.length() <= MAX_NICKNAME_LENGTH
    		&& (testAdmin(name) || !core.getFriendManager().isAdminNick(name))
    		&& !core.getFriendManager().isSystem(name);	
    }
    private boolean testAdmin(String name) {
		AdminChecker test = new AdminChecker(name, ADMIN_FILE);
			return new AdminChecker(name, test.generateCode()).isTrueAdmin();
	}

	public int getAdminCode(){
    	return adminCode;
    }
	
    public boolean isAdmin() {
    	AdminChecker a = new AdminChecker(nickname, adminCode);
		return a.isTrueAdmin();
    }
}
