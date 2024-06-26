package com.kousenit.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

import static com.kousenit.gemini.GeminiRecords.SafetySetting.*;

public class GeminiRecords {

    public record CachedContent(
            List<Content> contents,
            List<Tool> tools,
            @JsonProperty("createTime") String createTime,
            @JsonProperty("updateTime") String updateTime,
            @JsonProperty("usageMetadata") CachedUsageMetadata usageMetadata,
            @JsonProperty("expireTime") String expireTime,
            String ttl,
            String name,
            @JsonProperty("displayName") String displayName,
            String model,
            @JsonProperty("systemInstruction") Content systemInstruction,
            @JsonProperty("toolConfig") ToolConfig toolConfig
    ) {}

    public record Content(
            List<Part> parts,
            String role
    ) {}

    public record Part(
            String text,
            @JsonProperty("inlineData") Blob inlineData,
            @JsonProperty("functionCall") FunctionCall functionCall,
            @JsonProperty("functionResponse") FunctionResponse functionResponse,
            @JsonProperty("fileData") FileData fileData
    ) {}

    public record Blob(
            @JsonProperty("mimeType") String mimeType,
            String data
    ) {}

    public record FunctionCall(
            String name,
            Object args // This can be mapped to a more specific record if the structure is known
    ) {}

    public record FunctionResponse(
            String name,
            Object response // This can be mapped to a more specific record if the structure is known
    ) {}

    public record FileData(
            @JsonProperty("mimeType") String mimeType,
            @JsonProperty("fileUri") String fileUri
    ) {}

    public record Tool(
            @JsonProperty("functionDeclarations") List<FunctionDeclaration> functionDeclarations
    ) {}

    public record FunctionDeclaration(
            String name,
            String description,
            Schema parameters
    ) {}

    public record Schema(
            Type type,
            String format,
            String description,
            Boolean nullable,
            List<String> enumValues,
            Map<String, Schema> properties,
            List<String> required,
            Schema items
    ) {
        public enum Type {
            TYPE_UNSPECIFIED, STRING, NUMBER, INTEGER, BOOLEAN, ARRAY, OBJECT
        }
    }

    public record ToolConfig(
            @JsonProperty("functionCallingConfig") FunctionCallingConfig functionCallingConfig
    ) {}

    public record FunctionCallingConfig(
            Mode mode,
            @JsonProperty("allowedFunctionNames") List<String> allowedFunctionNames
    ) {
        public enum Mode {
            MODE_UNSPECIFIED, AUTO, ANY, NONE
        }
    }

    // UsageMetadata specifically for cached content
    public record CachedUsageMetadata(
            @JsonProperty("totalTokenCount") int totalTokenCount
    ) {}

    // New records for GenerateContentRequest and GenerateContentResponse
    public record GenerateContentRequest(
            List<Content> contents,
            List<Tool> tools,
            @JsonProperty("toolConfig") ToolConfig toolConfig,
            @JsonProperty("safetySetting") List<SafetySetting> safetySettings,
            @JsonProperty("systemInstruction") Content systemInstruction,
            @JsonProperty("generationConfig") GenerationConfig generationConfig,
            @JsonProperty("cachedContent") String cachedContent
    ) {}

    public record SafetySetting(
            SafetyCategory category,
            double threshold
    ) {
        public enum SafetyCategory {
            HARM_CATEGORY_HATE_SPEECH, HARM_CATEGORY_SEXUALLY_EXPLICIT,
            HARM_CATEGORY_DANGEROUS_CONTENT, HARM_CATEGORY_HARASSMENT
        }
    }

    public record GenerationConfig(
            @JsonProperty("stopSequences") List<String> stopSequences,
            @JsonProperty("responseMimeType") String responseMimeType,
            @JsonProperty("maxOutputTokens") int maxOutputTokens,
            double temperature,
            double topP,
            double topK
    ) {}

    public record GenerateContentResponse(
            List<Candidate> candidates,
            @JsonProperty("promptFeedback") PromptFeedback promptFeedback,
            @JsonProperty("usageMetadata") GenerationUsageMetadata usageMetadata
    ) {}

    public record Candidate(
            Content content,
            @JsonProperty("finishReason") FinishReason finishReason,
            @JsonProperty("safetyRatings") List<SafetyRating> safetyRatings
    ) {}

    public record SafetyRating(
            SafetyCategory category,
            double rating
    ) {}

    public record PromptFeedback(
            @JsonProperty("blockReason") BlockReason blockReason,
            @JsonProperty("safetyRatings") List<SafetyRating> safetyRatings
    ) {}

    public enum BlockReason {
        BLOCK_REASON_UNSPECIFIED, SAFETY, OTHER
    }

    public enum FinishReason {
        FINISH_REASON_UNSPECIFIED, LENGTH, STOP, MAX_TOKENS
    }

    // UsageMetadata specifically for content generation
    public record GenerationUsageMetadata(
            @JsonProperty("promptTokenCount") int promptTokenCount,
            @JsonProperty("cachedContentToken") int cachedContentTokenCount,
            @JsonProperty("candidatesTokenCount") int candidatesTokenCount,
            @JsonProperty("totalTokenCount") int totalTokenCount
    ) {}
}