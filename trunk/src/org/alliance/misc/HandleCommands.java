package org.alliance.misc;

import org.alliance.core.Language;
import org.alliance.core.node.Friend;
import org.alliance.ui.UISubsystem;
import org.alliance.misc.UserCommands;

public class HandleCommands {

	private int guid;
	private String message;
	private Friend friend;
	private UISubsystem ui;
	public HandleCommands(int guid, String message, UISubsystem ui){
		this.guid = guid;
		this.message = message;
		this.ui = ui;
		this.friend = ui.getCore().getFriendManager().getFriend(guid);
		if(isAdminCommand()){
			handleAdminCommand();
		}
	}
	
	private boolean isAdminCommand() {
		return friend.isAdmin() && message.startsWith(UserCommands.ADMIN_COMMAND);
	}
	
	private void handleAdminCommand() {
		guid = 0;// System message
		message = message.substring(UserCommands.ADMIN_COMMAND.length()).trim();
		// silence
		if (message.startsWith(UserCommands.SILENCE_COMMAND)) {
			String name = message.substring(UserCommands.SILENCE_COMMAND.length()).trim();
			// you were silenced
			if (ui.getCore().getSettings().getMy().getNickname().equals(name)) {
				ui.getCore().getSettings().getMy().setSilenced(true);
				message = Language.getLocalizedString(getClass(), "silenced");
			}
			// your friend was silenced
			else {
				Friend friend = ui.getCore().getFriendManager().getFriend(name);
				if (friend == null) {
					return;
				}
				else {
					ui.getCore().getSettings().getMy().addIgnore(friend.getGuid());
					message = Language.getLocalizedString(getClass(), "silenced_friend", friend.getNickname());
				}
             }
		}
		// unsilence
		else if (message.startsWith(UserCommands.SILENCE_COMMAND)) {
			String name = message.substring(UserCommands.SILENCE_COMMAND.length()).trim();
			// you were unsilenced
			if (ui.getCore().getSettings().getMy().getNickname().equals(name)){
				ui.getCore().getSettings().getMy().setSilenced(false);
				message = Language.getLocalizedString(getClass(), "unsilenced");
			}
			// your friend was unsilenced
			else {
				Friend friend = ui.getCore().getFriendManager().getFriend(name);
				if (friend == null) {
					return;
				}
				else {
					ui.getCore().getSettings().getMy().removeIgnore(friend.getGuid());
					message = Language.getLocalizedString(getClass(), "friend_unsilenced", friend.getNickname());
				}
             }
		}
		// ban
		else if (message.startsWith(UserCommands.BAN_COMMAND)) {
			String name = message.substring(UserCommands.BAN_COMMAND.length()).trim();
			// you were banned
			if (ui.getCore().getSettings().getMy().getNickname().equals(name)) {
				message = Language.getLocalizedString(getClass(), "banned");
			}
			// your friend was banned
			else {
				Friend friend = ui.getCore().getFriendManager().getFriend(name);
				if (friend == null) {
					return;
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

	public int getGuid() {
		return guid;
	}
	
	public String getMessage() {
		return message;
	}

}
