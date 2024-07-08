package com.kousenit.gemini;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

import static com.kousenit.gemini.GeminiRecords.SafetySetting.SafetyCategory;

public class GeminiRecords {

    @SuppressWarnings("unused")
    public sealed interface ApiResult<T> {
        record Success<T>(T data) implements ApiResult<T> { }
        record Failure<T>(String error) implements ApiResult<T> { }
    }

    public record CachedContent(
            List<Content> contents,
            List<Tool> tools,
            @SerializedName("createTime") String createTime,
            @SerializedName("updateTime") String updateTime,
            @SerializedName("usageMetadata") CachedUsageMetadata usageMetadata,
            @SerializedName("expireTime") String expireTime,
            String ttl,
            String name,
            @SerializedName("displayName") String displayName,
            String model,
            @SerializedName("systemInstruction") Content systemInstruction,
            @SerializedName("toolConfig") ToolConfig toolConfig
    ) {}

    public record Content(
            List<Part> parts,
            String role
    ) {}

    public record Part(
            String text,
            @SerializedName("inlineData") Blob inlineData,
            @SerializedName("functionCall") FunctionCall functionCall,
            @SerializedName("functionResponse") FunctionResponse functionResponse,
            @SerializedName("fileData") FileData fileData
    ) {}

    public record Blob(
            @SerializedName("mimeType") String mimeType,
            String data
    ) {}

    public record FunctionCall(
            String name,
            Object args
    ) {}

    public record FunctionResponse(
            String name,
            Object response
    ) {}

    public record FileData(
            @SerializedName("mimeType") String mimeType,
            @SerializedName("fileUri") String fileUri
    ) {}

    public record Tool(
            @SerializedName("functionDeclarations") List<FunctionDeclaration> functionDeclarations
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
            @SerializedName("functionCallingConfig") FunctionCallingConfig functionCallingConfig
    ) {}

    public record FunctionCallingConfig(
            Mode mode,
            @SerializedName("allowedFunctionNames") List<String> allowedFunctionNames
    ) {
        public enum Mode {
            MODE_UNSPECIFIED, AUTO, ANY, NONE
        }
    }

    // UsageMetadata specifically for cached content
    public record CachedUsageMetadata(
            @SerializedName("totalTokenCount") int totalTokenCount
    ) {}

    // New records for GenerateContentRequest and GenerateContentResponse
    public record GenerateContentRequest(
            List<Content> contents,
            List<Tool> tools,
            @SerializedName("toolConfig") ToolConfig toolConfig,
            @SerializedName("safetySetting") List<SafetySetting> safetySettings,
            @SerializedName("systemInstruction") Content systemInstruction,
            @SerializedName("generationConfig") GenerationConfig generationConfig,
            @SerializedName("cachedContent") String cachedContent
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
            @SerializedName("stopSequences") List<String> stopSequences,
            @SerializedName("responseMimeType") String responseMimeType,
            @SerializedName("maxOutputTokens") int maxOutputTokens,
            double temperature,
            double topP,
            double topK
    ) {}

    public record GenerateContentResponse(
            List<Candidate> candidates,
            @SerializedName("promptFeedback") PromptFeedback promptFeedback,
            @SerializedName("usageMetadata") GenerationUsageMetadata usageMetadata
    ) {}

    public record Candidate(
            Content content,
            @SerializedName("finishReason") FinishReason finishReason,
            @SerializedName("safetyRatings") List<SafetyRating> safetyRatings
    ) {}

    public record SafetyRating(
            SafetyCategory category,
            double rating
    ) {}

    public record PromptFeedback(
            @SerializedName("blockReason") BlockReason blockReason,
            @SerializedName("safetyRatings") List<SafetyRating> safetyRatings
    ) {}

    public enum BlockReason {
        BLOCK_REASON_UNSPECIFIED, SAFETY, OTHER
    }

    public enum FinishReason {
        FINISH_REASON_UNSPECIFIED, LENGTH, STOP, MAX_TOKENS
    }

    // UsageMetadata specifically for content generation
    public record GenerationUsageMetadata(
            @SerializedName("promptTokenCount") int promptTokenCount,
            @SerializedName("cachedContentTokenCount") int cachedContentTokenCount,
            @SerializedName("candidatesTokenCount") int candidatesTokenCount,
            @SerializedName("totalTokenCount") int totalTokenCount
    ) {}
}