package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.net.*;
import java.io.*;

public class Main {
	public static final int AVERAGE_GAME_LENGTH = 40;

	public static void analyzeGame(String gameId) throws Exception {
		URL lichessAnalysis = new URL(String.format("https://lichess.org/%s/request-analysis", gameId));
		HttpURLConnection lichessAnalysisConnection = (HttpURLConnection) lichessAnalysis.openConnection();
		lichessAnalysisConnection.setRequestMethod("POST");
		lichessAnalysisConnection.setRequestProperty("Cookie", "lila2=4a391d669feb555ebdf0c6ffa5c83c1f86f650eb-sid=JCHm8XZqbX2j1uCzEBYdbm&sessionId=QYrneIv3FwndT8OYckuxAM");
		System.out.println("lila2=4a391d669feb555ebdf0c6ffa5c83c1f86f650eb-sid=JCHm8XZqbX2j1uCzEBYdbm&sessionId=QYrneIv3FwndT8OYckuxAM");
		lichessAnalysisConnection.setRequestProperty("Origin", "https://lichess.org");
		lichessAnalysisConnection.setRequestProperty("Host", "localhost");
		lichessAnalysisConnection.setRequestProperty("Content-Length", "0");
		lichessAnalysisConnection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
		lichessAnalysisConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:221.0) Gecko/20100101 Firefox/31.0"); // add this line to your code
		lichessAnalysisConnection.setDoInput(true);
		lichessAnalysisConnection.setDoOutput(true);
		Map<String, List<String>> responseHeaders = lichessAnalysisConnection.getHeaderFields();
		for (Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
		BufferedReader in = new BufferedReader(
				new InputStreamReader(
						lichessAnalysisConnection.getInputStream()));
		String inputLine;

		while ((inputLine = in.readLine()) != null)
			System.out.println(inputLine);
		in.close();
	}

	public static void main(String[] args) throws Exception, IOException, ParseException {
//		analyzeGame("mAAfePPD");
		double totalMinutes = 0;
		double totalMoves = 0;
		int analyzedGames = 0;
		int blitzGames = 0;
		Set<String> dates = new HashSet<>();
		List<String> gamesWithCheckmate = new ArrayList<>();
		List<String> notAnalyzedBlitzGames = new ArrayList<>();
		JSONParser parser = new JSONParser();
//		Object obj = parser.parse(new FileReader("/Users/blevantovych/Desktop/total-time-played-on-lichess/src/main/resources/lichess_bodya17_2024-02-18.json"));
		Object obj = parser.parse(new FileReader("/Users/blevantovych/Desktop/total-time-played-on-lichess/src/main/resources/games_06_29.json"));
		JSONArray games = (JSONArray) obj;


		String[] firstGameDateString = ((String)((JSONObject) games.get(games.size() - 1)).get("UTCDate")).split("\\.");
		int firstGameYear = Integer.parseInt(firstGameDateString[0]);
		int firstGameMonth = Integer.parseInt(firstGameDateString[1]);
		int firstGameDay = Integer.parseInt(firstGameDateString[2]);
		LocalDate firstGameDate = LocalDate.of(firstGameYear , firstGameMonth, firstGameDay);

		String[] lastGameDateString = ((String)((JSONObject) games.get(0)).get("UTCDate")).split("\\.");
		int lastGameYear = Integer.parseInt(lastGameDateString[0]);
		int lastGameMonth = Integer.parseInt(lastGameDateString[1]);
		int lastGameDay = Integer.parseInt(lastGameDateString[2]);
		LocalDate lastGameDate = LocalDate.of( lastGameYear, lastGameMonth, lastGameDay);

		long daysBetween = ChronoUnit.DAYS.between(firstGameDate, lastGameDate);

		Pattern pattern = Pattern.compile("(.*)\\+(.*)");

		for (Object game : games) {
			dates.add((String) ((JSONObject) game).get("Date"));
			String timeControl = (String) ((JSONObject) game).get("TimeControl");
			String opening = (String) ((JSONObject) game).get("Opening");
			JSONArray moves = (JSONArray) ((JSONObject) game).get("moves");
			totalMoves += moves.size() / 2;
			Matcher matcher = pattern.matcher(timeControl);
			if (matcher.matches()) {
				double gameLengthInMinutes = Integer.parseInt(matcher.group(1)) / 60 * 2; // matcher.group(1) will be 180 for a 3-minute game
				double incrementInMinutes = Integer.parseInt(matcher.group(2)) / 60 * AVERAGE_GAME_LENGTH * 2; // 2 because clock is incremented for each player
				totalMinutes += gameLengthInMinutes + incrementInMinutes;
			}
			if (!moves.isEmpty() && ((JSONObject)(moves.get(0))).get("e") != null && ((String)(((JSONObject) game).get("Event"))).contains("Blitz")) {
				analyzedGames++;
			}
			if (!moves.isEmpty() && ((JSONObject)(moves.get(0))).get("e") == null && ((String)(((JSONObject) game).get("Event"))).contains("Blitz")) {
				notAnalyzedBlitzGames.add((String)((JSONObject) game).get("Site"));
			}
			if (((String)(((JSONObject) game).get("Event"))).contains("Blitz")) {
				blitzGames++;
			}
			if (moves.size() < 40 && opening.contains("Sicilian")) {
				for (Object move : moves) {
					String m = (String) ((JSONObject) move).get("m");
					if (m.contains("#")) {
						gamesWithCheckmate.add((String)((JSONObject) game).get("Site"));
					}
				}
			}
		}
		System.out.println("Total minutes played: " + totalMinutes);
		System.out.println("Average game length: " + totalMoves / games.size());
		System.out.println("Days played: " + dates.size());
		System.out.println("Average number of minutes played each day: " + totalMinutes / dates.size());
		System.out.println("Average number of minutes played each day (counting days when not played): " + totalMinutes / daysBetween);
		System.out.println("Average number of minutes played each day: " + totalMinutes / daysBetween);
		System.out.println("Average number of games played each day: " + games.size() / dates.size());
		System.out.println(gamesWithCheckmate.size() +" games with mate: " + gamesWithCheckmate);
//		System.out.println("Analyzed games: " + analyzedGames + ", All games: " + games.size());
//		System.out.println("Percentage of analyzed games: " + (double)analyzedGames / games.size());
		System.out.println("Percentage of analyzed blitz games: " + (double)analyzedGames / blitzGames);
		System.out.println("Not analyzed blitz games: " + notAnalyzedBlitzGames);
		System.out.println("Not analyzed blitz games: " + (blitzGames - analyzedGames));
	}

}
