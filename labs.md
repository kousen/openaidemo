# Labs

## Lab 1: Install Ollama

- Download the latest version of Ollama from the [Ollama website](https://ollama.com). 
- Run the installer and follow the instructions. 
- From a command prompt, enter `ollama run llama3` to download and install llama3 locally. 
- Feel free to enter some prompts when the download completes. When you're finished, type `/bye` to exit.

## Lab 2: Access Ollama programmatically

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

## Lab 3: Parse the strings into records

- Create a new class called `OllamaRecords`. Inside it, add a record called `OllamaRequest` with the following fields:
  - `model` of type `String`
  - `prompt` of type `String`
  - `stream` of type `boolean`

```java
public record OllamaRequest(String model, 
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
public record OllamaResponse(String model, 
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
    implementation 'com.google.code.gson:gson:2.10.1'
}
```
    
- For Maven:

```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
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
OllamaResponse response = gson.fromJson(response.body(), OllamaResponse.class);
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

## Lab 4: Create a vision request

* Ollama also supports vision models, like `llava`, that can read images and generate text descriptions from them. The images must be uploaded in the form of Base 64 encoded strings.
* Create a new record called `OllamaVisionRequest` with the following fields:
  - `model` of type `String`
  - `image` of type `String`
  - `stream` of type `boolean`
  - `images` of type `List<String>`

```java
public record OllamaVisionRequest(String model, 
                                  String image, 
                                  boolean stream, 
                                  List<String> images) {}
```

* Fortunately, the response is the same as the text models, so you can reuse the `OllamaResponse` record.
* Rather than require the client to submit the image in that form, let's provide a method to convert an image from a URL into the proper string. We'll call this method inside a _compact constructor_, a cool feature of records.
* Inside the `OllamaVisionRequest` record, add a compact constructor that takes a `String` path to a local image and converts it to a Base 64 encoded string.

```java
public record OllamaVisionRequest(String model, 
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
            "describe the included image",
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

## Lab 5: Have a conversation

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
public record OllamaChatRequest(String model, 
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
public record OllamaChatResponse(String model, 
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




