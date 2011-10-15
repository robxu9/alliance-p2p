package org.alliance.misc;

import org.alliance.core.CoreSubsystem;
import org.alliance.core.Language;

public class UserCommands {
	
	protected final String COMMANDS[] = {"/me"};
	protected CoreSubsystem core;
	 
	public UserCommands(CoreSubsystem core){
		this.core = core;
	}
	public int getCommand(String message){
		for(int i = 0; i < COMMANDS.length; i++){
		if(message.startsWith(COMMANDS[i] + " ")){
			return i;
		}
		}
		return -1;
	}
	
	public String handleCommand(String from, String message)
	{
		int i = getCommand(message);
		String s = "";
		if(i != -1){
			switch(i){
				case 0: s = setStatus(from, message);
						break;
					}
		}
		return s;
	}

	public String setStatus(String from, String message){
		String status = "";
		if(message.toLowerCase().startsWith("/me ")){
			if(message.length() < 145 && isOwnMessage(from)){
				core.getSettings().getMy().setCurrentStatus(message.substring(message.indexOf(" ")));
				status =("<i>"+ message.substring(message.indexOf(" ")) + "</i></font><br>");
				}
			else if(message.length() > 145 && isOwnMessage(from)){
				status = (Language.getLocalizedString(getClass(), "overstatus", Integer.toString(message.length() - 145)) + "</font><br>");
				}
			else if(message.length() < 145){
				status = ("<i>"+ message.substring(message.indexOf(" ")) + "</i></font><br>");
				}
			}
		return status;
	}
	protected boolean isOwnMessage(String from){
		if(core.getSettings().getMy().getNickname().equals(from)){
			return true;
		}
		return false;
	}
}
