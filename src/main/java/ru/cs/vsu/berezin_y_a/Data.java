package ru.cs.vsu.berezin_y_a;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Data {

    String api = Files.readString(Path.of("src/main/java/ru/cs/vsu/berezin_y_a/Api.txt"));
    String botToken = Files.readString(Path.of("src/main/java/ru/cs/vsu/berezin_y_a/BotToken.txt"));

    public Data() throws IOException {
    }
}
