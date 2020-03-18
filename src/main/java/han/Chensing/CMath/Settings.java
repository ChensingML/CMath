package han.Chensing.CMath;

import android.content.SharedPreferences;

public class Settings {
    public static SharedPreferences publicSharedPreferences;

    public static final String FIRST_START="first-start";
    public static final String SETTINGS_CHECK_UPDATE_ON_START="c-u-o-s";

    public static boolean isFirstStart;
    public static boolean settings_checkUpdatesOnStart;
}
