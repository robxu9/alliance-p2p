package org.alliance.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: maciek
 * Date: 2007-jan-12
 * Time: 15:24:52
 */
public class PublicChatHistory implements Serializable {
	private static final long serialVersionUID = 3573220861823618270L;
	
	private static final int NUMBERS_OF_MESSAGES_IN_HISTORY = 500;

    public static class Entry implements Serializable {
		private static final long serialVersionUID = -5500094852266772045L;
		
		public long tick;
        public int fromGuid;
        public String message;

        public Entry(long tick, int fromGuid, String message) {
            this.tick = tick;
            this.fromGuid = fromGuid;
            this.message = message;
        }
    }
    private ArrayList<Entry> chatMessages = new ArrayList<Entry>();

    public void addMessage(long tick, int fromGuid, String message) {
        chatMessages.add(new Entry(tick, fromGuid, message));
        if (chatMessages.size() > NUMBERS_OF_MESSAGES_IN_HISTORY) {
            chatMessages.remove(0);
        }
    }

    public void clearHistory() {
        while (chatMessages.size() > 0) {
            chatMessages.remove(0);
        }
    }

    public Collection<Entry> allMessages() {
        return chatMessages;
    }
}
