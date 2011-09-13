package org.alliance.core.file.filedatabase;

/**
 * Created by IntelliJ IDEA.
 * User: maciek
 * Date: 2006-feb-18
 * Time: 11:17:52
 * To change this template use File | Settings | File Templates.
 */
public class FileHasBeenRemovedOrChanged extends Exception {
	private static final long serialVersionUID = 1050247596070657837L;
	
	private FileDescriptor fd;

    public FileHasBeenRemovedOrChanged(FileDescriptor fd) {
        this.fd = fd;
    }

    public FileDescriptor getFd() {
        return fd;
    }

    public void setFd(FileDescriptor fd) {
        this.fd = fd;
    }
}
