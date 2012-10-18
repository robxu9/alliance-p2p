package org.alliance.misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

public class ChatBots {
	private static String botMessage(Object message) {
		return "<SPAN STYLE=\"COLOR: #458B00; BACKGROUND: #000000\">" + message.toString() + "</SPAN>";
	}
	
	private static String botValue(Object value) {
		return "<SPAN STYLE=\"COLOR: #FF6600\">" + value.toString() + "</SPAN>";
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
		int daysUntilAirs = 0;
		int hoursUntilAirs = 0;
		int minutesUntilAirs = 0;
		// Parse fields from data
		String line;
		while (!(((line = in.readLine()) == null))) {
			if (line.startsWith("No Show")) {
				// TODO: localize this string
				return botValue("No such show!");
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
				nextEpisodeDate = botValue(line.substring(line.lastIndexOf("^")).trim().replaceAll("\\^", "&nbsp;"));
			}
			else if (line.startsWith("GMT+0 NODST@")) {
				long epochAirtime = Integer.parseInt(line.substring(line.indexOf("GMT+0 NODST@") + "GMT+0 NODST@".length()));
				long currentTime = (System.currentTimeMillis() / 1000) - (2 * 60 * 60);
				daysUntilAirs = (int)Math.floor(((epochAirtime - currentTime) / (60 * 60 * 24)));
				hoursUntilAirs = (int)(Math.floor(((epochAirtime - currentTime) / (60 * 60))) - (24 * daysUntilAirs));
				minutesUntilAirs = (int)(Math.floor(((epochAirtime - currentTime) / (60)) - (24 * 60 * daysUntilAirs) - (60 * hoursUntilAirs)));
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
		sb.append("[<a href=\"" + linkURL + "\">" + showName + "</a>]");
		if (!ended.isEmpty()) {
			sb.append(" :: [Ended: " + botValue(ended) + "]");
		}
		else {
			sb.append(" :: [Airs: " + airTime + " on " + network + "]");
			sb.append(" :: [" + botValue(nextEpisodeDate) + " " + botValue(daysUntilAirs) + "d" + botValue(hoursUntilAirs) + "h" +
					botValue(minutesUntilAirs) + "m from now]");
			sb.append(" :: [" + nextEpisode + "]");
		}
		return botMessage(sb.toString());
	}
}
