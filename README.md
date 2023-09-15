# openaidemo
Demo of how access the OpenAI API for Chat and Whisper, and image generation using Picogen, DallE, and Stable Diffusion, all with Java 17.

For detailed explanations, see the playlist at https://www.youtube.com/watch?v=vRvlqFQGLzQ&list=PLZOgUaAUCiT7o0FAUWId7oeWatv6b3oCD&pp=iAQB on the [Tales from the jar side YouTube channel](https://www.youtube.com/@talesfromthejarside?sub_confirmation=1).

NOTE: Requires Java 17 or later, because it uses `HttpClient`, records, and text blocks.

One of the tests generates a transcription of a video located under `src/main/resources`. It's likely the most expensive test, though it costs less than a penny to run.

The following API codes are read as system properties using `System.getenv(...)`:

* `OPENAI_API_KEY` - required for `ServiceDemo.java`, `ChatGPT.java`, and `WhisperAI.java`
* `PICOGEN_API_KEY` - used by `Picogen.java`
* `STABILITY_API_KEY` - used by `StabilityAI.java`

To execute the `ImageCarousel` class, which reads all the downloaded images in the `src/main/resources/images` folder and its subfolders and displays a rotating carousel with JavaFX, use the `./gradlew run` task.
