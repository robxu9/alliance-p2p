package org.alliance.misc;

import org.alliance.core.Language;
import org.alliance.core.node.Friend;
import org.alliance.ui.UISubsystem;
import org.alliance.misc.UserCommands;
/**
 * 
 * @author Spiderman
 * This method handles incoming messages and checks if they're admin commands
 * If they are commands, it carries those commands out.
 * If it is not a command, it checks if that user should be ignored.
 *
 */
public class HandleCommands {

	private int guid;
	private String message;
	private Friend from;
	private UISubsystem ui;
	private boolean ignored = false;
	
	public HandleCommands(int guid, String message, UISubsystem ui){
		this.guid = guid;
		this.message = message;
		this.ui = ui;
		this.from = ui.getCore().getFriendManager().getFriend(guid);
		if(isAdminCommand()) {
			handleAdminCommand();
		}
		else {
			checkIgnored();
		}
	}
	
	private boolean isAdminCommand() {
		UserCommands[] cmds = UserCommands.values();
		if(!(from.isAdmin())) {
			return false;
		}
		else {
			for(UserCommands c : cmds){
				if(message.startsWith(c.getKey()) && c.isAdminOnly()){
					return true;
				}
			}
		}
		
		return false;
	}
	
	private void handleAdminCommand() {
		guid = 0;// System message
		UserCommands[] cmds = UserCommands.values();
		UserCommands command = null;
		String name = "";
		for(UserCommands c : cmds){
			if(message.startsWith(c.getKey())){
				name = message.substring(c.getKey().length()).trim();
				command = c;
				break;
			}
		}
		
		Friend friend = ui.getCore().getFriendManager().getFriend(name);

		
		// silence
		if (command == UserCommands.SILENCE) {
			// you were silenced
			if (ui.getCore().getSettings().getMy().getNickname().equals(name)) {
				ui.getCore().getSettings().getMy().setSilenced(true);
				message = Language.getLocalizedString(getClass(), "silenced");
			}
			// your friend was silenced
			else {
				if (friend == null) {
					ignored = true;
				}
				else {
					ui.getCore().getSettings().getMy().addIgnore(friend.getGuid());
					message = Language.getLocalizedString(getClass(), "silenced_friend", friend.getNickname());
				}
             }
		}
		// unsilence
		else if (command == UserCommands.UNSILENCE) {
			// you were unsilenced
			if (ui.getCore().getSettings().getMy().getNickname().equals(name)){
				ui.getCore().getSettings().getMy().setSilenced(false);
				message = Language.getLocalizedString(getClass(), "unsilenced");
			}
			// your friend was unsilenced
			else {
				if (friend == null) {
					ignored = true;
				}
				else {
					ui.getCore().getSettings().getMy().removeIgnore(friend.getGuid());
					message = Language.getLocalizedString(getClass(), "friend_unsilenced", friend.getNickname());
				}
             }
		}
		// ban
		else if (command == UserCommands.BAN) {
			// you were banned
			if (ui.getCore().getSettings().getMy().getNickname().equals(name)) {
				message = Language.getLocalizedString(getClass(), "banned");
			}
			// your friend was banned
			else {
				if (friend == null) {
					ignored = true;
				}
				else {
					//Adds banned users IP to Blacklist
					try {
						ui.getCore().getSettings().getRulelist().add("DENY" + friend.getFriendConnection().getSocketAddress());
					} catch (Exception e) {
						e.printStackTrace();
					}
					ui.getCore().getFriendManager().permanentlyRemove(friend);
					message = Language.getLocalizedString(getClass(), "friend_banned", friend.getNickname());
				}
				}
			}

	}
	
    private void checkIgnored() {
    	if (from != null) {
    		this.ignored = ui.getCore().getSettings().getMy().getIgnoreList().contains(from.getGuid());
    	}
	}
    
    public boolean isIgnored() {
    	return ignored;
    }

	public int getGuid() {
		return guid;
	}
	
	public String getMessage() {
		return message;
	}

}
