package han.Chensing.CMath.tools;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import han.Chensing.CMath.V;

public class CPermissions {

    public static volatile boolean allDown=false;

    public static void checkPermissions(String[] permissions, AppCompatActivity appCompatActivity) {
        if (checkPermissionsOnly(permissions,appCompatActivity))
            ActivityCompat.requestPermissions(appCompatActivity, permissions, 1);
        else allDown=true;
    }

    public static boolean checkPermissionsOnly(String[] permissions, AppCompatActivity appCompatActivity){
        boolean needRequest = false;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(appCompatActivity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                needRequest = true;
                break;
            }
        }
        return needRequest;
    }

    public static void requestPermissionsResult(final AppCompatActivity appCompatActivity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        int succeed = 0;

        if (requestCode == 1) {
            boolean isNever=false;
            for (int i = 0; i != permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    succeed++;
                }else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(appCompatActivity, permissions[i])){
                        isNever=true;
                    }
                }
            }
            if (succeed != permissions.length) {
                AlertDialog.Builder ab = new AlertDialog.Builder(appCompatActivity);
                ab.setTitle("Emm...");
                ab.setMessage("You should agree the permissions to continue.");
                //Close and never ask
                if (isNever) {
                    ab.setNegativeButton("Go to Settings..", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", appCompatActivity.getPackageName(), null);
                            intent.setData(uri);
                            appCompatActivity.startActivityForResult(intent, 2);
                        }
                    });
                    ab.setCancelable(false);
                    ab.show();
                } else {//Only close
                    ab.setNegativeButton("Agree", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(appCompatActivity, V.permissions, 1);
                        }
                    });
                    ab.setCancelable(false);
                    ab.show();
                }
            }else {
                allDown=true;
            }
        }
    }

    public static void activityResult(AppCompatActivity appCompatActivity, int requestCode, String[] permissions){
        if (requestCode==2){
            checkPermissions(permissions,appCompatActivity);
        }
    }
}
