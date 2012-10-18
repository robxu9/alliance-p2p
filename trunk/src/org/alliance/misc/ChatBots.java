package org.alliance.misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import org.alliance.core.Language;

/**
 * TV Bot has been implemented with the TVRage API:
 * http://services.tvrage.com/tools/quickinfo.php?show=QUERY
 * Other possibilities include IMDB for movies:
 * http://www.omdbapi.com/?t=QUERY
 * and MAL API for anime:
 * http://mal-api.com/anime/search?q=QUERY
 */

public class ChatBots {
	private static String botMessage(Object message) {
		return "<SPAN STYLE=\"COLOR: #E0E0E0; BACKGROUND: #303030\">" + message.toString() + "</SPAN>";
	}
	
	private static String botValue(Object value) {
		// Most of the message was highlighted anyway, so the non-highlighted parts ended up standing out
		return /*"<SPAN STYLE=\"COLOR: #FF6600\">" +*/ value.toString() /*+ "</SPAN>"*/;
	}
	
	public static String tvBot(String show) throws IOException {
		// Get show data via TVRage API
		URL showURL = new URL("http://services.tvrage.com/tools/quickinfo.php?show=" + URLEncoder.encode(show, "ISO-8859-1"));
		InputStream data = showURL.openStream();
		// Initialize fields
		BufferedReader in = new BufferedReader(new InputStreamReader(data));
		String showName = "";
		String airTime = "";
		String network = "";
		String nextEpisode = "";
		String nextEpisodeDate = "";
		String ended = "";
		String linkURL = "";
		String daysUntilAirs = "";
		String hoursUntilAirs = "";
		String minutesUntilAirs = "";
		// Parse fields from data
		String line;
		while (!(((line = in.readLine()) == null))) {
			if (line.startsWith("No Show")) {
				return botMessage(Language.getLocalizedString(ChatBots.class, "noshow", botValue(show)));
			}
			else if (line.startsWith("Show Name@")) {
				showName = botValue(line.substring(line.indexOf("Show Name@") + "Show Name@".length()));
			}
			else if (line.startsWith("Airtime@")) {
				airTime = botValue(line.substring(line.indexOf("Airtime@") + "Airtime@".length()));
			}
			else if (line.startsWith("Network@")) {
				network = botValue(line.substring(line.indexOf("Network@") + "Network@".length()));
			}
			else if (line.startsWith("Next Episode@")) {
				nextEpisode = botValue(line.substring(line.indexOf("Next Episode@") + "Next Episode@".length(),
						line.lastIndexOf("^")).replaceAll("\\^", "&nbsp;"));
				nextEpisodeDate = botValue(line.substring(line.lastIndexOf("^")).replaceAll("\\^", " ").replaceAll("/", " ").trim());
			}
			else if (line.startsWith("GMT+0 NODST@")) {
				try {
					long epochAirtime = Integer.parseInt(line.substring(line.indexOf("GMT+0 NODST@") + "GMT+0 NODST@".length()));
					long currentTime = (System.currentTimeMillis() / 1000) - (2 * 60 * 60);
					int d = (int)Math.floor(((epochAirtime - currentTime) / (60 * 60 * 24)));
					int h = (int)(Math.floor(((epochAirtime - currentTime) / (60 * 60))) - (24 * d));
					int m = (int)(Math.floor(((epochAirtime - currentTime) / (60)) - (24 * 60 * d) - (60 * h)));
					daysUntilAirs = botValue(d + "d");
					hoursUntilAirs = botValue(h + "h");
					minutesUntilAirs = botValue(m + "m");
				}
				catch (NumberFormatException ex) {}
			}
			else if (line.startsWith("Ended@")) {
				ended = line.substring(line.indexOf("Ended@") + "Ended@".length()).trim();
			}
			else if (line.startsWith("Show URL@")) {
				linkURL = line.substring(line.indexOf("Show URL@") + "Show URL@".length());
			}
		}
		in.close();
		// Build output string from fields
		StringBuilder sb = new StringBuilder();
		sb.append("<a href=\"" + linkURL + "\">" + showName + "</a>");
		if (!ended.isEmpty()) {
			sb.append(": Ended on " + botValue(ended));
		}
		else {
			sb.append(": Airs " + airTime + " on " + network);
			sb.append("; " + nextEpisode + " airs " + nextEpisodeDate);
			if (!(daysUntilAirs.isEmpty() || hoursUntilAirs.isEmpty() || minutesUntilAirs.isEmpty())) {
				sb.append(" (" + daysUntilAirs + hoursUntilAirs + minutesUntilAirs + " from now)");
			}
		}
		return botMessage(sb.toString());
	}
}
