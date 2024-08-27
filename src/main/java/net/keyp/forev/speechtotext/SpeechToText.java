package net.keyp.forev.speechtotext;

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
import com.google.cloud.texttospeech.v1.TextToSpeechSettings;
import com.google.protobuf.ByteString;

public class SpeechToText {

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger("speechtotext");
        // サービスアカウントキーのパスを設定
        //System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", "C:/Google/minecraft-405821-7d18a4974fb9.json");
        try {
            // サービスアカウントの JSON ファイルから認証情報を読み込む
            FileInputStream credentialsStream = new FileInputStream("path/to/your-service-account-file.json");
            GoogleCredentials credentials = ServiceAccountCredentials.fromStream(credentialsStream);

            // TextToSpeechSettings に認証情報を設定
            SpeechSettings settings = SpeechSettings.newBuilder().setCredentialsProvider(() -> credentials).build();

            // TextToSpeechClient を作成
            try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(settings)) {
                // ここで Text-to-Speech API の操作を行う
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String fileName = "C:\\Users\\maeka\\Downloads\\tadakiminihare(0_1M).mp3"; // 音声ファイルのパス
        try {
            convertSpeechToText(fileName);
        } catch (IOException e) {
            logger.error("An IOException error occurred: " + e.getMessage(), e);
        }
    }
    
    public static void convertSpeechToText(String fileName) throws IOException {
        try (SpeechClient speechClient = SpeechClient.create()) {
            ByteString audioBytes = ByteString.readFrom(new FileInputStream(fileName));

            // ファイルの拡張子を取得
            String extension = getFileExtension(fileName);

            RecognitionConfig.AudioEncoding encoding;

            // 拡張子に基づいてエンコーディングを設定
            switch (extension.toLowerCase()) {
                case "mp3"-> encoding = RecognitionConfig.AudioEncoding.MP3;
                case "wav"-> encoding = RecognitionConfig.AudioEncoding.LINEAR16;
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
    }

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
}

