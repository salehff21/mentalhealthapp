package com.example.mentalhealthapp;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LocaleUtil {

    /**
     * Update the given Context with the desired locale.
     *
     * @param context  Original context from the system.
     * @param langCode Language code such as "en" or "ar".
     * @return A new Context configured with the requested locale.
     */
    public static Context updateLocale(Context context, String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = res.getConfiguration();
        config.setLocale(locale);

        // Create a new context with the updated configuration
        return context.createConfigurationContext(config);
    }
}
