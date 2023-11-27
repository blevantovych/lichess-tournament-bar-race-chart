package org.example;

import org.apache.commons.io.FileUtils;
import java.io.*;

import java.net.URL;

public class FileDownload {

    public static void main(String[] args) {
        downloadWithApacheCommons("https://lichess.org/api/tournament/sH24g2zH/games", "games.pgn");
    }

    public static void downloadWithApacheCommons(String url, String localFilename) {

        int CONNECT_TIMEOUT = 10000;
        int READ_TIMEOUT = 10000;
        try {
            FileUtils.copyURLToFile(new URL(url), new File(localFilename), CONNECT_TIMEOUT, READ_TIMEOUT);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
