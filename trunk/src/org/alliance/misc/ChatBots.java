package org.alliance.misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alliance.core.Language;

// @TODO: This should be ENUM'ed like the UserCommands class.

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
		URLConnection con = showURL.openConnection();
	    con.setConnectTimeout(10000); //10 second connection timeout
	    con.setReadTimeout(5000); //5 second read timeout
		InputStream data = con.getInputStream();
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
		while ((line = in.readLine()) != null) {
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
			sb.append(" | " + nextEpisode + " airs " + nextEpisodeDate);
			if (!(daysUntilAirs.isEmpty() || hoursUntilAirs.isEmpty() || minutesUntilAirs.isEmpty())) {
				sb.append(" (" + daysUntilAirs + hoursUntilAirs + minutesUntilAirs + " from now)");
			}
		}
		return botMessage(sb.toString());
	}
	
	public static String movieBot(String movie) throws IOException {
		// Get movie data via IMDB API
		// If this ever breaks, we can use OMDB: http://www.deanclatworthy.com/imdb/?q=QUERY
		URL movieURL = new URL("http://www.omdbapi.com/?t=" + URLEncoder.encode(movie, "ISO-8859-1"));
		URLConnection con = movieURL.openConnection();
	    con.setConnectTimeout(10000); //10 second connection timeout
	    con.setReadTimeout(5000); //5 second read timeout
		InputStream data = con.getInputStream();
		// Initialize fields
		BufferedReader in = new BufferedReader(new InputStreamReader(data));
		// Parse fields from data
		String lines = "";
		String line;
		while ((line = in.readLine()) != null) {
			lines += line;
		}
		in.close();
		// Check for error
		Matcher errorRegex = Pattern.compile("\"error\":\"([^\"]+)\"", Pattern.CASE_INSENSITIVE).matcher(lines);
		if (errorRegex.find()) {
			return movieBotv2(movie);
		}
		// Parse title
		String title = "";
		Matcher titleRegex = Pattern.compile("\"title\":\"([^\"]+)\"", Pattern.CASE_INSENSITIVE).matcher(lines);
		if (titleRegex.find()) {
			title = titleRegex.group(1);
		}
		title = botValue(title);
		// Parse runtime
		String runtime = "";
		Matcher runtimeRegex = Pattern.compile("\"runtime\":\"([^\"]+)\"", Pattern.CASE_INSENSITIVE).matcher(lines);
		if (runtimeRegex.find()) {
			runtime = runtimeRegex.group(1);
		}
		runtime = botValue(runtime);	
		//Parse MPAA Rating
		String rating = "";
		Matcher ratingRegex = Pattern.compile("\"rated\":\"([^\"]+)\"", Pattern.CASE_INSENSITIVE).matcher(lines);
		if (ratingRegex.find()) {
			rating = ratingRegex.group(1);
		}
		rating = botValue(rating);
		//Parse IMDB Rating
		String imdbRating = "";
		Matcher imdbRatingRegex = Pattern.compile("\"imdbrating\":\"([^\"]+)\"", Pattern.CASE_INSENSITIVE).matcher(lines);
		if (imdbRatingRegex.find()) {
			imdbRating = imdbRatingRegex.group(1);
		}
		imdbRating = botValue(imdbRating);
		//Parse IMDB ID
		String imdbID = "";
		Matcher imdbIDRegex = Pattern.compile("\"imdbid\":\"([^\"]+)\"", Pattern.CASE_INSENSITIVE).matcher(lines);
		if (imdbIDRegex.find()) {
			imdbID = imdbIDRegex.group(1);
		}
		imdbID = botValue(imdbID);
		// Parse year
		String year = "";
		Matcher yearRegex = Pattern.compile("\"year\":\"([^\"]+)\"", Pattern.CASE_INSENSITIVE).matcher(lines);
		if (yearRegex.find()) {
			year = yearRegex.group(1);
			if (year.isEmpty() || year.equals("n\\/a")) {
				year = "N/A";
			}
		}
		year = botValue(year);
		
		// Build output string from fields
		StringBuilder sb = new StringBuilder();
		sb.append("<a href=\"" + "http://www.imdb.com/title/"+ imdbID + "\">" + title + "</a>");
		sb.append(": Released in " + year);
		sb.append(" | Runtime: " + runtime);
		sb.append(" | Rated: " + rating);
		sb.append(" | IMDB Rating: " + imdbRating);
	
		return botMessage(sb.toString());
	}
	
	private static String movieBotv2 (String movie) throws IOException{
			// Get movie data via IMDB API
			// If this ever breaks, we can use OMDB: http://www.deanclatworthy.com/imdb/?q=
			URL movieURL = new URL("http://www.omdbapi.com/?t=" + URLEncoder.encode(movie, "ISO-8859-1"));
			URLConnection con = movieURL.openConnection();
		    con.setConnectTimeout(10000); //10 second connection timeout
		    con.setReadTimeout(5000); //5 second read timeout
			InputStream data = con.getInputStream();
			// Initialize fields
			BufferedReader in = new BufferedReader(new InputStreamReader(data));
			// Parse fields from data
			String lines = "";
			String line;
			while ((line = in.readLine()) != null) {
				lines += line;
			}
			in.close();
			// Check for error
			Matcher errorRegex = Pattern.compile("\"error\":\"([^\"]+)\"", Pattern.CASE_INSENSITIVE).matcher(lines);
			if (errorRegex.find()) {
				String error = errorRegex.group(1);
				return botMessage(Language.getLocalizedString(ChatBots.class, "nomovie", botValue(movie), botValue(error)));
			}
			// Parse link URL
			String linkURL = "";
			Matcher linkURLRegex = Pattern.compile("\"imdburl\":\"([^\"]+)\"", Pattern.CASE_INSENSITIVE).matcher(lines);
			if (linkURLRegex.find()) {
				linkURL = linkURLRegex.group(1).replace("\\", "");
			}
			// Parse title
			String title = "";
			Matcher titleRegex = Pattern.compile("\"title\":\"([^\"]+)\"", Pattern.CASE_INSENSITIVE).matcher(lines);
			if (titleRegex.find()) {
				title = titleRegex.group(1).replace("\\", "").replace("&#x27;", "'").replace("&amp;", "&");
			}
			title = botValue(title);
			// Parse runtime
			String runtime = "";
			Matcher runtimeRegex = Pattern.compile("\"runtime\":\"([^\"]+)\"", Pattern.CASE_INSENSITIVE).matcher(lines);
			if (runtimeRegex.find()) {
				runtime = runtimeRegex.group(1).replace("min", "m").replace(" ",  "");
				if (runtime.isEmpty() || runtime.equals("n\\/a")) {
					runtime = "N/A";
				}
			}
			runtime = botValue(runtime);
			// Parse year
			String year = "";
			Matcher yearRegex = Pattern.compile("\"year\":\"([^\"]+)\"").matcher(lines);
			if (yearRegex.find()) {
				year = yearRegex.group(1);
			}
			if (year.isEmpty() || year.equals("n\\/a")) {
				year = "N/A";
			}
			year = botValue(year);
			// Parse screening
			boolean screening = false;
			Matcher screensRegex = Pattern.compile("\"usascreens\":(\\d+)").matcher(lines);
			if (screensRegex.find()) {
				screening = Integer.parseInt(screensRegex.group(1)) > 0;
			}
			// Build output string from fields
			StringBuilder sb = new StringBuilder();
			sb.append("<a href=\"" + linkURL + "\">" + title + "</a>");
			sb.append(": Released in " + year);
			sb.append(" | Runtime: " + runtime);
			if (screening) {
				sb.append(" | Now in theaters");
			}
			return botMessage(sb.toString());
		
	}
	
	/*public static String animeBot(String anime) throws IOException {
		// Get anime data via MAL API
		URL animeURL = new URL("http://mal-api.com/anime/search?q=" + URLEncoder.encode(anime, "ISO-8859-1"));
		InputStream data = animeURL.openStream();
		// Initialize fields
		BufferedReader in = new BufferedReader(new InputStreamReader(data));
		return "";
	}*/
}
