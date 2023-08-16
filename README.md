# openaidemo
Demo of how access the OpenAI API for Chat and Whisper, and image generation using Picogen, all with Java 17.
First presented as part of my presentation called _AI Tools for Java Developers_ at UberConf, Westminster, CO, July 2023.

The `ServiceDemo` class has a simple `main` method, but the actual system is driven by the tests in `ChatGPTTest.java`.

NOTE: Requires Java 17 or later, because it uses `HttpClient`, records, and text blocks.

One of the tests transmits the srt (video transcript) file located under `src/main/resources`. It's likely the most expensive test, though it costs less than a penny to run.

The following API codes are read as system properties using `System.getenv(...)`:

* `OPENAI_API_KEY` - required for `ServiceDemo.java`, `ChatGPT.java`, and `WhisperAI.java`
* `PICOGEN_API_KEY` - used by `Picogen.java`

To execute the `ImageDownloader` class, which downloads the current job list from Picogen and displays the first image with JavaFX, use the `./gradlew run` task.
