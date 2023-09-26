package org.example;

import com.google.gson.Gson;
import lombok.SneakyThrows;
import lombok.ToString;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@ToString
class Move {
	public String m; // actual move (i.e. e5, d5, Nf3 e.g)
	public String c; // clock info (0:02:25) - 2 minutes 25 seconds left

	public int clockTimeToSeconds() {
		String[] hoursMinutesSeconds = c.split(":");
		int minutes = Integer.parseInt(hoursMinutesSeconds[1]);
		int seconds = Integer.parseInt(hoursMinutesSeconds[2]);
		return minutes * 60 + seconds;
	}
}

@ToString
class Game {
	public String Event;
	public String Site;
	public String Date;
	public String White;
	public String Black;
	public String Result;
	public String UTCDate;
	public String UTCTime;
	public String WhiteElo;
	public String BlackElo;
	public String WhiteRatingDiff;
	public String BlackRatingDiff;
	public String WhiteTitle;
	public String BlackTitle;
	public String WhiteTeam;
	public String BlackTeam;
	public String Variant;
	public String TimeControl;
	public String ECO;
	public String Termination;
	public List<Move> moves;
	// custom properties
	public LocalTime UTCEndTime;
	public int whiteScore;
	public int blackScore;
}

@ToString
class Sheet {
	private String scores;
	private List<Integer> scoreList;

	public List<Integer> getScores() {

		if (scoreList == null) {
			scoreList = Arrays.stream(scores.split("")).map(Integer::valueOf).collect(Collectors.toList());
			Collections.reverse(scoreList);
		}
		return scoreList;
	}
}

@ToString
class Standing {
	public int rank;
	public int score;
	public int rating;
	public String username;
	public int performance;
	public String team;
	public Sheet sheet;
}

@ToString
class LongestThoughtMove {
	public Game game;
	public int ply;
	public int thoughtTimeInSeconds;
}

public class BarRace {
	static List<Standing> standings = new ArrayList<>();
	static List<Game> games = new ArrayList<>();
	static Map<String, String> teamNames;
	static {
		Gson gson = new Gson();
		JSONParser parser = new JSONParser();
		try {
			// https://lichess.org/api/tournament/{id}/results?sheet=true
			JSONArray standingsJson = (JSONArray) parser.parse(new FileReader("/Users/blevantovych/Desktop/total-time-played-on-lichess/src/main/resources/standings.json"));
			// https://lichess.org/api/tournament/{id}/games?clocks=true
			JSONArray gamesJson = (JSONArray) parser.parse(new FileReader("/Users/blevantovych/Desktop/total-time-played-on-lichess/src/main/resources/lichess_tournament.json"));
			// https://lichess.org/api/tournament/{id}
			JSONObject teamNamesJson = (JSONObject) parser.parse(new FileReader("/Users/blevantovych/Desktop/total-time-played-on-lichess/src/main/resources/teamNames.json"));
			for (Object gameObj : gamesJson) {
				games.add(gson.fromJson(gameObj.toString(), Game.class));
			}
			for (Object standingJson : standingsJson) {
				Standing standing = gson.fromJson(standingJson.toString(), Standing.class);
				standings.add(standing);
			}
			teamNames = JSONToMapConverter.convertJSONToMap(teamNamesJson.toJSONString());
		} catch (IOException | ParseException e) {
			throw new RuntimeException(e);
		}
	}

	@SneakyThrows
	public static Map<String, Integer> getTeamScores(int secondsSinceStart) {
		Map<String, Integer> playerCurrentGame = new HashMap<>();

		for (Standing standing : standings) {
			playerCurrentGame.put(standing.username, 0);
		}
		var gamesWithEndTime = games.stream()
				.peek(game -> {
					int gameDurationInSeconds = getGameDuration(game);
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
					LocalTime localTime = LocalTime.parse(game.UTCTime, formatter);
					game.UTCEndTime = localTime
							.plusMinutes(gameDurationInSeconds / 60)
							.plusSeconds(gameDurationInSeconds % 60);
					var whitePlayerScoreSheet = standings.stream().filter(s -> s.username.equals(game.White)).findFirst();
					var blackPlayerScoreSheet = standings.stream().filter(s -> s.username.equals(game.Black)).findFirst();
					game.whiteScore = whitePlayerScoreSheet.isPresent() ? whitePlayerScoreSheet.get().sheet.getScores().get(playerCurrentGame.get(game.White)) : 0;
					game.blackScore = blackPlayerScoreSheet.isPresent() ? blackPlayerScoreSheet.get().sheet.getScores().get(playerCurrentGame.get(game.Black)) : 0;
					if (whitePlayerScoreSheet.isPresent()) {
						playerCurrentGame.put(game.White, playerCurrentGame.get(game.White) + 1);
					}

					if (blackPlayerScoreSheet.isPresent()) {
						playerCurrentGame.put(game.Black, playerCurrentGame.get(game.Black) + 1);
					}
				}).filter(game -> {
					var tournamentStart = LocalTime.parse("18:00:00");
					var newTime = tournamentStart.plusSeconds(secondsSinceStart);
					return game.UTCEndTime.compareTo(newTime) < 0;
				}).collect(Collectors.toList());

		Map<String, List<Game>> teamToGames = new HashMap<>();
		for (Game game : gamesWithEndTime) {
			addGameToTeam(game, game.WhiteTeam, teamToGames);
			addGameToTeam(game, game.BlackTeam, teamToGames);
		}

//		List<Pair<String, Integer>> teamScores = new ArrayList<>();
		Map<String, Integer> teamScores = new HashMap<>();

		for (Map.Entry<String, List<Game>> teamGames : teamToGames.entrySet()) {
			String team = teamNames.get(teamGames.getKey());
			Map<String, Integer> contibutionsByPlayer = new HashMap<>();

			teamGames.getValue().forEach(game -> {
				String teamPlayer = teamGames.getKey().equals(game.WhiteTeam) ? game.White : game.Black;
				int playerContribution = teamGames.getKey().equals(game.WhiteTeam) ? game.whiteScore : game.blackScore;
				if (contibutionsByPlayer.containsKey(teamPlayer)) {
					contibutionsByPlayer.put(teamPlayer, contibutionsByPlayer.get(teamPlayer) + playerContribution);
				} else {
					contibutionsByPlayer.put(teamPlayer, playerContribution);
				}
			});

			int teamScore = contibutionsByPlayer.values().stream().sorted(Comparator.reverseOrder()).limit(5).reduce(0, Integer::sum);
			teamScores.put(team, teamScore);
		}

//		teamScores.stream().sorted(Comparator.comparing(Pair::getValue1)).forEach(teamScore -> {
//			System.out.println(teamScore.getValue0() + ": " + teamScore.getValue1());
//		});
		return teamScores;
	}
	
