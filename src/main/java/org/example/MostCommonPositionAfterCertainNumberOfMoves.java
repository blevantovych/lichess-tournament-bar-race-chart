package org.example;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveConversionException;
import com.google.gson.Gson;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class MostCommonPositionAfterCertainNumberOfMoves {

	public static void main(String[] args) throws IOException, ParseException {

		StockfishIntegration stockfish = new StockfishIntegration();
		stockfish.startEngine("stockfish"); // Modify this path to your Stockfish path

		final int numberOfMovesToApply = 25;
		Map<String, Integer> positionFrequency = new HashMap<>();

		List<Game> games = getGames().stream()
				.filter(game -> game.moves.size() > numberOfMovesToApply)
				.filter(game -> !game.Variant.equals("From Position"))
				.collect(Collectors.toList());
		for (var game : games) {
			Board board = new Board();
//			if (numberOfMovesToApply > game.moves.size()) {
//				numberOfMovesToApply = game.moves.size();
//			}
			for (int i = 0; i < numberOfMovesToApply; i++) {
				var move = game.moves.get(i).m;
				try {
					board.doMove(move);
				} catch (MoveConversionException moveConversionException) {
					System.out.println(moveConversionException + String.format(" Move: %s, game: %s", move, game));
				}
			}
			var fen = board.getFen();
			positionFrequency.put(fen, positionFrequency.getOrDefault(fen, 0) + 1);
		}

		// Finding and printing the top 5 frequent positions
		List<Map.Entry<String, Integer>> list = new ArrayList<>(positionFrequency.entrySet());
		list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

		System.out.println("Top 20 most frequent positions:");
		int limit = Math.min(20, list.size());
		for (int i = 0; i < limit; i++) {
			Map.Entry<String, Integer> entry = list.get(i);
			System.out.println("FEN: " + entry.getKey() + ", Frequency: " + entry.getValue());
			var fen = entry.getKey();
			double evaluation = stockfish.getEvaluation(fen, 20);
			System.out.println("Evaluation: " + evaluation);
		}

		stockfish.stopEngine();

	}

	public static List<Game> getGames() throws IOException, ParseException {

		JSONParser parser = new JSONParser();
		Gson gson = new Gson();

		Object obj = parser.parse(new FileReader("/Users/blevantovych/Desktop/total-time-played-on-lichess/src/main/resources/games_06_29.json"));
		JSONArray gamesJson = (JSONArray) obj;

		List<Game> games = new ArrayList<>();
		for (Object gameObj : gamesJson) {
			games.add(gson.fromJson(gameObj.toString(), Game.class));
		}
		return games;
	}
}
