package org.alliance.misc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.alliance.core.CoreSubsystem;
import org.alliance.core.NeedsUserInteraction;
import org.alliance.core.NonWindowUICallback;
import org.alliance.core.comm.SearchHit;
import org.alliance.core.node.Friend;
import org.alliance.core.node.Node;
import org.alliance.core.plugins.ConsolePlugInExtension;
import org.alliance.core.plugins.PlugIn;
import org.alliance.launchers.console.Console.Printer;

/**
 * PlugIn to test the PlugIn and UICallback systems
 *
 * We're trying to enforce the following design:
 * <ul>
 * <li>Make the PlugIn system initialize and shutdown all PlugIns and UICallbacks.</li>
 * <li>Multiple loads and unloads of the core system or the UI (independently) will not affect the state of UICallbacks.</li>
 * </ul>
 *
 * In order to test this, run in these modes:
 * <ul>
 * <li>standard: org.alliance.launchers.ui.Main</li>
 * <li>testsuite: run 'ant-plugin-jar', then GenerateTestSuite, then org.alliance.launchers.testsuite.Main</li>
 * <li>console: org.alliance.launchers.console.Main</li>
 * </ul>
 *
 * ... and try various combinations of the following:
 * <ul>
 * <li>all: remove the alliance-test-plugin.jar and then add it, at least twice; result is exception-free</li>
 * <li>testsuite only: Open UI & Stop, and for multiple people; result is exception-free</li>
 * <li>testsuite only: Start & Stop, and for multiple people; result is exception-free</li>
 * <li>console only: run 'ui' and 'killui'; result is exception-free</li>
 * <li>all: at startup plugins are initialized and at exit they're shutdown; don't have test result for this</li>
 * <li>all: in the Console, type 'TestPlugIn' and see it call something (or not... why doesn't this work?!)</li>
 * </ul>
 *
 */
public class TestPlugIn implements PlugIn {

    private static boolean everInitialized = false;

    private CoreSubsystem core;
    private String nickname;
    private TestUICallback testCallback;



    public static void checkInitialized() {
        if (!everInitialized) {
            throw new IllegalStateException("The TestPlugIn was not ever initialized by core init.");
        }
    }




    public void init(CoreSubsystem core) throws Exception {

        everInitialized = true;
        
        this.core = core;
        this.nickname = core.getSettings().getMy().getNickname();

        if (testCallback == null ) {
            testCallback = new TestUICallback();
            core.addUICallback(testCallback);
        }
        // init, and check to make sure this isn't a repeated init
        if (testCallback.active) {
            throw new IllegalStateException("PlugIns have been initialized a second time without any shutdown call.");
        } else {
            testCallback.active = true;
        }
    }

    public void shutdown() throws Exception {

        if (testCallback.active) {
            testCallback.active = false;
            hashCodeOfLastActiveCallbackInvokedForMain.remove(nickname);
        } else {
            // don't worry if multiple shutdowns are called (at least for now)
        }
    }









    public ConsolePlugInExtension getConsoleExtensions() {
        return new ConsolePlugInExtension() {
            public boolean handleLine(String line, Printer print) {
                if (line.equals("TestPlugIn")) {
                    hashCodeOfLastActiveCallbackInvokedForMain.remove(nickname);
                    // run any callback method to force it through the chain
                    core.getUICallback().isUIVisible();
                    print.println("Finished calling UICallback method.");
                    // now check that the callback for this got called
                    if (hashCodeOfLastActiveCallbackInvokedForMain.get(nickname) == null) {
                        throw new IllegalStateException("The TestUICallback method wasn't called.");
                    }
                    return true;
                } else {
                    return false;
                }
            }
        };
    }






    public static Map<String,Integer> hashCodeOfLastActiveCallbackInvokedForMain = new HashMap<String,Integer>();

    public class TestUICallback extends NonWindowUICallback {
        public boolean active = false;
        
        public void testDuplicateCallbacksInChain() {
            //System.out.println("-------------------- TestUICallback for " + nickname + " was called!  Active: " + active + "  hash: " + this.hashCode() + "  Method: " + Thread.currentThread().getStackTrace()[2]);
            if (active) {
                System.out.println("Successful call by " + nickname + " (" + this.hashCode() + ") to method " + Thread.currentThread().getStackTrace()[2]);
                Integer lastCallbackHash = hashCodeOfLastActiveCallbackInvokedForMain.get(nickname);
                if (lastCallbackHash != null
                    && lastCallbackHash.intValue() != this.hashCode()) {
                    throw new IllegalStateException("We've got multiple active TestUICallback objects for " + nickname + ": " + lastCallbackHash.intValue() + " != " + this.hashCode() + ".");
                }
                hashCodeOfLastActiveCallbackInvokedForMain.put(nickname, this.hashCode());
            }
        }




        @Override
        public void nodeOrSubnodesUpdated(Node node) {
            testDuplicateCallbacksInChain();
        }

        @Override
        public void noRouteToHost(Node node) {
            testDuplicateCallbacksInChain();
        }

        @Override
        public void pluginCommunicationReceived(Friend source, String data) {
            testDuplicateCallbacksInChain();
        }

        @Override
        public void searchHits(int srcGuid, int hops, List<SearchHit> hits) {
            testDuplicateCallbacksInChain();
        }

        @Override
        public void trace(int level, String message, Exception stackTrace) {
            testDuplicateCallbacksInChain();
        }

        @Override
        public void handleError(Throwable e, Object source) {
            testDuplicateCallbacksInChain();
        }

        @Override
        public void statusMessage(String s) {
            testDuplicateCallbacksInChain();
        }

        @Override
        public void statusMessage(String s, boolean b) {
            testDuplicateCallbacksInChain();
        }

        @Override
        public void toFront() {
            testDuplicateCallbacksInChain();
        }

        @Override
        public void signalFriendAdded(Friend friend) {
            testDuplicateCallbacksInChain();
        }

        @Override
        public boolean isUIVisible() {
            testDuplicateCallbacksInChain();
            return false;
        }

        @Override
        public void logNetworkEvent(String event) {
            testDuplicateCallbacksInChain();
        }

        @Override
        public void receivedShareBaseList(Friend friend, String[] shareBaseNames) {
            testDuplicateCallbacksInChain();
        }

        @Override
        public void receivedDirectoryListing(Friend friend, int i, String s, TreeMap<String, Long> fileSize) {
            testDuplicateCallbacksInChain();
        }

        @Override
        public void newUserInteractionQueued(NeedsUserInteraction ui) {
            testDuplicateCallbacksInChain();
        }

        @Override
        public void firstDownloadEverFinished() {
            testDuplicateCallbacksInChain();
        }

        @Override
        public void callbackRemoved() {
            testDuplicateCallbacksInChain();
        }

    }

}
