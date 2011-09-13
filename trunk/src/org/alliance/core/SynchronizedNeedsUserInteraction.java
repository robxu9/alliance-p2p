package org.alliance.core;

/**
 * Created by IntelliJ IDEA.
 * User: maciek
 * Date: 2006-jul-11
 * Time: 14:25:51
 */
public abstract class SynchronizedNeedsUserInteraction implements NeedsUserInteraction {
	private static final long serialVersionUID = 9093459328431178801L;

	@Override
    public boolean canRunInParallelWithOtherInteractions() {
        return false;
    }
}