	public static int getGameDuration(Game game) {
		if (game.moves.size() < 2) return 0;
		int gameDurationInSeconds = 0;
		int initialTimeInSeconds = Integer.parseInt(game.TimeControl.split("\\+")[0]);
		// <= probably could have been ==
		boolean hasWhiteBerserked = game.moves.get(0).clockTimeToSeconds() <= initialTimeInSeconds / 2;
		// we can't know for sure that black has berseked as they could have spent more than half of the time on the first move
		// but that should be fairly rare
		boolean hasBlackBerserked = game.moves.get(1).clockTimeToSeconds() <= initialTimeInSeconds / 2;

		int maximumGameDuration = Integer.parseInt(game.TimeControl.split("\\+")[0]) * 2;
		Move lastMove = game.moves.get(game.moves.size() - 1);

		if (hasWhiteBerserked) {
			maximumGameDuration /= 2;
		}

		if (hasBlackBerserked) {
			maximumGameDuration /= 2;
		}

		if (game.Termination.equals("Time forfeit")) {
			gameDurationInSeconds = maximumGameDuration - lastMove.clockTimeToSeconds();
		} else {
			Move secondToLastMove = game.moves.get(game.moves.size() - 2);
			gameDurationInSeconds = maximumGameDuration - secondToLastMove.clockTimeToSeconds() - lastMove.clockTimeToSeconds();
		}

		return gameDurationInSeconds;
	}

	public static void writeToFile(String data, FileWriter writer) {
		try {
			writer.write(data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) throws IOException {
		// System.out.println(games.stream().map(game -> game.moves.size()).reduce(Integer::sum));
		// System.out.println(games.stream().filter(game -> game.moves.size() < 2).collect(Collectors.toList()).size());
		// System.out.println(games.stream().filter(game -> game.moves.size() < 2).collect(Collectors.toList()));
		int tournamentDurationInSeconds = (int) (1.5 /* hours */ * 60 * 60 /* to make sure that all games are counted */);

		var teamScoresInEachSecond = IntStream.rangeClosed(0, tournamentDurationInSeconds);
		FileWriter f = new FileWriter("output.csv");
		var teamNamesList = teamNames.values();
		writeToFile(",", f);
		teamNamesList.forEach(teamName -> writeToFile(teamName.replace("\"", "").replace(",", "") + ",", f));
		writeToFile(System.getProperty("line.separator"), f);
		teamScoresInEachSecond.forEach(second -> {
			var teamScoresAtSpecificInstant  = getTeamScores(second);
			var tournamentStart = LocalTime.parse("18:00:00");
			var newTime = tournamentStart.plusSeconds(second);
			writeToFile(newTime + ",", f);
			teamNamesList.forEach(teamName -> writeToFile(Optional.ofNullable(teamScoresAtSpecificInstant.get(teamName)).orElse(0) + ",", f));
			writeToFile(System.getProperty("line.separator"), f);
		});
		f.close();
	}

	private static void addGameToTeam(Game game, String teamId, Map<String, List<Game>> teamToGames) {
		if (teamToGames.containsKey(teamId)) {
			teamToGames.get(teamId).add(game);
		} else {
			List<Game> teamGames = new ArrayList<>();
			teamGames.add(game);
			teamToGames.put(teamId, teamGames);
		}
	}

	private static int getBerserkedForfeitedGameDuration(Game game, int initialTimeInSeconds, boolean hasBerserked) {
		final int berserkedGameDurationInSeconds = initialTimeInSeconds / 2;
		int gameDurationInSeconds = berserkedGameDurationInSeconds;
		final Move lastMove = game.moves.get(game.moves.size() - 1);
		int timeWinnerUsed;
		if (hasBerserked) {
			timeWinnerUsed = berserkedGameDurationInSeconds - lastMove.clockTimeToSeconds();
		} else {
			timeWinnerUsed = initialTimeInSeconds - lastMove.clockTimeToSeconds();
		}
		gameDurationInSeconds += timeWinnerUsed;
		return gameDurationInSeconds;
	}
}
