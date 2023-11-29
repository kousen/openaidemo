package com.kousenit.anthropic;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ChatTest {
    private final Chat chat = new Chat();

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
        var response = chat.getResponse(
                """
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
        var response = chat.getResponse(
                        """
                        Here is a Java record representing a person:
                        record Person(String firstName, String lastName, LocalDate dob) {}
                        
                        Here is a passage of text that includes information about a person:
                        <person>
                        Captain Picard was born on the 13th of juillet, %d years from now,
                        in La Barre, France, Earth.
                        His given name, Jean-Luc, is of French origin and translates
                        to "John Luke".
                        </person>
                        
                        Please extract the relevant fields into a Person instance.
                        """.formatted(2305 - LocalDate.now().getYear())
        );
        System.out.println(response);
        assertThat(response).contains("Jean-Luc", "Picard", "record Person");
    }

}