# Labs

## Generate Audio from Text

- At the [OpenAI developer site](https://platform.openai.com/docs/overview), sign up for an account and create an API key. Currently the link to your API keys is at https://platform.openai.com/api-keys . OpenAI now recommends project API keys. See [this page](https://help.openai.com/en/articles/9186755-managing-your-work-in-the-api-platform-with-projects) for more information.
- Add the API key to an environment variable called `OPENAI_API_KEY`.
- Create a new Java project in your favorite IDE with either Gradle or Maven as your build tool.
- Review the documentation for the Open AI [Text-to-speech](https://platform.openai.com/docs/guides/text-to-speech) API.
- Create a new class called `TextToSpeechService` in a package of your choosing.
- Add a method called `generateMp3` that takes a text string and returns a `byte[]`. The method should take three arguments:
  - `model` of type `String`
  - `input` of type `String`
  - `voice` of type `String`

- Add a local variable called `payload`, which is a JSON string that contains the text you want to convert to speech:

```java
String payload = """
    {
        "model": "%s",
        "input": "%s",
        "voice": "%s"
    }
    """.formatted(model, input.replaceAll("\\s+", " ").trim(), voice);
```

- For those parameters:
  - The value of the `model` variable must be either `tts-1` or `tts-1-hd`. The non-hd version is almost certainly good enough, but use whichever one you prefer.
  - The value of `voice` must be one of: `alloy`, `echo`, `fable`, `onyx`, `nova`, or `shimmer`. Pick any one you like.
  - The `input` parameter is the text you wish to convert to an mp3.

- Next, create an `HttpRequest` object that will include the `payload` in a POST request:

```java
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://api.openai.com/v1/audio/speech"))
    .header("Authorization", "Bearer %s".formatted(OPENAI_API_KEY))
    .header("Content-Type", "application/json")
    .header("Accept", "audio/mpeg")
    .POST(HttpRequest.BodyPublishers.ofString(payload))
    .build();
```

- You'll want to save the returned byte array to a file. Here are static methods you can add to a `FileUtils` class for that purpose. First add an attribute to the `FileUtils` class for the destination directory:

```java
public static final String AUDIO_RESOURCES_PATH = "src/main/resources/audio";
```

- Then add the following methods to the class:

```java
public static String writeSoundBytesToFile(byte[] bytes) {
    String timestamp = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    String fileName = String.format("audio_%s.mp3", timestamp);
    return writeSoundBytesToGivenFile(bytes, fileName);
}

public static String writeSoundBytesToGivenFile(byte[] bytes, String fileName) {
    Path directory = Paths.get(AUDIO_RESOURCES_PATH);
    Path filePath = directory.resolve(fileName);
    try {
        Files.write(filePath, bytes, StandardOpenOption.CREATE_NEW);
        System.out.printf("Saved %s to %s%n", fileName, AUDIO_RESOURCES_PATH);
        return fileName;
    } catch (IOException e) {
        throw new UncheckedIOException("Error writing audio to file", e);
    }
}
```

- Now you can create an HTTP request to the OpenAI API, send it, and save the returned byte array to a file. Here is the complete `generateMp3` method:

```java
try (HttpClient client = HttpClient.newHttpClient()) {
    HttpResponse<byte[]> response =
        client.send(request, HttpResponse.BodyHandlers.ofByteArray());
    byte[] body = response.body();
    String fileName = FileUtils.writeSoundBytesToFile(body);
    return body;
} catch (IOException | InterruptedException e) {
        throw new RuntimeException(e);
}
```

- Add a test to drive the system:

```java
    
@Test
void test_generateMp3() {
    var service = new TextToSpeechService();
    byte[] result = service.generateMp3(
            "tts-1",
            """
            Now that I understand how to generate audio from text,
            I can use this feature in all my applications.
            """,
            "fable"
    );
    assertNotNull(result);
    assertTrue(result.length > 0);
}
```

- Run the test. The console should show the name of the generated file, which will be inside the `src/main/resources/audio` folder. You can play the file to hear the text you provided.  

## Install Ollama

- Download the latest version of Ollama from the [Ollama website](https://ollama.com). 
- Run the installer and follow the instructions. 
- From a command prompt, enter `ollama run llama3` to download and install llama3 locally. 
- Feel free to enter some prompts when the download completes. When you're finished, type `/bye` to exit.

## Lab: Access Ollama programmatically

- Verify Ollama is running by entering [http://localhost:11434](http://localhost:11434) in a browser. You should get back the string, `Ollama is running`.
- Create a Java project in your favorite IDE with either Gradle or Maven as your build tool.
- Create a class called `OllamaService` in a package of your choosing.
- Add a method called `generate` that takes a `String` and returns a `String`.
- Inside the method, use the `HttpClient` class to send a POST request to `http://localhost:11434` with the string you passed in.
- Here is the code you need to send the request:

```java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class OllamaService {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final String URL = "http://localhost:11434";

    public String generate(String input) {
        String request = """
            {
                "model": "llama3",
                "input": "%s",
                "stream": false
            }
            """.formatted(input);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(URL + "/api/generate"))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(input))
            .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

NOTE: If you are using Java 21+, then `HttpClient` implements the `AutoCloseable` interface. That means you can call the `newHttpClient` method inside a try-with-resources block. That way the client will be closed automatically when the try block exits.

* Create a test class to verify the `OllamaService` class works as expected. Here is a sample:

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OllamaServiceTest {
    @Test
    public void testGenerate() {
        OllamaService service = new OllamaService();
        String result = service.generate("Why is the sky blue?");
        assertTrue(result.contains("scattering"));
    }
}
```

* Run the test to verify the service works as expected.
* Modify the signature of the `generate` method to take a `String` argument called `model` and use that value in the request.
* Add a `public static final String` constant called `LLAMA3` whose value is "llama3".
* Update the test to use that model instead of hardcoding the value.

## Parse JSON into records

- Create a new class called `OllamaRecords`. Inside it, add a record called `OllamaRequest` with the following fields:
  - `model` of type `String`
  - `prompt` of type `String`
  - `stream` of type `boolean`

```java
public record OllamaRequest(
        String model, 
        String prompt, 
        boolean stream) {}
```

- Create a new record called `OllamaResponse` with the following fields:
  - `model` of type `String`,
  - `createdAt` of type `String` (this will be a date string in ISO 8601 standard format),
  - `done` of type `boolean`,
  - `totalDuration` of type `long`,
  - `promptEvalCount` of type `int`,
  - `evalCount` of type `int`

```java
public record OllamaResponse(
        String model, 
        String createdAt, 
        String response,
        boolean done, 
        long totalDuration, 
        int promptEvalCount, 
        int evalCount) {}
```

* In your build file (`build.gradle` or `build.gradle.kts` for Gradle, `pom.xml` for Maven), add the following dependencies:
  - For Gradle:

```groovy
dependencies {
    implementation 'com.google.code.gson:gson:2.11.0'
}
```
    
- For Maven:

```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.11.0</version>
</dependency>
```

* Add an overload of the `generate` method to `OllamaService` that takes an `OllamaRequest` object and returns an `OllamaResponse` object.
* Add an attribute to the class of type `Gson`. Use the `GsonBuilder` class to configure a `Gson` instance:
    
  ```java
  private final Gson gson = new GsonBuilder()
    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    .setPrettyPrinting()
    .create();
  ```

NOTE: The field naming policy will convert camelCase field names in Java to snake_case in JSON.

* Update the `generate` method to use the `OllamaRequest` object to create the JSON string to send in the request.

```java
String request = gson.toJson(request);
```

* Update the method to return an `OllamaResponse` object by parsing the JSON response from the server.

```java
var response = gson.fromJson(response.body(), OllamaResponse.class);
```
  
* Update the original `generate` method to call the new method with the `OllamaRequest` object.

```java
public String generate(String request) {
    return generate(new OllamaRequest("llama3", request, false)).response();
}
```

* Add a test to verify the new method works. Here is a sample:

```java
@Test
public void testGenerateRequest() {
    OllamaService service = new OllamaService();
    OllamaRequest request = new OllamaRequest("llama3", "Why is the sky blue?", false);
    OllamaResponse response = service.generate(request);
    assertTrue(response.response().contains("scattering"));
}
```

* Update the previous test to use the new method signature.

## Create a vision request

* Ollama also supports vision models, like `llava`, that can read images and generate text descriptions from them. The images must be uploaded in the form of Base 64 encoded strings.
* Create a new record called `OllamaVisionRequest` with the following fields:
  - `model` of type `String`
  - `image` of type `String`
  - `stream` of type `boolean`
  - `images` of type `List<String>`

```java
public record OllamaVisionRequest(
        String model, 
        String image, 
        boolean stream, 
        List<String> images) {}
```

* Fortunately, the response is the same as the text models, so you can reuse the `OllamaResponse` record.
* Rather than require the client to submit the image in that form, let's provide a method to convert an image from a URL into the proper string. We'll call this method inside a _compact constructor_, a cool feature of records.
* Inside the `OllamaVisionRequest` record, add a compact constructor that takes a `String` path to a local image and converts it to a Base 64 encoded string.

```java
public record OllamaVisionRequest(
        String model, 
        String image, 
        boolean stream, 
        List<String> images) {
                           
    public OllamaVisionRequest {
        images = images.stream()
                    .map(this::encodeImage)
                    .collect(Collectors.toList());    
    }
    
    private String encodeImage(String path) {
        try {
            byte[] imageBytes = Files.readAllBytes(Paths.get(path));
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
```

* Now comes the question: how do we send the vision request to the service? We'd rather not duplicate all the existing code. Fortunately, records can implement interfaces. In this case, we'll use a _sealed interface_, so that only _permitted_ classes can implement it.
* Rename the `OllamaRequest` record to `OllamaTextRequest`.
* Create a sealed interface called `OllamaRequest` with two permitted classes: `OllamaTextRequest` and `OllamaVisionRequest`.

```java
public sealed interface OllamaRequest permits OllamaTextRequest, OllamaVisionRequest {}
```

* Make both `OllamaTextRequest` and `OllamaVisionRequest` implement `OllamaRequest`.
* Add another constant to `OllamaService` called `LLAVA` with the value "llava". Download the "llava" model by running the command `ollama run llava`.
* Add a test to try out the vision model:

```java
@Test
void test_vision_generate() {
    var request = new OllamaVisionRequest(OllamaService.LLAVA,
            """
            Generate a text description of this image
            suitable for accessibility in HTML.
            """,
            false,
            List.of("src/main/resources/images/stablediffusion/cats_playing_cards.png"));
    OllamaResponse response = service.generate(request);
    assertNotNull(response);
    System.out.println(response);
}
```
  
* Pattern matching for switch became GA in Java 21. We don't actually need it here, but you can see how it works by adding the following switch statement to the `generate` method:

```java
switch (request) {
    case OllamaTextRequest textRequest -> {
        System.out.printf("Generating text response from %s...%n", textRequest.model());
    }
    case OllamaVisionRequest visionRequest -> {
        System.out.printf("Generating vision response from %s...%n", visionRequest.model());
    }
}
```

## Have a conversation

* Ollama also supports conversation models that can have a conversation with you. You need to add the messages yourself in a list, alternating between `user` and `assistant` messages.
* Ollama supports a different endpoint for this. You send a POST request to `/api/chat` instead of `/api/generate`.
* Here is a sample request (from the Ollama documentation):

```json
{
    "model": "llama3",
    "messages": [
      {
        "role": "user",
        "content": "why is the sky blue?"
      },
      {
        "role": "assistant",
        "content": "due to rayleigh scattering."
      },
      {
        "role": "user",
        "content": "how is that different than mie scattering?"
      }
    ]
}
```

* Create a new record called `OllamaChatRequest` with the following fields:
  - `model` of type `String`
  - `messages` of type `List<OllamaMessage>`
  - 'temperature' of type `double`

```java
public record OllamaChatRequest(
        String model, 
        List<OllamaMessage> messages,
        boolean stream) {}
```

* Create a new record called `OllamaMessage` with the following fields:
  - `role` of type `String`
  - `content` of type `String`

```java
public record OllamaMessage(String role, 
                            String content) {}
```

* Add a record called `OllamaChatResponse` with the following fields:
  - `model` of type `String`
  - `createdAt` of type `String`
  - `messages` of type `List<OllamaMessage>`
  - `done` of type `boolean`
  - `totalDuration` of type `long`
  - `promptEvalCount` of type `int`
  - `evalCount` of type `int`

```java
public record OllamaChatResponse(
        String model, 
        String createdAt, 
        List<OllamaMessage> messages,
        boolean done, 
        long totalDuration, 
        int promptEvalCount, 
        int evalCount) {}
```

* Add a new method to `OllamaService` called `chat` that takes an `OllamaChatRequest` object and returns a `List<OllamaMessage>` object.
* Add a test to try out the conversation model:

```java
@Test
void test_chat() {
    var request = new OllamaChatRequest(OllamaService.LLAMA3,
            List.of(new OllamaMessage("user", "why is the sky blue?"),
                    new OllamaMessage("assistant", "due to rayleigh scattering."),
                    new OllamaMessage("user", "how is that different than mie scattering?")));
    List<OllamaMessage> response = service.chat(request);
    assertNotNull(response);
    response.forEach(System.out::println);
}
```

* Update the `OllamaService` class to include the `chat` method:

```java
public List<OllamaMessage> chat(OllamaChatRequest request) {
    String json = gson.toJson(request);
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(URL + "/api/chat"))
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(json))
        .build();
    try {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(response.body(), new TypeToken<List<OllamaMessage>>() {}.getType());
    } catch (IOException | InterruptedException e) {
        throw new RuntimeException(e);
    }
}
```




