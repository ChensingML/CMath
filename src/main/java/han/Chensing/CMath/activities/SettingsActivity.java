package han.Chensing.CMath.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import han.Chensing.CMath.R;
import han.Chensing.CMath.V;
import han.Chensing.CMath.tools.Download;
import han.Chensing.CMath.tools.OkDown;
import han.Chensing.CMath.widget.Ea;

public class SettingsActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;

    private static ArrayList<Button> buttons;
    private static boolean isRunning=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        sharedPreferences=getApplicationContext().getSharedPreferences("settings",MODE_PRIVATE);
        //Get settings
        //Coming soon

        if (buttons==null) {
            buttons = new ArrayList<>();
        }else {
            buttons.clear();
        }

        Button
                clear_cache=findViewById(R.id.settings_clear),
                update=findViewById(R.id.settings_update);
        ProgressBar progressBar=findViewById(R.id.settings_progress);

        clear_cache.setOnClickListener(v -> new AsyClean(this).execute());
        update.setOnClickListener(v -> new AsyUpdate(this).execute());

        buttons.add(clear_cache);
        buttons.add(update);
        doButtons(buttons,progressBar,isRunning);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK&&isRunning){
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * @param doingOrDid true-doing,false-did
     */
    private static void doButtons(ArrayList<Button> buttons,ProgressBar bar,boolean doingOrDid){
        isRunning=doingOrDid;
        if (doingOrDid) bar.setVisibility(View.VISIBLE);
        else  bar.setVisibility(View.GONE);
        for (Button button:buttons){
            button.setEnabled(!doingOrDid);
        }
    }

    static class AsyClean extends AsyncTask<Void,Void,Void>{

        private WeakReference<SettingsActivity> weakReference;

        AsyClean(SettingsActivity context){
            this.weakReference=new WeakReference<>(context);
        }

        private SettingsActivity getActivity(){
            SettingsActivity settingsActivity = weakReference.get();
            if (settingsActivity==null||settingsActivity.isFinishing()) throw new NullPointerException();
            return settingsActivity;
        }

        long size;

        private long getSize(File file){
            long size=0;
            File[] files = file.listFiles();
            if (files==null) return 0;
            for (int i=0;i!=files.length;i++){
                File thisFile=files[i];
                if (thisFile.isDirectory()){
                    getSize(thisFile);
                    continue;
                }
                size+=thisFile.length();
            }
            return size;
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        static void delete(File file){
            File[] files = file.listFiles();
            if (files==null) return;
            for (int i=0;i!=files.length;i++){
                File thisFile=files[i];
                if (thisFile.isDirectory()){
                    delete(thisFile);
                }
                thisFile.delete();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            size=getSize(getActivity().getCacheDir());
            ProgressBar progressBar=getActivity().findViewById(R.id.settings_progress);
            doButtons(buttons,progressBar,true);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Snackbar.make(
                    getActivity().findViewById(R.id.settings_sv),
                    (size/1024)+"KB cache cleaned",
                    Snackbar.LENGTH_SHORT)
                    .show();
            ProgressBar progressBar=getActivity().findViewById(R.id.settings_progress);
            doButtons(buttons,progressBar,false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            delete(getActivity().getCacheDir());
            return null;
        }
    }

    static class AsyUpdate extends AsyncTask<Void,Integer,Integer>{

        /**
         * 0---No new
         * 1---Has new
         * 2---User don't need new
         * -1--Error
         */

        private WeakReference<SettingsActivity> weakReference;

        AsyUpdate(SettingsActivity context){
            this.weakReference=new WeakReference<>(context);
        }

        private SettingsActivity getActivity(){
            SettingsActivity settingsActivity = weakReference.get();
            if (settingsActivity==null||settingsActivity.isFinishing()) throw new NullPointerException();
            return settingsActivity;
        }

        Exception error;
        Ea downloadEa;
        View[] views=new View[1];
        boolean decideToDownload;
        final Object lock=new Object();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressBar progressBar=getActivity().findViewById(R.id.settings_progress);
            doButtons(buttons,progressBar,true);
        }

        @Override
        protected void onPostExecute(Integer ex) {
            super.onPostExecute(ex);
            ProgressBar progressBar = getActivity().findViewById(R.id.settings_progress);
            ScrollView scrollView = getActivity().findViewById(R.id.settings_sv);
            doButtons(buttons, progressBar, false);
            if (ex == 0) {//Newest
                Snackbar.make(scrollView, "Congratulations! Your version is newest", Snackbar.LENGTH_SHORT)
                        .show();
            } else if (error != null) {//Error
                Snackbar.make(scrollView, "Oops! Failed to check update!", Snackbar.LENGTH_SHORT)
                        .show();
            }

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int value=values[0];
            if (value==-1){
                Ea ea=new Ea(getActivity());
                ea
                        .setTitle("Find a new version")
                        .setMessage("Would you want ot download it?")
                        .getBuilder().setPositiveButton("Yes",(dialog, which) -> {
                            decideToDownload=true;
                            synchronized (lock){
                                lock.notify();
                            }
                        })
                        .setNeutralButton("No",(dialog, which) -> {
                            decideToDownload=false;
                            synchronized (lock){
                                lock.notify();
                            }
                        })
                        .setCancelable(false)
                        .show();
            }else if (value==-2) {
                downloadEa = new Ea(getActivity());
                @SuppressLint("InflateParams") View view = LayoutInflater.from(getActivity()).inflate(R.layout.prog, null);
                views[0]=view;
                downloadEa.setTitle("Downloading");
                downloadEa.getBuilder().setView(view);
                downloadEa.show();
            }else if (value==-3){
                downloadEa.dismiss();
            }else{
                ProgressBar progress=views[0].findViewById(R.id.the_prog);
                progress.setProgress(value);
                switch (value){
                    case 101:{
                        progress.setIndeterminate(true);
                        downloadEa.getDialog().setTitle("Writing");
                        break;
                    }
                    case 102:{
                        downloadEa.getDialog().setTitle("Installing");
                        break;
                    }
                    case 103:{
                        progress.setIndeterminate(true);
                    }
                }
            }
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                long nowVersion;
                PackageManager packageManager=getActivity().getPackageManager();
                PackageInfo packageInfo=packageManager.getPackageInfo(getActivity().getPackageName(),0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    nowVersion = packageInfo.getLongVersionCode();
                } else {
                    nowVersion = packageInfo.versionCode;
                }
                long version = Download.downloadVersion();
                if (version <= nowVersion){
                    return 0;
                }
                publishProgress(-1);
                //Has new Version
                synchronized (lock){
                    lock.wait();
                }
                if (decideToDownload){
                    publishProgress(-2);
                    OkDown.get().download(V.hostHead + "CMath.apk", new OkDown.DownloadLister() {
                        @Override
                        public void downloadProgress(int progress) {
                            if(progress==-1){
                                publishProgress(103);
                                return;
                            }
                            publishProgress(progress);
                        }

                        @Override
                        public void failed(Exception ex) {
                            error=ex;
                            synchronized (lock){
                                lock.notify();
                            }
                        }

                        @Override
                        public void done(byte[] bs) {
                            try{
                                publishProgress(101);
                                File apk = new File(getActivity().getCacheDir(),"newVer.apk");
                                OutputStream outputStream=new FileOutputStream(apk);
                                outputStream.write(bs);
                                outputStream.flush();
                                outputStream.close();
                                publishProgress(102);
                                Intent intent=new Intent(Intent.ACTION_VIEW);
                                Uri data;
                                data= FileProvider.getUriForFile(getActivity(),"han.Chensing.CMath.FileProvider",apk);
                                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                intent.setDataAndType(data,"application/vnd.android.package-archive");
                                getActivity().startActivity(intent);
                            }catch (Exception e){
                                error=e;
                            }
                            synchronized (lock){
                                lock.notify();
                            }
                        }
                    });
                    synchronized (lock){
                        lock.wait();
                    }
                    publishProgress(-3);
                    return 1;
                }else {
                    return 2;
                }
            } catch (Exception e) {
                e.printStackTrace();
                error=e;
                publishProgress(-3);
                return -1;
            }
        }
    }
}
