package com.kousenit.openai.json;

import com.google.gson.annotations.SerializedName;

import java.util.Random;

public enum Voice {
    @SerializedName("alloy") ALLOY,
    @SerializedName("echo") ECHO,
    @SerializedName("fable") FABLE,
    @SerializedName("onyx") ONYX,
    @SerializedName("nova") NOVA,
    @SerializedName("shimmer") SHIMMER;

    private static final Random RANDOM = new Random();

    public static Voice getRandomVoice()  {
        Voice[] values = Voice.values();
        return values[RANDOM.nextInt(values.length)];
    }

}
