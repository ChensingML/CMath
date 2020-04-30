package han.Chensing.CMath.activities;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Objects;

import dalvik.system.DexClassLoader;
import han.Chensing.CMath.CountRule;
import han.Chensing.CMath.R;
import han.Chensing.CMath.V;
import han.Chensing.CMath.widget.Ea;

/**
 * ImportantActivity And CardLoader
 *
 * CardLoader version Indev 3
 * 2020/3/22
 */


public class ImportantActivity extends AppCompatActivity {

    static ArrayList<View> forS;
    static Class<?> clazz;
    TextView zt_text;
    CountRule countRule;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.important);


        ProgressBar progressBar=findViewById(R.id.impo_progress);
        Button button=findViewById(R.id.impo_count);
        button.setOnClickListener(v -> {
            //Preparing data
            String[] strings=new String[forS.size()];
            for (int i=0;i!=strings.length;i++){
                TextView textView=forS.get(i).findViewById(R.id.codes_editt);
                strings[i]=textView.getText().toString();
            }
            countRule.setRealParameters(strings);
            //Go
            new AsyCount(this).execute(countRule);
        });
        zt_text=findViewById(R.id.impo_zt);

        int opens=getIntent().getIntExtra("opens",-1);

        if (opens==-1){
            progressBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.ic_action_error));
            zt_text.setText(R.string.importantErr);
        }else {
            countRule = V.countRules.get(opens);
            new AsyLoad(this).execute(countRule);
        }
    }

    static class AsyLoad extends AsyncTask<CountRule,String, AsyLoad.Inf>{

        Exception error;

        private WeakReference<ImportantActivity> weakReference;

        AsyLoad(ImportantActivity context){
            this.weakReference=new WeakReference<>(context);
        }

        private ImportantActivity getActivity(){
            ImportantActivity importantActivity = weakReference.get();
            if (importantActivity==null||importantActivity.isFinishing()) throw new NullPointerException();
            return importantActivity;
        }

        @Override
        protected void onPostExecute(Inf inf) {
            super.onPostExecute(inf);
            if (inf==null){
                TextView textView=getActivity().findViewById(R.id.impo_zt);
                textView.setText(R.string.importantErr);
                Button button=getActivity().findViewById(R.id.impo_exbtn);
                Button detail=getActivity().findViewById(R.id.impo_detail);
                ProgressBar progressBar=getActivity().findViewById(R.id.impo_progress);
                button.setVisibility(View.VISIBLE);
                detail.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter, true);
                error.printStackTrace(printWriter);
                printWriter.flush();
                stringWriter.flush();
                String det=stringWriter.toString();
                button.setOnClickListener(v -> getActivity().finish());
                detail.setOnClickListener(v -> {
                    Ea ea=new Ea(getActivity());
                    ea.setTitle("Error Detail");
                    ea.setMessage(det);

                    ea.getBuilder().setPositiveButton("Copy to clipboard",(dialog, which) -> {
                        ClipboardManager clipboardManager=(ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        if (clipboardManager != null) {
                            clipboardManager.setPrimaryClip(ClipData.newPlainText(null, det));
                            Toast.makeText(getActivity(),"Copied",Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(getActivity(),"Copy failed",Toast.LENGTH_SHORT).show();
                        }
                    }).setNeutralButton("Close",(dialog, which) -> ea.dismiss());
                    ea.show();
                });
            }else {
                TextView
                        title=getActivity().findViewById(R.id.impo_title),
                        introduction=getActivity().findViewById(R.id.impo_introduction);
                ImageView imageView=getActivity().findViewById(R.id.impo_imageView);
                LinearLayout codeLin=getActivity().findViewById(R.id.impo_codes);
                title.setText(inf.title);
                introduction.setText(inf.introduction);

                if (inf.bitmap == null)imageView.setImageResource(R.drawable.ic_action_noimage);
                else imageView.setImageBitmap(inf.bitmap);

                forS=new ArrayList<>();

                for (String fom : inf.formalParameters) {
                    @SuppressLint("InflateParams") View view= LayoutInflater.from(getActivity()).inflate(R.layout.codes_item,null);
                    TextView name=view.findViewById(R.id.codes_textv);
                    name.setText(fom);
                    codeLin.addView(view);
                    forS.add(view.findViewById(R.id.codes_editt));
                }

                RelativeLayout relativeLayout=getActivity().findViewById(R.id.impo_child_Relative);
                LinearLayout linearLayout=getActivity().findViewById(R.id.impo_child_Line);
                linearLayout.setVisibility(View.GONE);
                relativeLayout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            TextView textView=getActivity().findViewById(R.id.impo_zt);
            textView.setText(values[0]);
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        protected Inf doInBackground(CountRule... countRules) {
            CountRule countRule = countRules[0];
            Inf inf= new Inf();
            /*
             * 0    title
             * 1    introduction
             * 2    bitmap
             * 3    formal parameters
             */
            try {
                publishProgress("Cleaning cache");
                SettingsActivity.AsyClean.delete(getActivity().getCacheDir());
                publishProgress("Load information");
                inf.title = countRule.getName();
                inf.introduction = countRule.getIntroduction();
                inf.formalParameters = countRule.getFormalParameters();
                byte[] bitmap = countRule.getBitmap();
                publishProgress("Decode to Bitmap");
                try{inf.bitmap=BitmapFactory.decodeByteArray(bitmap,0,bitmap.length);}
                catch (Exception ignore){inf.bitmap=null;}
                publishProgress("Load method");
                File file=new File(getActivity().getCacheDir().getPath()+"/classes.apk");
                if (!file.exists()){
                    Objects.requireNonNull(file.getParentFile()).mkdirs();
                    file.createNewFile();
                }
                OutputStream outputStream=new FileOutputStream(file);
                outputStream.write(countRule.getOutput());
                outputStream.close();
                DexClassLoader dexClassLoader=new DexClassLoader(
                        file.getPath(),
                        file.getParent(),
                        null,
                        getActivity().getApplicationContext().getClassLoader());
                clazz=dexClassLoader.loadClass(String.format("%s.%s",countRule.getPackageName(),"Plug"));
            }catch (Exception e){
                error=e;
                inf=null;
            }
            return inf;
        }

        private static class Inf{
            String title;
            String introduction;
            Bitmap bitmap;
            String[] formalParameters;

        }
    }

    static class AsyCount extends AsyncTask<CountRule,Void,String>{

        CountRule countRule;

        private WeakReference<ImportantActivity> weakReference;

        AsyCount(ImportantActivity context){
            this.weakReference=new WeakReference<>(context);
        }

        private ImportantActivity getActivity(){
            ImportantActivity importantActivity = weakReference.get();
            if (importantActivity==null||importantActivity.isFinishing()) throw new NullPointerException();
            return importantActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Button button=getActivity().findViewById(R.id.impo_count);
            button.setEnabled(false);
            button.setText(R.string.importantCounting);
        }

        @Override
        protected void onPostExecute(String aString) {
            super.onPostExecute(aString);
            Button button=getActivity().findViewById(R.id.impo_count);
            button.setEnabled(true);
            if (aString==null){
                button.setText(R.string.importantCount);
                Snackbar snackBar = Snackbar.make(
                        getActivity().findViewById(R.id.impo_rel),
                        "Oops! Count error!",
                        Snackbar.LENGTH_SHORT);
                snackBar.setAction("Close",v -> snackBar.dismiss());
                snackBar.show();
                return;
            }
            switch (countRule.getShowPlace()){
                case IN_BUTTON:{
                    button.setText(aString);
                    break;
                }
                case IN_TEXT:{
                    TextView textView=getActivity().findViewById(R.id.impo_optext);
                    textView.setText(aString);
                    button.setText(R.string.importantCount);
                    break;
                }
                case IN_SCREEN:{
                    Ea ea=new Ea(getActivity());
                    ea
                            .setTitle("Result")
                            .setMessage("Here is count result:\n"+aString);
                    ea.getBuilder().setPositiveButton("OK",(dialog, which) -> dialog.dismiss());
                    ea.show();
                    button.setText(R.string.importantCount);
                    break;
                }
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected String doInBackground(CountRule... countRules) {
            try {
                this.countRule=countRules[0];
                Method method=clazz.getMethod("run",String[].class);
                Object object=clazz.newInstance();
                Object invoke = method.invoke(object, (Object) countRule.getRealParameters());
                return String.valueOf(invoke);
            }catch (Exception ignore){
                return null;
            }
        }
    }
}
