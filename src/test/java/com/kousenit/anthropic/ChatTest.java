package com.kousenit.anthropic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kousenit.anthropic.json.GsonLocalDateAdapter;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class ChatTest {
    private final Chat chat = new Chat();

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new GsonLocalDateAdapter())
            .create();

    @Test
    void getHHG2tGResponse() {
        var response = chat.getResponse(
                """
                        According to Douglas Adams, what is the Ultimate Answer
                        to the Ultimate Question of Life, the Universe, and Everything?
                        """
        );
        System.out.println(response);
        assertThat(response).contains("42");
    }

    @Test
    void getResponseWithXML() {
        var response = chat.getResponse("""
                You are a Spring developer expert. Please generate a
                multiple-choice quiz with 4 possible answers. The question
                topic should be <topic>%s</topic>. Please use the latest
                versions of Spring Boot and Spring Framework, which at the
                time of this writing are 3.1 and 6.0, respectively.
                                        
                Indicate with a * which answer or answers is/are correct.
                Before answering, please think about the question within
                <thinking></thinking> tags.
                """.formatted("The @GetExchange annotation in an Http Interface")
        );
        System.out.println(response);
    }

    @Test
    void extractDataIntoRecord() {
        var response = chat.getResponse("""
                Here is a Java record representing a person:
                record Person(String firstName, String lastName, LocalDate dob) {}
                                                
                Here is a passage of text that includes information about a person:
                <person>
                Captain Picard was born on the 13th of juillet, %d years from now,
                in La Barre, France, Earth. His given name, Jean-Luc, is of French
                origin and translates to "John Luke".
                </person>
                                                
                Please extract the relevant fields into a Person instance.
                """.formatted(2305 - LocalDate.now().getYear())
        );
        assertThat(response).contains("Jean-Luc", "Picard", "record Person");

        var jsonResponse = chat.getResponse("""
                The Java code in %s represents a Person record.
                Please reply ONLY with its JSON representation, in this form:
                {
                    "firstName": <firstName></firstName>,
                    "lastName": <lastName></lastName>,
                    "dob": <dob>date of birth string is ISO-8601 format</dob>
                }
                """.formatted(response));
        String jsonPart = jsonResponse.contains("```json") ?
                parseJSONFromResponse(jsonResponse) : jsonResponse;
        var person = gson.fromJson(jsonPart, Person.class);
        System.out.println(person);
        assertAll(
                () -> assertThat(person.firstName()).isEqualTo("Jean-Luc"),
                () -> assertThat(person.lastName()).isEqualTo("Picard"),
                () -> assertThat(person.dob().getDayOfMonth()).isEqualTo(13),
                () -> assertThat(person.dob().getMonth()).isEqualTo(Month.JULY),
                () -> assertThat(person.dob().getYear()).isCloseTo(2305, Offset.offset(2))
        );
    }

    private String parseJSONFromResponse(String response) {
        Pattern pattern = Pattern.compile("```json\n(.*)\n```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);
        String json = "";
        if(matcher.find()){
            json = matcher.group(1);
        }
        return json;
    }

    @Test
    void parseJson() {
        String response = """
                Here is the JSON representation of the Person instance:
                                                                               
                ```json
                {
                   "firstName": "Jean-Luc",
                   "lastName": "Picard",
                   "dob": "2305-07-13"
                }
                ```
                JSON:  Here is the JSON representation of the Person instance:
                                                                               
                {
                    "firstName": "Jean-Luc",
                    "lastName": "Picard",
                    "dob": "2305-07-13"
                }
                                                                               
                """;
        String json = parseJSONFromResponse(response);
        System.out.println(json);
        var person = gson.fromJson(json, Person.class);
        System.out.println(person);
        assertAll(
                () -> assertThat(person.firstName()).isEqualTo("Jean-Luc"),
                () -> assertThat(person.lastName()).isEqualTo("Picard"),
                () -> assertThat(person.dob()).isEqualTo(LocalDate.of(2305, 7, 13))
        );
    }

}