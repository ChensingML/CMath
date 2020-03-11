package han.Chensing.CMath.widget;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

public class Ea {

    private AlertDialog alertDialog;
    private AlertDialog.Builder builder;

    public Ea(Context context){
        builder=new AlertDialog.Builder(context);
    }

    public Ea setTitle(CharSequence charSequence){
        builder.setTitle(charSequence);
        return this;
    }

    public Ea setMessage(CharSequence charSequence){
        builder.setMessage(charSequence);
        return this;
    }

    public AlertDialog.Builder getBuilder() {
        return builder;
    }

    public AlertDialog show(){
        return alertDialog=builder.show();
    }

    public void dismiss(){
        alertDialog.dismiss();
    }

    public AlertDialog getDialog(){
        return alertDialog;
    }
}
