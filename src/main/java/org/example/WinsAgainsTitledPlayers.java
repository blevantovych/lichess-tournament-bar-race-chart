package org.example;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;

public class WinsAgainsTitledPlayers {

	public static void main(String[] args) throws IOException, ParseException {
		List<Game> games = getGames();

//		var gamesWithTitlePlayers = games.stream().filter(game -> game.WhiteTitle != null || game.BlackTitle != null).collect(Collectors.toList());
		var gamesWithTitlePlayers = games.stream().filter(game -> (game.WhiteTitle != null && game.WhiteTitle.equals("GM")) || (game.BlackTitle != null && game.BlackTitle.equals("GM"))).collect(Collectors.toList());
		int nGamesWithTitledPlayers = gamesWithTitlePlayers.stream().mapToInt(value -> 1).sum();
		System.out.println("Games with titled players: " + nGamesWithTitledPlayers + " " + (double) nGamesWithTitledPlayers / games.size());
		var wins = gamesWithTitlePlayers.stream()
				.filter(game -> (game.White.equals("bodya17") && game.Result.equals("1-0")) || (game.Black.equals("bodya17") && game.Result.equals("0-1")))
				.collect(Collectors.toList());
		var loses = gamesWithTitlePlayers.stream()
				.filter(game -> (game.White.equals("bodya17") && game.Result.equals("0-1")) || (game.Black.equals("bodya17") && game.Result.equals("1-0")))
				.collect(Collectors.toList());
		System.out.println("Wins: " + wins.size());
		wins.stream().map(game -> game.Site).forEach(System.out::println);
		System.out.println("Losses: " + loses.size());
		System.out.println("Draws: " + (gamesWithTitlePlayers.size() - wins.size() - loses.size()));

	}

	public static List<Game> getGames() throws IOException, ParseException {

		JSONParser parser = new JSONParser();
		Gson gson = new Gson();

		Object obj = parser.parse(new FileReader("/Users/blevantovych/Desktop/total-time-played-on-lichess/src/main/resources/games.json"));
		JSONArray gamesJson = (JSONArray) obj;

		List<Game> games = new ArrayList<>();
		for (Object gameObj : gamesJson) {
			games.add(gson.fromJson(gameObj.toString(), Game.class));
		}
		return games;
	}

	public WinsAgainsTitledPlayers() {

	}

	public int distributeCandies(int[] candyType) {
		return Collections.max(List.of(Arrays.asList(candyType).stream().distinct().collect(Collectors.toList()).size(), candyType.length / 2));
	}	
}
