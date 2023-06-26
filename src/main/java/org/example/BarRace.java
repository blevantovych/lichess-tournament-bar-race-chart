package org.example;

import com.google.gson.Gson;
import lombok.ToString;
import org.javatuples.Triplet;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ToString
class Move {
	public String m; // actual move (i.e. e5, d5, Nf3 e.g)
	public String c; // clock info (0:02:25) - 2 minutes 25 seconds left

	public int clockTimeToSeconds() {
		String[] hoursMinutesSeconds = c.split(":");
		int minutes = Integer.valueOf(hoursMinutesSeconds[1]);
		int seconds = Integer.valueOf(hoursMinutesSeconds[2]);
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
	public int WhiteElo;
	public int BlackElo;
	public String WhiteRatingDiff;
	public String BlackRatingDiff;
	public String WhiteTeam;
	public String BlackTeam;
	public String Variant;
	public String TimeControl;
	public String ECO;
	public String Termination;
	public List<Move> moves;
}

@ToString
class LongestThoughtMove {
	public Game game;
	public int ply;
	public int thoughtTimeInSeconds;
}

public class BarRace {
	public static void main(String[] args) throws IOException, ParseException {
		JSONParser parser = new JSONParser();
		Gson gson = new Gson();

		Object obj = parser.parse(new FileReader("/Users/blevantovych/Desktop/total-time-played-on-lichess/src/main/resources/lichess_tournament.json"));
		JSONArray gamesJson = (JSONArray) obj;

		List<Game> games = new ArrayList<>();
		for (Object gameObj : gamesJson) {
			games.add(gson.fromJson(gameObj.toString(), Game.class));
		}
		var max= games.stream()
				.map(game ->
						IntStream.range(0, game.moves.size() - 2)
								.mapToObj(i -> {
									var moveTime = game.moves.get(i).clockTimeToSeconds() - game.moves.get(i + 2).clockTimeToSeconds();
									var ply = i + 2;
									return new Triplet(game, moveTime, ply);
								}).reduce((longestThoughtMove, current) ->
										((int) current.getValue1() > (int) longestThoughtMove.getValue1()) ? current : longestThoughtMove
								)).filter(t -> t != null && t.isPresent())
				.sorted(Comparator.comparingInt(a ->
					(int) a.get().getValue1()
				)).collect(Collectors.toList());
		System.out.println(max);

	}
}
