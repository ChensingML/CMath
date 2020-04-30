package han.Chensing.CMath;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import han.Chensing.CMath.tools.CPermissions;

public class Welcome extends AppCompatActivity {

    Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.welcome);
        //Only for develop and test
        handler = new Handler(getMainLooper());
        new Init().start();
    }

    void requestPermissions(){
        CPermissions.checkPermissions(V.permissions,this);
    }

    boolean needGetPermissions(){
        return CPermissions.checkPermissionsOnly(V.permissions,this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        CPermissions.requestPermissionsResult(this,requestCode,permissions,grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CPermissions.activityResult(this,requestCode,V.permissions);
    }

    private class Init extends Thread {

        private TextView zt;
        private LinearLayout load;
        private LinearLayout perm;

        volatile int runZt=0;
        //Ask permissions
        //0     Wait
        //1     Start
        //2     Exit
        //Connects
        //0     Wait
        //1     Jump
        //2     Retry

        @Override
        public void run() {
            super.run();
            zt = findViewById(R.id.welZt);
            load = findViewById(R.id.welLoadL);
            perm = findViewById(R.id.welPermissL);
            Button perB = findViewById(R.id.welPermiss);
            Button exit = findViewById(R.id.welExit);
            View.OnClickListener onClickListener = view -> {
                switch (view.getId()) {
                    case R.id.welPermiss:
                        runZt = 1;
                        break;
                    case R.id.welExit:
                        runZt = 2;
                        break;
                }
            };
            perB.setOnClickListener(onClickListener);
            exit.setOnClickListener(onClickListener);
            while (true) {
                try {
                    setText(R.string.welHello);
                    Thread.sleep(1000);
                    if (needGetPermissions())
                        perShow();
                    else
                        runZt=1;
                    while (runZt == 0) ;
                    if (runZt == 2) {
                        finish();
                        return;
                    }
                    runZt=0;
                    requestPermissions();
                    perHide();
                    setText(R.string.welWait);
                    while (!CPermissions.allDown) ;
                    setText(R.string.welPrep);

                    Settings.publicSharedPreferences=getApplicationContext().getSharedPreferences("settings",MODE_PRIVATE);
                    Settings.isFirstStart=Settings.publicSharedPreferences.getBoolean(Settings.FIRST_START,true);
                    Settings.settings_checkUpdatesOnStart=Settings.publicSharedPreferences.getBoolean(Settings.SETTINGS_CHECK_UPDATE_ON_START,false);
                    if (Settings.isFirstStart) Settings.publicSharedPreferences.edit().putBoolean(Settings.SETTINGS_CHECK_UPDATE_ON_START,true).apply();

                    setText(R.string.welLoadFiles);
                    V.countRules = new ArrayList<>();
                    if (Settings.settings_checkUpdatesOnStart){
                        setText(R.string.welCheckUpdate);
                    }
                    MainActivity.firstLoad(Welcome.this,null,0);
                    setText(R.string.welDone);
                    Thread.sleep(500);
                    startActivity(new Intent(Welcome.this,MainActivity.class));
                    SharedPreferences.Editor editor = Settings.publicSharedPreferences.edit();
                    editor.putBoolean(Settings.FIRST_START,false);
                    editor.apply();
                    finish();
                    break;
                    //Finish here
                    //At 2019/12/29 21:14 By Chen.ZH
                } catch (Exception e) {
                    e.printStackTrace();
                    Snackbar sb=Snackbar.make(load,"Here are some problems with here, retry?",Snackbar.LENGTH_INDEFINITE);
                    sb.setAction("Retry", v -> runZt=1);
                    sb.show();
                    while (runZt==0);
                }
            }
        }

        private void setText(final int res){
            handler.post(() -> zt.setText(res));
        }

        private void perShow(){
            handler.post(() -> {
                perm.setVisibility(View.VISIBLE);
                load.setVisibility(View.GONE);
            });
        }

        private void perHide(){
            handler.post(() -> {
                perm.setVisibility(View.GONE);
                load.setVisibility(View.VISIBLE);
            });
        }
    }
}

