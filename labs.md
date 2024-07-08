# Labs

## Generate Audio from Text

- At the [OpenAI developer site](https://platform.openai.com/docs/overview), sign up for an account and create an API key. Currently the link to your API keys is at https://platform.openai.com/api-keys . OpenAI now recommends project API keys. See [this page](https://help.openai.com/en/articles/9186755-managing-your-work-in-the-api-platform-with-projects) for more information.
- Add the API key to an environment variable called `OPENAI_API_KEY`.
- Create a new Java project called `aijavalabs` in your favorite IDE with either Gradle or Maven as your build tool.
- Review the documentation for the Open AI [Text-to-speech](https://platform.openai.com/docs/guides/text-to-speech) API.
- Create a new class called `TextToSpeechService` in a package of your choosing.
- Add a private, static, final attribute called `OPENAI_API_KEY` of type `String` that reads the API key from the environment variable.

```java
private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
```
- Add a method called `generateMp3` that returns a `byte[]`. The method should take three arguments:
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

- To send a request, you need an `HttpClient` instance. Starting in Java 21, the `HttpClient` class implements `AutoCloseable`, so you can use it in a try-with-resources block. Here is the code to send the request:

```java
public byte[] generateMp3(String model, String input, String voice) {
  
    // ... from before ...
  
    try (HttpClient client = HttpClient.newHttpClient()) {
        HttpResponse<byte[]> response =
                client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        return response.body();
    } catch (IOException | InterruptedException e) {
        throw new RuntimeException(e);
    }
}
```

- You'll probably want to save the returned byte array to a file. Create a class called `FileUtils` and add the following static method:

```java
public static void saveBinaryFile(byte[] data, String fileName) {
    Path directory = Paths.get("src/main/resources");
    Path filePath = directory.resolve(fileName);
    try {
        Files.write(filePath, data, StandardOpenOption.CREATE_NEW);
        System.out.printf("Saved %s to %s%n", fileName, directory.toAbsolutePath());
    } catch (IOException e) {
        throw new UncheckedIOException("Error writing audio to file", e);
    }
}
```

- Add a test to drive the system:

```java

@Test
void testGenerateMp3() {
    var service = new TextToSpeechService();
    byte[] result = service.generateMp3(
          "tts-1",
          """
             Now that I know how to generate audio from text,
             I can use this feature in my applications.""",
          "fable"
    );
    
    FileUtils.saveBinaryFile(result, "test.mp3");
    assertNotNull(result);
    assertTrue(result.length > 0);
}
```

- Run the test. The console should that `test.mp3` was saved in the `src/main/resources` folder. You can play the file to hear the text you provided.

## List the OpenAI Models

- Whenever you access the OpenAI API, you need to specify a model. To know which models are available, the OpenAI API provides an endpoint to list them. Review the documentation for that endpoint [here](https://platform.openai.com/docs/api-reference/models).
- You'll find that our task is to send an HTTP GET request to `https://api.openai.com/v1/models` with the required `Authorization` header, and that the result is a block of JSON data.
- We're going to want to parse that JSON data, as well as generate JSON for other interactions. For that, we need a parser. We'll use the Gson library from Google. Add the following dependency to your build file:

```kotlin
dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
}
```

- Add a class called `OpenAiRecords` to your project. It will hold all the records we need to parse the JSON data.
- Inside the `OpenAiRecords` class, add a record called `ModelList`, which itself contains a record called `Model`:

```java
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OpenAiRecords {
  // List the models
  public record ModelList(List<Model> data) {
    public record Model(
            String id,
            long created,
            @SerializedName("owned_by") String ownedBy) {
    }
  }
}
```

- Create a new class called `OpenAiService` in a package of your choosing.
- Add a private, static, final attribute called `OPENAI_API_KEY` of type `String` that reads the API key from the environment variable.

```java
private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
```

- Also add a constant for the Models endpoint:

```java
private static final String MODELS_URL = "https://api.openai.com/v1/models";
```

- Add a private, static, final attribute called `GSON` of type `Gson` that creates a new `Gson` instance:

```java
private final Gson gson = new Gson();
```

- Add a method called `listModels` that returns a `List<String>`. This method will send a GET request to the OpenAI API and return a `ModelList`.

```java
public ModelList listModels() {
    HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(MODELS_URL))
          .header("Authorization", "Bearer %s".formatted(API_KEY))
          .header("Accept", "application/json")
          .build();
    try (var client = HttpClient.newHttpClient()) {
        HttpResponse<String> response =
            client.send(request, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(response.body(), ModelList.class);
    } catch (IOException | InterruptedException e) {
        throw new RuntimeException(e);
    }
}
```

- Add a test to drive the system:

```java
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static com.kousenit.OpenAiRecords.ModelList;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenAiServiceTest {
  private final OpenAiService service = new OpenAiService();

  @Test
  void listModels() {
    List<String> models = service.listModels().data().stream()
            .map(ModelList.Model::id)
            .sorted()
            .toList();

    models.forEach(System.out::println);
    assertTrue(new HashSet<>(models).containsAll(
            List.of("dall-e-3", "gpt-3.5-turbo", "gpt-4o",
                    "tts-1", "whisper-1")));
    }
}
```

- Run the test. You should see a list of model names in the console, and the assertion should pass.

## Install Ollama

- Download the latest version of Ollama from the [Ollama website](https://ollama.com). 
- Run the installer and follow the instructions. 
- From a command prompt, enter `ollama run gemma2` to download and install the Gemma 2 model from Google (an open source model based on their Gemini model) locally. 
- Try out a couple of sample prompts at the command line, like `Why is the sky blue?` or `Given the power required to train large language models, how do companies ever expect to make money?`.
- When you're finished, type `/bye` to exit.

## Access Ollama programmatically

- Ollama installs a small web server on your system. Verify that it is running by accessing [http://localhost:11434](http://localhost:11434) in a browser. You should get back the string, `Ollama is running`.
- Ollama maintains a programmatic API accessible through a RESTful web service. The documentation is located [here](https://github.com/ollama/ollama/blob/main/docs/api.md).
- We'll start by accessing the `api/generate` endpoint, as it is the simplest. It takes a JSON object with three fields: `model`, `prompt`, and `stream`. The `model` field is the name of the model you want to use (`gemma2` in our case), the `prompt` field is the question you want the model to answer, and the `stream` field is a boolean that determines whether the response should be streamed back to the client.
- Create a Java project (or simply add to the existing one) in your favorite IDE with either Gradle or Maven as your build tool.
- Create a class called `OllamaService` in a package of your choosing.
- Add a method called `generate` that takes a `String` and returns a `String`.
- If you created a new project, add the Gson dependency to your build file and synchronize the project. If you added to an existing project, you can skip this step.
- Model the input and output JSON with Java records. To do so, add a class called `OllamaRecords` to your project.

```java
public class OllamaRecords {
    public record OllamaRequest(String model, String prompt, boolean stream) {
    }
}
```

- The output consists of a JSON object with `model`, `created_at`, `response`, and `done` field (among other optional fields we won't use right now). Add a record for that:

```java
public class OllamaRecords {
    public record OllamaRequest(
            String model, 
            String prompt, 
            boolean stream) {
    }

    public record OllamaResponse(
            String model, 
            String created_at, // Shouldn't this be camel case?
            String response, 
            boolean done) {
    }
}
```

- Now we have enough to create the `OllamaService` class and use it to access the chat endpoint. Add the following code to the `OllamaService` class:

```java
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import static com.kousenit.OllamaRecords.*;

public class OllamaService {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final String URL = "http://localhost:11434";
    
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public OllamaResponse generate(OllamaRequest request) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(URL + "/api/generate"))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(
                    gson.toJson(ollamaRequest)))
            .build();
        try {
            HttpResponse<String> response = 
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), OllamaResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

* Create a test class to verify the `OllamaService` class works as expected. Here is a sample:

```java
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static com.kousenit.OllamaRecords.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class OllamaServiceTest {
    private final OllamaService service = new OllamaService();
  
    @Test
    public void testGenerate() {
        var ollamaRequest = new OllamaRequest("gemma2", "Why is the sky blue?", false);
        OllamaResponse ollamaResponse = service.generate(ollamaRequest);
        String answer = ollamaResponse.response();
        System.out.println(answer);
        assertTrue(answer.contains("scattering"));
    }
}
```

* Run the test to verify the service works as expected.
* Add an overload of the `generate` method to `OllamaService` that takes two Strings: one for the `model` and one for the `prompt`, and returns an `OllamaResponse` object.

```java
public OllamaResponse generate(String model, String prompt) {
    return generate(new OllamaRequest(model, prompt, false));
}
```

* Adding convenience methods like that to make the calls easier for a client is a common practice. Add a test to verify the new method works.

```java
@Test
void generate_with_model_and_name() {
    var ollamaResponse = service.generate("gemma2", "Why is the sky blue?");
    String answer = ollamaResponse.response();
    System.out.println(answer);
    assertTrue(answer.contains("scattering"));
}
```

## Streaming Results

* To see what a streaming result looks like, add a method to send the message and return the response as a `String`:

```java
public String generateStreaming(OllamaRequest ollamaRequest) {
    HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(URL + "/api/generate"))
          .header("Content-Type", "application/json")
          .header("Accept", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(ollamaRequest)))
          .build();
  try {
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    return response.body();
  } catch (IOException | InterruptedException e) {
    throw new RuntimeException(e);
  }
}
```

* Add a test to see the streaming result:

```java
@Test
public void streaming_generate_request() {
    var request = new OllamaRequest("gemma2", "Why is the sky blue?", true);
    String response = service.generateStreaming(request);
    System.out.println(response);
}
```

* Run the test to see the streaming result. It will be similar to:

```json lines
{"model":"gemma2","created_at":"2024-07-08T20:15:46.965689Z","response":"The","done":false}
{"model":"gemma2","created_at":"2024-07-08T20:15:46.991009Z","response":" sky","done":false}
{"model":"gemma2","created_at":"2024-07-08T20:15:47.019408Z","response":" appears","done":false}
{"model":"gemma2","created_at":"2024-07-08T20:15:47.050245Z","response":" blue","done":false}
{"model":"gemma2","created_at":"2024-07-08T20:15:47.095448Z","response":" due","done":false}
{"model":"gemma2","created_at":"2024-07-08T20:15:47.126318Z","response":" to","done":false}
```

followed by lots more lines, until the `done` field is `true`.

## Fix the camel case issue

* The `created_at` field in the `OllamaResponse` record should be `createdAt`. You can fix this by adding a `@SerializedName` annotation to the field:

```java
public record OllamaResponse(
        String model, 
        @SerializedName("created_at") String createdAt, 
        String response, 
        boolean done) {
}
```

* Run one of the existing tests to see that the field is populated correctly.
* As an alternative, you can create the `Gson` object using a builder and set a field naming policy. In that case, inside the `OllamaService`, you would create the `Gson` object like this:

```java
private final Gson gson = new GsonBuilder()
    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    .setPrettyPrinting()
    .create();
```

* Make sure the test prints the full `OllamaResponse`, then run the test again to see that the field is populated correctly. You may also want to add an assertion that the `createdAt` field of the response is not null.

## Create a vision request

* Ollama also supports vision models, like `moondream`, that can read images and generate text descriptions from them. The images must be uploaded in the form of Base 64 encoded strings.
* We're going to need a _multimodal_ model that supports vision requests. A small one, useful for experiements, is called `moondream`. Download it by running the command `ollama pull moondream`. (Note this is a `pull` rather than a `run` -- we don't plan to run the vision model at the command line.)
* Add a new record to `OllamaRecords` called `OllamaVisionRequest` with the following fields:
  - `model` of type `String`
  - `prompt` of type `String`
  - `stream` of type `boolean`
  - `images` of type `List<String>`

```java
public record OllamaVisionRequest(
        String model, 
        String prompt, 
        boolean stream, 
        List<String> images) {}
```

* Fortunately, the output response from vision models is the same as from the text models, so you can reuse the `OllamaResponse` record.
* Rather than require the client to submit the image in that form, let's provide a method to convert an image from a URL into the proper string. We'll call this method inside a _compact constructor_, a cool feature of records.
* Inside the `OllamaVisionRequest` record, add a compact constructor that takes a `String` path to a local image and converts it to a Base 64 encoded string.

```java
public record OllamaVisionRequest(
        String model, 
        String prompt, 
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

* Add a new method to `OllamaService` called `generateVision` that takes an `OllamaVisionRequest` object and returns an `OllamaResponse` object.
* We need an image to view. Here is one if you don't already have one available:

![Cats playing cards](src/main/resources/images/stablediffusion/cats_playing_cards.png)

* Add the following test to the `OllamaServiceTest` class:

```java
    
@Test
void test_vision_generate() {
    var request = new OllamaVisionRequest("moondream",
            """
            Generate a text description of this image
            suitable for accessibility in HTML.
            """,
            false,
            List.of("src/main/resources/images/stablediffusion/cats_playing_cards.png"));
    OllamaResponse ollamaResponse = service.generateVision(request);
    assertNotNull(ollamaResponse);
    System.out.println(ollamaResponse.response());
}
``` 

* Add the `generateVision` method to the `OllamaService` class:

```java
public OllamaResponse generateVision(OllamaVisionRequest visionRequest) {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(URL + "/api/generate"))
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(
                gson.toJson(visionRequest)))
        .build();
    try {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(response.body(), OllamaResponse.class);
    } catch (IOException | InterruptedException e) {
        throw new RuntimeException(e);
    }
}
```

* The result is inside the `response` field of the `OllamaResponse` object. Run the test to see the result.

* That's an awful lot of duplicated code. Surely we can do better? We can, by using a _sealed interface_.

## Refactor to sealed interfaces

* Now comes the question: how do we send the vision request to the service? We'd rather not duplicate all the existing code. Fortunately, records can implement interfaces. In this case, we'll use a _sealed interface_, so that only _permitted_ classes can implement it.
* Refactor our existing code by renaming the `OllamaRequest` record to `OllamaTextRequest`.
* Make sure the tests for the text model still pass.
* Create a sealed interface called `OllamaRequest` with two permitted classes: `OllamaTextRequest` and `OllamaVisionRequest` and add it to our `OllamaRecords` class. Add `implements OllamaRequest` to both records.

```java
public class OllamaRecords {
    public sealed interface OllamaRequest
            permits OllamaTextRequest, OllamaVisionRequest {
    }
    
    public record OllamaTextRequest(
            String model, 
            String prompt, 
            boolean stream) implements OllamaRequest {}
  
    public record OllamaVisionRequest(
            String model, 
            String prompt, 
            boolean stream, 
            List<String> images) implements OllamaRequest {

        public OllamaVisionRequest {
            images = images.stream()
                    .map(this::encodeImage)
                    .collect(Collectors.toList());
        }

        private String encodeImage(String path) {
            try {
                byte[] imageBytes = Files.readAllBytes(Paths.get(path));
                return Base64.getEncoder()
                        .encodeToString(imageBytes);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
```

* In `OllamaService`, we can go back to when our `generate` method took an `OllamaRequest` object as its only argument and let polymorphism distinguish between a text request and a vision request. We can then use pattern matching to determine which type of request we have and act accordingly. We had this before, but let's repeat it here for convenience:

```java
public OllamaResponse generate(OllamaRequest ollamaRequest) {
    HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(URL + "/api/generate"))
          .header("Content-Type", "application/json")
          .header("Accept", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(ollamaRequest)))
          .build();
    try {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(response.body(), OllamaResponse.class);
    } catch (IOException | InterruptedException e) {
        throw new RuntimeException(e);
    }
}
```
  
* Pattern matching for switch became GA in Java 21. If you have Java 21 available, let's use it to identify what type of request we are sending. First, add a system logger as an attribute to `OllamaService`:

```java
private final System.Logger logger = System.getLogger(OllamaService.class.getName());
```

* With that available, we can now add a switch statement to the beginning of the `generate` method:

```java
public OllamaResponse generate(OllamaRequest ollamaRequest) {
    switch (ollamaRequest) {
        case OllamaTextRequest textRequest -> {
            logger.log(System.Logger.Level.INFO, "Text request to: {0}", textRequest.model());
            logger.log(System.Logger.Level.INFO, "Prompt: {0}", textRequest.prompt());
        }
        case OllamaVisionRequest visionRequest -> {
            logger.log(System.Logger.Level.INFO, "Vision request to: {0}", visionRequest.model());
            logger.log(System.Logger.Level.INFO, "Size of uploaded image: {0}", visionRequest.images()
                    .getFirst()
                    .length());
            logger.log(System.Logger.Level.INFO, "Prompt: {0}", visionRequest.prompt());
        }
    }

    // ... rest as before ...
}
```

* Because we used a sealed interface, there are only two possible types of requests. Our switch expression is therefore exhaustive. We can add tests for each to verify that the logging works as expected.

```java
@Test
void generate_with_text_request() {
  var ollamaRequest = new OllamaTextRequest("gemma2", "Why is the sky blue?", false);
  OllamaResponse ollamaResponse = service.generate(ollamaRequest);
  System.out.println(ollamaResponse);
  String answer = ollamaResponse.response();
  System.out.println(answer);
  assertTrue(answer.contains("scattering"));
  assertNotNull(ollamaResponse.createdAt());
}

@Test
void generate_with_vision_request() {
  var request = new OllamaVisionRequest("moondream",
          """
                  Generate a text description of this image
                  suitable for accessibility in HTML.
                  """,
          false,
          List.of("src/main/resources/cats_playing_cards.png"));
  OllamaResponse response = service.generateVision(request);
  assertNotNull(response);
  System.out.println(response);
}
```

* Run the tests to verify that the logging works as expected. This combination of modeling the data as records, limiting the available entries with sealed interfaces, and selecting between them with switch expressions, is a form of [data oriented programming](https://www.infoq.com/articles/data-oriented-programming-java/), as described in the linked article by Brian Goetz.

## Have a conversation

* The `generate` endpoint we've been using is very limited. Ollama also supports a more complicated model, similar to those from OpenAI, Anthropic, Gemini, and Mistral. That model allows _conversations_, that connect messages together. You need to add the messages yourself in a list, alternating between `user` and `assistant` messages, and the AI tool will respond to the complete sequence.
* In Ollama, you send a POST request to `/api/chat` instead of `/api/generate` when you're dealing with a list of messages rather than just a single one.
* Here is a sample request (from the [Ollama documentation](https://github.com/ollama/ollama/blob/main/docs/api.md)):

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

* Create a new record called `OllamaMessage` with the following fields:
  - `role` of type `String`
  - `content` of type `String`

```java
public record OllamaMessage(
        String role, 
        String content) {}
```

* The value of `role` is restricted to:
  - `user`, for client questions
  - `assistant`, for AI responses
  - `system`, for global context
- With that in mind, add a compact constructor to `OllamaMessage` that validates the `role` field:

```java
public record OllamaMessage(String role, 
                            String content) {
    public OllamaMessage {
        if (!List.of("user", "assistant", "system").contains(role)) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }
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


* Add a record called `OllamaChatResponse` with the following fields:
  - `model` of type `String`
  - `createdAt` of type `String`
  - `message` of type `OllamaMessage`
  - `done` of type `boolean`

```java
public record OllamaChatResponse(
        String model, 
        String createdAt, 
        OllamaMessage message,
        boolean done) {}
```

* Add a new method to `OllamaService` called `chat` that takes an `OllamaChatRequest` object and returns a `List<OllamaMessage>` object.
* Add a test to try out the conversation model:

```java
@Test
void test_chat() {
    var request = new OllamaChatRequest("gemma2",
            List.of(new OllamaMessage("user", "why is the sky blue?"),
                    new OllamaMessage("assistant", "due to rayleigh scattering."),
                    new OllamaMessage("user", "how is that different than mie scattering?")),
            false);
    OllamaChatResponse response = service.chat(request);
    assertNotNull(response);
    System.out.println(response);
}
```

* Update the `OllamaService` class to include the `chat` method:

```java
public OllamaChatResponse chat(OllamaChatRequest chatRequest) {
    String json = gson.toJson(chatRequest);
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(URL + "/api/chat"))
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(json))
        .build();
    try {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(response.body(), OllamaChatResponse.class);
    } catch (IOException | InterruptedException e) {
        throw new RuntimeException(e);
    }
}
```

* Run the test to see it in action. In practice, you would extract the `assistant` response from each message and add it to the next request. Note that all the major framework (Spring AI, LangChain4j, etc) have ways of doing that for you.

## Generating Images

* We now return to OpenAI to use it's DALL-E 3 image model. Generating images is straightforward. Again, we'll create an image request object that models the input JSON data, send it in a POST request, and process the results. It turns out we can retrieve the response as either a URL or a Base 64 encoded string.

* Add a record called `DalleImageRequest` :

```java
public record ImageRequest(
        String model,
        String prompt,
        Integer n,
        String quality,
        String responseFormat,
        String size,
        String style
) {}
```

* The response wraps either a URL to the generated image, or the actual Base 64 encoded data. For this exercise, we'll get the URL. Add a record called `DalleImageResponse`:

```java
public record ImageResponse(
        long created,
        List<Image> data) {
  public record Image(
          String url,
          String revisedPrompt) {}
}
```

* Add a class called `DalleService` with constants for the endpoint, the API key, and a `Gson` object:

```java
public class DalleService {
  private static final String IMAGE_URL = "https://api.openai.com/v1/images/generations";
  private static final String API_KEY = System.getenv("OPENAI_API_KEY");

  private final Gson gson = new GsonBuilder()
          .setPrettyPrinting()
          .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
          .create();
}
```

* Add a new method to `DalleService` called `generateImage` that takes a `DalleImageRequest` object and returns a `DalleImageResponse` object.

```java
public ImageResponse generateImage(ImageRequest imageRequest) {
  HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(IMAGE_URL))
          .header("Authorization", "Bearer %s".formatted(API_KEY))
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(imageRequest)))
          .build();
  try (HttpClient client = HttpClient.newHttpClient()) {
    HttpResponse<String> response =
            client.send(request, HttpResponse.BodyHandlers.ofString());
    return gson.fromJson(response.body(), ImageResponse.class);
  } catch (IOException | InterruptedException e) {
    throw new RuntimeException("Error sending prompt prompt", e);
  }
}
```

* Add a test to try out the image generation model:

```java
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static com.kousenit.OpenAiRecords.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class DalleServiceTest {
    private final DalleService service = new DalleService();

    @Test
    void generate_image() {
        var imageRequest = new ImageRequest(
            "dall-e-3",
            "Draw a picture of cats playing cards",
            1,
            "standard",
            "url",
            "1024x1024",
            "natural");
        ImageResponse imageResponse = service.generateImage(imageRequest);
        System.out.println(imageResponse.data().getFirst().revisedPrompt());
        System.out.println(imageResponse.data().getFirst().url());
    }
}
```

* Run the test to see it in action. The response will contain a URL to the generated image, with you can either click on or copy and paste into a browser to download the image.

