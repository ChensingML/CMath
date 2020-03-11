package han.Chensing.CMath;

import android.Manifest;
import android.annotation.SuppressLint;

import java.util.ArrayList;

import han.Chensing.CMath.adapters.MathAdapter;

public final class V {

    //Permissions
    public static String[] permissions=new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //Users
    @SuppressLint("StaticFieldLeak")
    public static MathAdapter mathAdapter;

    //Functions
    public static final String hostHead="https://gitee.com/ChensingML/CMath-Resources/raw/master/";
    public static ArrayList<CountRule> countRules;
}
