package com.github.verazza.stt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognizeRequest;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.cloud.speech.v1.SpeechSettings;
import com.google.protobuf.ByteString;

public class App {
  private static final String AUTH_JSON_FILENAME = "auth.json";
  private static final String DEFAULT_MP3_FILENAME = "audio.mp3"; // デフォルトのMP3ファイル名

  public static void main(String[] args) {
    Logger logger = LoggerFactory.getLogger("speech-to-text");

    File authFile = new File(AUTH_JSON_FILENAME);
    File mp3File = new File(DEFAULT_MP3_FILENAME);

    try {
      InputStream credentialsStream;
      if (authFile.exists()) {
        logger.info("auth.json found in the same directory. Loading...");
        credentialsStream = new FileInputStream(authFile);
      } else {
        System.out.println("auth.json をjarファイルと同じ階層に配置してください。");
        return;
      }

      GoogleCredentials credentials = ServiceAccountCredentials.fromStream(credentialsStream);

      SpeechSettings settings = SpeechSettings.newBuilder().setCredentialsProvider(() -> credentials).build();

      try (SpeechClient speechClient = SpeechClient.create(settings)) {
        ByteString audioBytes;
        String fileNamePath;

        if (mp3File.exists()) {
          logger.info(DEFAULT_MP3_FILENAME + " found in the same directory. Loading...");
          fileNamePath = DEFAULT_MP3_FILENAME;
          audioBytes = ByteString.readFrom(new FileInputStream(fileNamePath));
        } else {
          System.out.println(DEFAULT_MP3_FILENAME + " をjarファイルと同じ階層に配置してください。");
          return;
        }

        String extension = getFileExtension(fileNamePath);
        RecognitionConfig.AudioEncoding encoding;

        switch (extension.toLowerCase()) {
          case "mp3":
            encoding = RecognitionConfig.AudioEncoding.MP3;
            break;
          case "wav":
            encoding = RecognitionConfig.AudioEncoding.LINEAR16;
            break;
          default:
            throw new IllegalArgumentException("Unsupported audio format: " + extension);
        }

        RecognitionConfig config = RecognitionConfig.newBuilder()
            .setEncoding(encoding)
            .setSampleRateHertz(16000)
            .setLanguageCode("ja-JP")
            .build();

        RecognitionAudio audio = RecognitionAudio.newBuilder()
            .setContent(audioBytes)
            .build();

        RecognizeRequest request = RecognizeRequest.newBuilder()
            .setConfig(config)
            .setAudio(audio)
            .build();

        RecognizeResponse response = speechClient.recognize(request);

        for (SpeechRecognitionResult result : response.getResultsList()) {
          System.out.printf("Transcription: %s%n", result.getAlternativesList().get(0).getTranscript());
        }
      }
    } catch (IOException e) {
      logger.error("An IOException error occurred: " + e.getMessage(), e);
    }
  }

  private static String getFileExtension(String fileNamePath) {
    int dotIndex = fileNamePath.lastIndexOf('.');
    return (dotIndex == -1) ? "" : fileNamePath.substring(dotIndex + 1);
  }
}
