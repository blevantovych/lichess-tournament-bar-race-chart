package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class StockfishIntegration {

	private Process engineProcess;
	private BufferedReader reader;
	private PrintWriter writer;

	public void startEngine(String pathToEngine) throws IOException {
		ProcessBuilder builder = new ProcessBuilder(pathToEngine);
		engineProcess = builder.start();
		reader = new BufferedReader(new InputStreamReader(engineProcess.getInputStream()));
		writer = new PrintWriter(new OutputStreamWriter(engineProcess.getOutputStream()), true);

		if (!isReady()) {
			throw new IllegalStateException("Stockfish engine initialization failed.");
		}
		setOptions();
	}

	private boolean isReady() throws IOException {
		writer.println("uci");
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.equals("uciok")) return true;
			if (line.contains("readyok")) return true;
		}
		return false;
	}

	private void setOptions() {
		// Set any options for Stockfish, for example:
		writer.println("setoption name Skill Level value 20");
	}

	public double getEvaluation(String fen, int depth) throws IOException {
		writer.println("position fen " + fen);
		writer.println("go depth " + depth);

		String line;
		double score = 0.0; // Default score
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("info depth " + depth) && line.contains("score cp")) {
				String[] parts = line.split(" ");
				for (int i = 0; i < parts.length; i++) {
					if ("score".equals(parts[i]) && i + 2 < parts.length) {
						// Expected format: score cp 97 or score mate 3
						if ("cp".equals(parts[i + 1])) {
							score = Double.parseDouble(parts[i + 2]) / 100.0; // Convert centipawn score to decimal
						} else if ("mate".equals(parts[i + 1])) {
							// This is a checkmate score, handle accordingly
							int mateIn = Integer.parseInt(parts[i + 2]);
							score = (mateIn > 0) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
						}
						break;
					}
				}
			}
			if (line.startsWith("bestmove")) {
				break;
			}
		}
		return score;
	}


	public void stopEngine() {
		writer.println("quit");
		try {
			engineProcess.waitFor();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt(); // handle interrupted status
		}
	}

	public static void main(String[] args) {
		StockfishIntegration stockfish = new StockfishIntegration();
		try {
			stockfish.startEngine("stockfish"); // Modify this path to your Stockfish path
			String fen = "r1bqkbnr/pppp1ppp/2n5/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 2 4";
			double evaluation = stockfish.getEvaluation(fen, 20);
			System.out.println("Evaluation: " + evaluation);
			stockfish.stopEngine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
