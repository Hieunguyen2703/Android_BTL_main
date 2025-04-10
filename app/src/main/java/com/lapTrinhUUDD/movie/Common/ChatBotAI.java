package com.lapTrinhUUDD.movie.Common;

import android.annotation.SuppressLint;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;

public class ChatBotAI {

    @SuppressLint("SecretInSource")
    private static final String API_KEY = "AIzaSyCWgtr9_5H89C5YtT9qOn8yuD4P7ozEnfU";

    public static final GenerativeModel streamModel = new GenerativeModel(
            "gemini-1.5-flash",
            API_KEY
    );

    public static final GenerativeModelFutures futureModel = GenerativeModelFutures.from(streamModel);
}


