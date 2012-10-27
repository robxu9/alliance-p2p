package org.alliance.core.file.filedatabase;

import org.alliance.core.Language;

/**
 * Created by IntelliJ IDEA. User: maciek Date: 2006-mar-02 Time: 19:51:22 To
 * change this template use File | Settings | File Templates.
 */
public enum FileType {

	EVERYTHING("everything", 0, new FileTypeIdentifier() {
		@Override
		public boolean matches(String s) {
			return true;
		}
	}),
	
	AUDIO("audio", 1, new ExtensionFileType(new String[] {
			"mp3", "mp2", "mp1", "mpa", "aac", "m4a", "wma", "ra", "oga", "ogg",
			"mka", "wav", "wave", "au", "aif", "aiff", "aifc", "flac", "alac",
			"ape", "f4a", "ac3", "mid", "midi"
	})),
	
	VIDEO("video", 2, new ExtensionFileType(new String[] {
			"avi", "mkv", "mpg", "mpe", "mpv", "mpeg", "m1pg", "mpg2", "mpeg2",
			"mpeg4", "m15", "m21", "m1v", "m2v", "m4v", "mp21", "mp4", "mp4v",
			"h264", "mov", "movie", "vid", "video", "wmv", "wm", "wmx", "wvx",
			"rm", "rv", "rmd", "rms", "rmvb", "ogm", "ogv", "ogx", "asf", "asx",
			"divx", "xvid", "dvx", "flv", "f4v", "webm", "vob", "ts", "trp"
	})),
	
	CDDVD("cddvd", 3, new ExtensionFileType(new String[] {
			"iso", "bin", "cue", "000", "dmg", "dmgpart", "img", "ima", "image",
			"disk", "disc", "toast", "mdf", "mds", "mdx", "vcd", "vfd", "vhd",
			"vmdk", "vdi", "pvm", "rom", "gb", "sgb", "gbc", "gba", "nds", "nes",
			"smc", "n64", "z64", "v64", "gcm"
	})),
	
	ARCHIVE("archive", 4, new ExtensionFileType(new String[] {
			"zip", "zipx",  "rar", "sit", "sitx", "7z", "s7z", "cab", "bz",
			"bz2", "bzip2", "gz", "gzip", "lz", "lzma", "tar", "tz", "taz",
			"tgz", "tlz", "tpz", "txz", "z"
		}) {

		@Override
		public boolean matches(String s) {
			if (super.matches(s))
				return true;
			// match split files like .r00, .r01, .r02, etc
			int e = s.lastIndexOf('.');
			int n = s.length();
			if (e == -1 || n - e < 4)
				return false;
			char c = s.charAt(e + 1);
			if (c != 'a' && c != 'r' && c != 's' && c != 'z')
				return false;
			for (int i = e + 1; i < n; i++) {
				if (!Character.isDigit(s.charAt(i)))
					return false;
			}
			return true;
		}
	}),
	
	IMAGE("image", 5, new ExtensionFileType(new String[] {
			"jpg", "jpe", "jpeg", "jpc", "jpf", "jps", "jpx", "jp2", "j2k", "jng",
			"jxr", "j", "jif", "jiff", "pjpg", "pjpg", "ljp", "tjp", "gif", "png",
			"apng", "bmp", "tif", "tiff", "ppm", "pgm", "pbm", "pnm", "xbm",
			"xpm", "fig", "webp", "psp", "pspimage", "thm", "thumb", "rgb", "yuv",
			"ico", "icn", "icon", "psd", "psb", "svg", "svgz", "drw", "ai",
			"psid", "odg"
	})),
	
	DOCUMENT("document", 6, new ExtensionFileType(new String[] {
			"txt", "utxt", "nfo", "readme", "me", "1st", "now", "doc", "docx",
			"docm", "wp", "wpd", "wps", "rtf", "rtx", "tex", "ltx", "latex",
			"csv", "tsv", "psv", "skv", "log", "pdf", "ps", "eps"
	})),
	
	PRESENTATION("presentation", 7, new ExtensionFileType(new String[] {
			"ppt", "pptx", "pptm", "pps", "pps", "ppsx", "ppsm", "pot", "potx",
			"potm", "odp", "key", "sdp", "sdd"
	})),
	
	SPREADSHEET("spreadsheet", 8, new ExtensionFileType(new String[] {
			"xls", "xlsx", "xlsm", "xlsb", "xlr", "xl", "wks", "ods", "sdc",
			"nb", "numbers"
	})),
	
	EBOOK("ebook", 9, new ExtensionFileType(new String[] {
			"lit", "epub", "mobi", "prc", "fb2", "mbp"
	})),
	
	COMIC("comic", 10, new ExtensionFileType(new String[] {
			"cbz", "cbr", "cbt", "cb7"
	})),
	
	PLAYLIST("playlist", 11, new ExtensionFileType(new String[] {
			"pls", "m3u", "wpl", "zpl", "fpl"
	})),
	
	SUBTITLE("subtitle", 12, new ExtensionFileType(new String[] {
			"srt", "idx", "sub", "ass", "ssa", "smi", "sami", "usf", "ssf"
	})),
	
	SCRIPT("script", 13, new ExtensionFileType(new String[] {
			"bat", "hta", "js", "jse", "vb", "vbe", "cgi", "pl", "rb", "py",
			"ws", "wsf", "applescript", "ahk", "scr", "script"
	})),
	
	EXECUTABLE("executable", 14, new ExtensionFileType(new String[] {
			"exe", "com", "command", "reg", "gadget", "app", "ipa", "out", "msi",
			"pkg", "deb", "rpm", "apk", "xap", "air", "xpi", "jar"
	})),
	
	TORRENT("torrent", 15, new ExtensionFileType(new String[] {
			"torrent", "alliance"
	})),
	
	TEMPORARY("temporary", 16, new ExtensionFileType(new String[] {
			"tmp", "temp", "part", "partial", "dtapart", "ds_store", "!ut",
			"!bt", "bc!", "fb!", "jc!", "ob!"
		}) {

		@Override
		public boolean matches(String s) {
			return super.matches(s) || s.endsWith("~")
					|| s.equals("desktop.ini") || s.equals("thumbs.db") || s.equals(".TemporaryItems")
					|| s.equals(".cedata") || s.startsWith(".");
		}
	});
	
	private final String description;
	private final byte id;
	private final FileTypeIdentifier fileTypeIdentifier;

	FileType(String description, int id, FileTypeIdentifier fileTypeIdentifier) {
		this.description = Language.getLocalizedString(getClass(), description);
		this.id = (byte) id;
		this.fileTypeIdentifier = fileTypeIdentifier;
	}

	public byte id() {
		return id;
	}

	public FileTypeIdentifier fileTypeIdentifier() {
		return fileTypeIdentifier;
	}

	public String description() {
		return description;
	}

	public static int indexOf(FileType ft) {
		for (int i = 0; i < values().length; i++) {
			if (ft == values()[i]) {
				return i;
			}
		}
		return -1;
	}

	public static FileType getFileTypeById(int id) {
		for (int i = 0; i < values().length; i++) {
			if (id == values()[i].id) {
				return values()[i];
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "FileType " + description + " (" + id + ")";
	}

	public static FileType getByFileName(String filename) {
		for (FileType t : values()) {
			if (t == EVERYTHING) {
				continue;
			}
			if (t.fileTypeIdentifier().matches(filename)) {
				return t;
			}
		}
		return EVERYTHING;
	}
}
