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

public class Main {
	public static final int AVERAGE_GAME_LENGTH = 40;

	public static void main(String[] args) throws IOException, ParseException {
		double totalMinutes = 0;
		double totalMoves = 0;
		int analyzedGames = 0;
		int blitzGames = 0;
		Set<String> dates = new HashSet<>();
		List<String> gamesWithCheckmate = new ArrayList<>();
		List<String> notAnalyzedBlitzGames = new ArrayList<>();
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(new FileReader("/Users/blevantovych/Desktop/total-time-played-on-lichess/src/main/resources/games.json"));
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
			JSONArray moves = (JSONArray) ((JSONObject) game).get("moves");
			totalMoves += moves.size() / 2;
			Matcher matcher = pattern.matcher(timeControl);
			if (matcher.matches()) {
				double gameLengthInMinutes = Integer.parseInt(matcher.group(1)) / 60 * 2; // matcher.group(1) will be 180 for a 3-minute game
				double incrementInMinutes = Integer.parseInt(matcher.group(2)) / 60 * AVERAGE_GAME_LENGTH * 2; // 2 because clock is incremented for each player
				totalMinutes += gameLengthInMinutes + incrementInMinutes;
			}
			if (((JSONObject)(moves.get(0))).get("e") != null && ((String)(((JSONObject) game).get("Event"))).contains("Blitz")) {
				analyzedGames++;
			}
			if (((JSONObject)(moves.get(0))).get("e") == null && ((String)(((JSONObject) game).get("Event"))).contains("Blitz")) {
				notAnalyzedBlitzGames.add((String)((JSONObject) game).get("Site"));
			}
			if (((String)(((JSONObject) game).get("Event"))).contains("Blitz")) {
				blitzGames++;
			}
			if (moves.size() < 40) {
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
