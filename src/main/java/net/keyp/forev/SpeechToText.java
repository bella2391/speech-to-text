package net.keyp.forev;

import java.io.FileInputStream;
import java.io.IOException;

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

public class SpeechToText {

    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger("speechtotext");
        try {
            String jsonNamePath = "C:\\Google\\google-service-account-auth.json";
            String fileNamePath = "C:\\Test\\audio\\shining.mp3"; // 音声ファイルのパス

            // サービスアカウントの JSON ファイルから認証情報を読み込む
            FileInputStream credentialsStream = new FileInputStream(jsonNamePath);
            GoogleCredentials credentials = ServiceAccountCredentials.fromStream(credentialsStream);

            // SpeechSettings に認証情報を設定
            SpeechSettings settings = SpeechSettings.newBuilder().setCredentialsProvider(() -> credentials).build();

            // SpeechClient を作成
            try (SpeechClient speechClient = SpeechClient.create(settings)) {
                ByteString audioBytes = ByteString.readFrom(new FileInputStream(fileNamePath));

                // ファイルの拡張子を取得
                String extension = getFileExtension(fileNamePath);

                RecognitionConfig.AudioEncoding encoding;

                // 拡張子に基づいてエンコーディングを設定
                switch (extension.toLowerCase()) {
                    case "mp3" -> encoding = RecognitionConfig.AudioEncoding.MP3;
                    case "wav" -> encoding = RecognitionConfig.AudioEncoding.LINEAR16;
                    default -> throw new IllegalArgumentException("Unsupported audio format: " + extension);
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
