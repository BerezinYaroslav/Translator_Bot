package ru.cs.vsu.berezin_y_a;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

public class Bot extends TelegramLongPollingBot {

    Data data = new Data();

    @Override
    public void onUpdateReceived(Update update) {
        String chatId = update.getMessage().getChatId().toString();
        String messageText = update.getMessage().getText();

        if (update.hasMessage() && update.getMessage().hasText()) {
            if (messageText.contains("/start")) {
                String stickerId = "CAACAgIAAxkBAAEEO2xiOWxKyxk2V8O5bPWqOf_n-C_hgQACNhYAAlxA2EvbRm7S3ZV6DSME";
                String text = "Привет! Я не просто бот, а испанский словарь, готовый помочь тебе в трудную минуту!\n" +
                        "\nПросто отправь мне любое слово и я переведу его!";
                sendSticker(update, chatId, stickerId);
                sendMessage(update, chatId, text);
            } else {
                String text = "Вот, что мне удалось найти: ";
                sendMessage(update, chatId, text);
                try {
                    sendTranslation(update, chatId, messageText);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendTranslation(Update update, String chatId, String text) throws IOException {
        String api = data.api;
        String dict = "join_es_ru";

        String request = "http://api.diccionario.ru/?key=[" + api + "]&q=" + text; //  + "&dict=" + dict

        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) new URL(request).openConnection();

            connection.setRequestMethod("GET");
            connection.setUseCaches(false);

            connection.connect();

            if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                JSONParser jsonParser = new JSONParser();

                JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
                JSONArray hits = (JSONArray) jsonObject.get("hits");

                Iterator i = hits.iterator();
                StringBuilder answer = new StringBuilder();

                while (i.hasNext()) {
                    JSONObject innerObject = (JSONObject) i.next();
                    String l = (String) innerObject.get("l");
                    String r = (String) innerObject.get("r");

                    if ((!l.contains("<")) && (!r.contains("<"))) {
                        answer.append(innerObject.get("l"));
                        answer.append(" - ");
                        answer.append(innerObject.get("r"));
                        answer.append("\n");
                        answer.append("\n");
                    } else if (!l.contains("<")) {
                        answer.append(innerObject.get("l"));
                        answer.append("\n");
                        answer.append("\n");
                    } else if (!r.contains("<")) {
                        answer.append(innerObject.get("r"));
                        answer.append("\n");
                        answer.append("\n");
                    }
                }

                sendMessage(update, chatId, answer.toString());
            } else {
                System.out.println("fail:" + connection.getResponseCode() + ", " + connection.getResponseMessage());
            }
        } catch (Throwable cause) {
            cause.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

    }

    private void sendMessage(Update update, String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        System.out.println("Message sent to " + chatId);
    }

    private void sendImageFromFileId(String fileId, String chatId) {
        SendPhoto sendPhotoRequest = new SendPhoto();
        sendPhotoRequest.setChatId(chatId);
        sendPhotoRequest.setPhoto(new InputFile(fileId));

        try {
            execute(sendPhotoRequest);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendSticker(Update update, String chatId, String StickerId) {
        String ChatId = update.getMessage().getChatId().toString();
        InputFile StickerFile = new InputFile(StickerId);
        SendSticker TheSticker = new SendSticker(ChatId, StickerFile);

        try {
            execute(TheSticker);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        System.out.println("Sticker sent to " + chatId);
    }

    @Override
    public String getBotUsername() {
        return "Spanish Dictionary RGF Bot";
    }

    @Override
    public String getBotToken() {
        return data.botToken;
    }

}
