package org.alliance.ui;


import org.alliance.core.CoreSubsystem;
import org.alliance.core.PacedRunner;
import org.alliance.core.node.Friend;
import org.alliance.core.Language;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;
import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;

/**
 * Created by IntelliJ IDEA.
 * User: maciek
 * Date: 2006-mar-27
 * Time: 19:50:47
 */
public class FriendListModel extends DefaultListModel {
	private static final long serialVersionUID = 2341832448515812151L;
	
	private boolean ignoreFires;
    private CoreSubsystem core;
    private UISubsystem ui;
    private PacedRunner pacedRunner;
    private TreeSet<Friend> friendsSortedByName;
    private TreeSet<Friend> friendsSortedBySize;
    private TreeSet<String> groupsSorted;
    private ArrayList<String> hiddenGroups;
    private ArrayList<Friend> friends;
    private boolean sortSize = false;

    public FriendListModel(CoreSubsystem core, final UISubsystem ui) {
        this.core = core;
        this.ui = ui;

        pacedRunner = new PacedRunner(new Runnable() {

            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        updateFriendList();
                    }
                });
            }
        }, 1000);

        hiddenGroups = new ArrayList<String>();

        friendsSortedByName = new TreeSet<Friend>(new Comparator<Friend>() {
            @Override
            public int compare(Friend o1, Friend o2) {
                String s1 = o1.getNickname();
                String s2 = o2.getNickname();
                if (s1.equalsIgnoreCase(s2)) {
                    return o1.getGuid() - o2.getGuid();
                }
                return o1.getNickname().compareToIgnoreCase(o2.getNickname());
            }
        });
        
        friendsSortedBySize = new TreeSet<Friend>(new Comparator<Friend>() {
            @Override
            public int compare(Friend o1, Friend o2) {
            	long s1 = o1.getShareSize();
            	long s2 = o2.getShareSize();
                if (s1 == s2) {
                    return o1.getGuid() - o2.getGuid();
                }
                return s1 < s2 ? 1 : -1; // largest on top
            }
        });

        groupsSorted = new TreeSet<String>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (o1.equalsIgnoreCase(o2)) {
                    return 0;
                }
                return o1.compareToIgnoreCase(o2);
            }
        });
        updateFriendList();
    }

    public void updateFriendList() {
        ignoreFires = true;

        friends = new ArrayList<Friend>(core.getFriendManager().friends());
        for (Friend f : friends) {
            if (f != null) {
                friendsSortedByName.add(f);
                friendsSortedBySize.add(f);
                if (!f.getUGroupName().isEmpty()) {
                    groupsSorted.add(f.getUGroupName());
                }
            }
        }
        friends.clear();
        runSort();
        friendsSortedByName.clear();
        friendsSortedBySize.clear();

        clear();
        //Draw myself
        addElement(core.getFriendManager().getMe());
        //Draw custom groups
        if (groupsSorted.size() > 0) {
            for (String group : groupsSorted) {
                drawGroups(group, false);
            }
        }
        groupsSorted.clear();
        //Draw no groups
        drawGroups(Language.getLocalizedString(getClass(), "nogroup"), true);

        friends.clear();
        ignoreFires = false;
        fireIntervalAdded(this, 0, size() - 1);
    }

    private void drawGroups(String group, boolean noGroup) {
        int groupPosition = getSize();
        int totalFriends = 0;
        int onlineFriends = 0;
        for (Friend f : friends) {
            if (f.getUGroupName().equals(group) || (f.getUGroupName().isEmpty() && noGroup)) {
                if (!hiddenGroups.contains(group)) {
                    addElement(f);
                }
                totalFriends++;
                if (f.isConnected()) {
                    onlineFriends++;
                }
            }
        }
        add(groupPosition, group + " (" + onlineFriends + "/" + totalFriends + ")");
    }
   
    public boolean getSort(){
    	return sortSize;
    }
    
    public void setSortSize(boolean type){
    	sortSize = type;
    }
    
    private void runSort() {
    	TreeSet<Friend> friendsSorted = getSort() ? friendsSortedBySize : friendsSortedByName;
        int onlineEnd = 0;
        int offlineEnd = 0;
        for (Friend f : friendsSorted) {
            if (f.isConnected()) {
                friends.add(onlineEnd, f);
                onlineEnd++;
                offlineEnd++;
            }
            else if (!f.hasNotBeenOnlineForLongTime()) {
                friends.add(offlineEnd, f);
                offlineEnd++;
            }
            else {
                friends.add(f);
            }
        }
    }

    public void signalFriendChanged() {
        pacedRunner.invoke();
    }

    public void signalFriendAdded() {
        pacedRunner.invoke();
    }

    public void changeHiddenGroups(String group) {
        group = group.substring(0, group.lastIndexOf(" "));
        if (hiddenGroups.contains(group)) {
            hiddenGroups.remove(group);
        } else {
            hiddenGroups.add(group);
        }
        updateFriendList();
    }

    @Override
    protected void fireContentsChanged(Object source, int index0, int index1) {
        if (ignoreFires) {
            return;
        }
        super.fireContentsChanged(source, index0, index1);
    }

    @Override
    protected void fireIntervalAdded(Object source, int index0, int index1) {
        if (ignoreFires) {
            return;
        }
        super.fireIntervalAdded(source, index0, index1);
    }

    @Override
    protected void fireIntervalRemoved(Object source, int index0, int index1) {
        if (ignoreFires) {
            return;
        }
        super.fireIntervalRemoved(source, index0, index1);
    }
}
