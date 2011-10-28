package org.alliance.ui.windows.mdi.chat;

import org.alliance.Version;
import org.alliance.core.Language;
import org.alliance.core.comm.rpc.ChatMessage;
import org.alliance.core.node.Friend;
import org.alliance.ui.UISubsystem;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: maciek
 * Date: 2006-jan-05
 * Time: 13:21:55
 * To change this template use File | Settings | File Templates.
 */
public class PublicChatMessageMDIWindow extends AbstractChatMessageMDIWindow {
	private static final long serialVersionUID = 5514523640925257976L;

	public PublicChatMessageMDIWindow(UISubsystem ui) throws Exception {
        super(ui.getMainWindow().getMDIManager(), "publicchat", ui);
        Language.translateXUIElements(getClass(), xui.getXUIComponents());
        setTitle(Language.getLocalizedString(getClass(), "title"));

        postInit();
    }

    @Override
    public void send(final String text) throws Exception {
    //Cannot public chat if silenced
    if(!ui.getCore().getFriendManager().getMe().isSilenced()){
    	if (text == null || text.trim().length() == 0) {
            return;
        }
        ui.getCore().invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    for (Friend f : ui.getCore().getFriendManager().friends()) {
                    	if(f.getAllianceBuildNumber() < Version.BUILD_NUMBER){
                    		//If friend is using old Version, send two messages
                    		//One with the actually message, and another stating to update
                    		 ui.getCore().getFriendManager().getNetMan().sendPersistently(new ChatMessage(Language.getLocalizedString(getClass(), "pleaseupdate", Version.VERSION), true), f);
                    	}
                        ui.getCore().getFriendManager().getNetMan().sendPersistently(new ChatMessage(text, true), f);
                    }
                } catch (IOException e) {
                    ui.getCore().reportError(e, this);
                }
            }
        });
        chat.setText("");
        ui.getMainWindow().publicChatMessage(ui.getCore().getFriendManager().getMe().getGuid(), text, System.currentTimeMillis(), false);
    }
    else{
		addSystemMessage(Language.getLocalizedString(getClass(), "silenced"));
	}
    }

    @Override
    public String getIdentifier() {
        return "publicchat";
    }
}
