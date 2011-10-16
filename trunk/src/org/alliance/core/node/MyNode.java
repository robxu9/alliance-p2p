package org.alliance.core.node;

import org.alliance.core.CoreSubsystem;
import org.alliance.Version;

/**
 * Created by IntelliJ IDEA.
 * User: maciek
 * Date: 2006-jan-03
 * Time: 14:38:28
 * To change this template use File | Settings | File Templates.
 */
public class MyNode extends Node {

    private CoreSubsystem core;

    public MyNode(String nickname, int guid, CoreSubsystem core) {
        super(nickname, guid);
        this.core = core;
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
    public void setNickname(String name){
    	if(canNickname(name)){
    	this.nickname = name;
    	}
    }
    
    public boolean canNickname(String name){
    	 if(name.contains(" ")){
          	return false;
          }
    	 else if(name.length() >= 22){
    		 return false;
    	 }
          else{
          	int hash = name.hashCode();
          	int[] admin = core.getFriendManager().getAdmin();
          	for(int i = 0; i < admin.length; i++)
          	{
          		if(hash == admin[i]){
          			return false;
          		}
          	}
          }
    	 return true;
    	
    }
    
 }
