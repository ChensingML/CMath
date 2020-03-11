package han.Chensing.CMath.activities;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import han.Chensing.CMath.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        boolean isLess=getIntent().getBooleanExtra("is_less",false);
        if (!isLess){
            setContentView(R.layout.about);
        }else{
            setContentView(R.layout.about_less);
        }
    }

    /*private Bitmap smallerBitmap(Bitmap bitmap,int targetKb){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        int options = 100;
        while ( byteArrayOutputStream.toByteArray().length / 1024>targetKb) {
            byteArrayOutputStream.reset();
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, byteArrayOutputStream);
            options -= 10;
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        return BitmapFactory.decodeStream(isBm, null, null);
    }*/
}
