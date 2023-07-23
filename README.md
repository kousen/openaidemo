# openaidemo
Demo of how access the OpenAI API using Java 17.
First presented as part of my presentation called _AI Tools for Java Developers_ at UberConf, Westminster, CO, July 2023.

To use, add your own OpenAI key as a system property under the name `OPENAI_API_KEY`.

The `ServiceDemo` class has a simple `main` method, but the actual system is driven by the tests in `ChatGPTTest.java`.

NOTE: Requires Java 17 or later, because it uses `HttpClient`, records, and text blocks.

One of the tests transmits the srt (video transcript) file located under `src/main/resources`. It's likely the most expensive test, though it costs less than a penny to run.