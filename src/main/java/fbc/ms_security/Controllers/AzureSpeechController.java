package fbc.ms_security.Controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@RestController
@RequestMapping("/api/public/azure")
public class AzureSpeechController {

    @Value("${azure.speech.key}")
    private String speechKey;

    @Value("${azure.speech.region}")
    private String region;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/token")
    public ResponseEntity<String> getToken() {

        String url = "https://" + region + ".api.cognitive.microsoft.com/sts/v1.0/issueToken";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Ocp-Apim-Subscription-Key", speechKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return ResponseEntity.ok(response.getBody());
    }

    @PostMapping("/tts-audio")
    public ResponseEntity<byte[]> getAudio(@RequestBody Map<String, String> body) {
        try {
            String text = body.get("text");
            String voice = body.get("voice");

            // 1. token
            String token = getToken().getBody();

            // 2. SSML
            String ssml = """
        <speak version='1.0' xml:lang='es-CO'>
            <voice name='%s'>%s</voice>
        </speak>
        """.formatted(voice, text);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(new URI("https://" + region + ".tts.speech.microsoft.com/cognitiveservices/v1"))
                    .header("Content-Type", "application/ssml+xml")
                    .header("X-Microsoft-OutputFormat", "audio-24khz-48kbitrate-mono-mp3")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(ssml))
                    .build();

            HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());

            return ResponseEntity.ok()
                    .header("Content-Type", "audio/mpeg")
                    .body(resp.body());

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

}