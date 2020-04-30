package han.Chensing.CMath;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import han.Chensing.CMath.activities.AboutActivity;
import han.Chensing.CMath.activities.DownloadActivity;
import han.Chensing.CMath.activities.HelpActivity;
import han.Chensing.CMath.activities.ImportantActivity;
import han.Chensing.CMath.activities.LocalFileActivity;
import han.Chensing.CMath.activities.SettingsActivity;
import han.Chensing.CMath.adapters.EaAdapter;
import han.Chensing.CMath.adapters.MathAdapter;
import han.Chensing.CMath.tools.Download;
import han.Chensing.CMath.tools.OkDown;
import han.Chensing.CMath.widget.Ea;

public class MainActivity extends AppCompatActivity {

    LinearLayout noFile;
    ListView listView;
    Ea builder;

    boolean isDrawerOpen = false;

    public static Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        @SuppressLint("InflateParams") View actionView = LayoutInflater.from(this).inflate(R.layout.caction, null);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(actionView);
        actionBar.show();

        setContentView(R.layout.activity_main);

        ImageButton imageButton = actionView.findViewById(R.id.action_button);
        DrawerLayout drawerLayout = findViewById(R.id.mainDr);
        ListView bar_list = findViewById(R.id.bar_list);
        FloatingActionButton floatingActionButton = findViewById(R.id.mainFloatingA);

        listView = findViewById(R.id.mainList);
        listView.setAdapter(V.mathAdapter);
        noFile = findViewById(R.id.mainNofileL);

        floatingActionButton.setEnabled(true);

        bar_list.setAdapter(new EaAdapter(this,
                new EaAdapter.EaData[]{
                        new EaAdapter.EaData(R.drawable.ic_check_all,R.string.bar_check_all),
                        new EaAdapter.EaData(R.drawable.ic_add, R.string.bar_add),
                        new EaAdapter.EaData(R.drawable.ic_help, R.string.bar_help),
                        new EaAdapter.EaData(R.drawable.ic_settings,R.string.bar_settings),
                        new EaAdapter.EaData(R.drawable.ic_about, R.string.bar_about)
                }
        ));

        bar_list.setOnItemLongClickListener((parent, view, position, id) -> {
            closeDrawer(imageButton, drawerLayout);
            if (position==4){
                Intent intent=new Intent(this,AboutActivity.class);
                intent.putExtra("is_less",true);
                startActivity(intent);
            }
            return false;
        });

        bar_list.setOnItemClickListener((parent, view, position, id) -> {
            closeDrawer(imageButton, drawerLayout);
            switch (position) {
                case 0:{//Check
                    new AsyCheckAll(this).execute();
                    break;
                }
                case 1: {//Add
                    add();
                    break;
                }
                case 2: {//Help
                    Intent intent=new Intent(this, HelpActivity.class);
                    startActivity(intent);
                    break;
                }
                case 3:{//Settings
                    Intent intent=new Intent(this, SettingsActivity.class);
                    startActivity(intent);
                    break;
                }
                case 4: {//About
                    Intent intent=new Intent(this, AboutActivity.class);
                    startActivity(intent);
                }
            }
        });

        handler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0x01) {
                    if (V.countRules.size() == 0) {
                        noFile.setVisibility(View.VISIBLE);
                    } else {
                        noFile.setVisibility(View.GONE);
                    }
                    listView.setAdapter(V.mathAdapter);
                } else if (msg.what == 0x02) {
                    runtimeLoad(MainActivity.this,null,2);
                }
            }
        };
        handler.sendEmptyMessage(0x01);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(MainActivity.this, ImportantActivity.class);
            intent.putExtra("opens", position);
            startActivity(intent);
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Ea ea = new Ea(this);
            ea.setTitle("Edit");
            ea.getBuilder().setSingleChoiceItems(
                    new EaAdapter(this,
                            new EaAdapter.EaData[]{
                                    new EaAdapter.EaData(R.drawable.ic_check, R.string.list_check),
                                    new EaAdapter.EaData(R.drawable.ic_delete, R.string.list_delete)
                            }
                    ),
                    -1,
                    (dialog, which) -> {
                        dialog.dismiss();
                        switch (which) {
                            case 1: {
                                Ea delEa = new Ea(this);
                                delEa
                                        .setTitle("Are you sure?!")
                                        .setMessage("Do you know? You are deleting a count card!\n>=\nAre you sure to delete it?");
                                delEa.getBuilder().setPositiveButton("Delete!",
                                        (dialog1, which1) -> {
                                            dialog1.dismiss();
                                            new AsyDel(this).execute(position);
                                        }
                                );
                                delEa.getBuilder().setNeutralButton("Let me think",
                                        (dialog1, which1) -> dialog1.dismiss());
                                delEa.show();
                                break;
                            }
                            case 0: {
                                new AsyCheck(this).execute(position);
                                break;
                            }
                        }
                    }
            );
            ea.show();
            return true;
        });

        View.OnClickListener onClickListener = v -> add();
        floatingActionButton.setOnClickListener(onClickListener);
        builder = new Ea(this);
        builder.setTitle(getResString(R.string.menu_1));
        builder.getBuilder().setSingleChoiceItems(
                new EaAdapter(this,
                        new EaAdapter.EaData[]{
                                new EaAdapter.EaData(R.drawable.ic_store, R.string.list_store),
                                new EaAdapter.EaData(R.drawable.ic_local, R.string.list_local)
                        }),
                -1,
                (dialog, which) -> {
                    if (which == 0) {
                        addFromServer();
                    } else {
                        addFromFile();
                    }
                    dialog.dismiss();
                });
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                openDrawer(imageButton, drawerLayout);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                closeDrawer(imageButton, drawerLayout);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        imageButton.setOnClickListener(v -> {
            if (isDrawerOpen) {//If open, close drawer, the menu icon
                closeDrawer(imageButton, drawerLayout);
            } else {//If close, open drawer, the close icon
                openDrawer(imageButton, drawerLayout);
            }
        });
    }

    public void openDrawer(ImageButton button, DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(GravityCompat.END);
        button.setImageResource(R.drawable.ic_close);
        isDrawerOpen = true;
    }

    public void closeDrawer(ImageButton button, DrawerLayout drawerLayout) {
        drawerLayout.closeDrawer(GravityCompat.END);
        button.setImageResource(R.drawable.ic_menu);
        isDrawerOpen = false;
    }

    public String getResString(int res_id) {
        return getResources().getString(res_id);
    }

    public static void runtimeLoad(AppCompatActivity appCompatActivity, ArrayList<CountRule> countRulesFromOther,int needCheckUpdate) {
        firstLoad(appCompatActivity, countRulesFromOther,needCheckUpdate);
        MainActivity.handler.sendEmptyMessage(0x01);
    }

    /**
     * @param needCheckUpdate 1-Force need; Other&0-Follow Settings; 2-Force close;
     */
    public static void firstLoad(AppCompatActivity appCompatActivity, ArrayList<CountRule> countRulesFromOther,int needCheckUpdate) {
        boolean needUpdate;
        switch (needCheckUpdate){
            default:
            case 0:{
                needUpdate=Settings.settings_checkUpdatesOnStart;
                break;
            }
            case 1:{
                needUpdate=true;
                break;
            }
            case 2:{
                needUpdate=false;
                break;
            }
        }
        ArrayList<MathAdapter.MathAdapterData> dataList = loadList(appCompatActivity, countRulesFromOther,needUpdate);
        V.mathAdapter = new MathAdapter(dataList, appCompatActivity);
    }

    public static boolean hasUpdate(ArrayList<String[]> downloadData,CountRule countRule){
        if (downloadData==null) return false;
        ArrayList<Integer> jOC=new ArrayList<>();
        ArrayList<Float> ver=new ArrayList<>();
        for (String[] s:downloadData){
            jOC.add(Integer.parseInt(s[3]));
            ver.add(Float.parseFloat(s[4]));
        }
        for (int i=0;i!=jOC.size();i++){
            if (jOC.get(i)==countRule.getJustOneCode()){//Matched
                if (ver.get(i)>countRule.getVersion()){//Has update
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public static ArrayList<MathAdapter.MathAdapterData> loadList(AppCompatActivity appCompatActivity, ArrayList<CountRule> countRulesFromOther, boolean isNeedCheckUpdate) {
        ArrayList<MathAdapter.MathAdapterData> dataList = new ArrayList<>();
        ArrayList<CountRule> rules = new ArrayList<>();
        if (countRulesFromOther != null) {
            for (CountRule countRule : countRulesFromOther) {
                MathAdapter.MathAdapterData data = new MathAdapter.MathAdapterData(
                        countRule.getName(),
                        countRule.getMessage(),
                        countRule.getEditor(),
                        true,
                        false
                );
                dataList.add(data);
                rules.add(countRule);
            }
        }
        V.countRules.clear();

        V.countRules.addAll(rules);
        File dataFile = appCompatActivity.getFilesDir();
        if (!dataFile.exists()) while (!dataFile.mkdirs()) ;
        File[] files = dataFile.listFiles();
        if (files != null) {
            ArrayList<String[]> downloadList=null;
            if (isNeedCheckUpdate) {
                try {
                    downloadList = Download.downloadList();
                } catch (IOException ignore) {
                }
            }
            for (File file : files) {
                try {
                    String fileName = file.getName();
                    String la = fileName.substring(fileName.lastIndexOf("."));
                    if (la.equals(".cr")) {
                        CountRule countRule = new CountRule(file.getAbsolutePath());
                        V.countRules.add(countRule);
                        MathAdapter.MathAdapterData data = new MathAdapter.MathAdapterData(
                                countRule.getName(),
                                countRule.getMessage(),
                                countRule.getEditor(),
                                true,
                                hasUpdate(downloadList,countRule)
                        );
                        dataList.add(data);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return dataList;
    }

    private void add() {
        builder.show();
    }

    private void addFromServer() {
        /*
         * Get data and list from gitee
         */
        startActivity(new Intent(MainActivity.this, DownloadActivity.class));
    }

    private void addFromFile() {
        startActivity(new Intent(this, LocalFileActivity.class));
    }

    public void addButton_onclick(View view) {
        add();
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    static class AsyDel extends AsyncTask<Integer, Void, Boolean> {

        private Ea delEa;
        private WeakReference<MainActivity> weakReference;

        AsyDel(MainActivity context) {
            this.weakReference = new WeakReference<>(context);
        }

        private MainActivity getActivity() {
            MainActivity mainActivity = weakReference.get();
            if (mainActivity == null || mainActivity.isFinishing())
                throw new NullPointerException();
            return mainActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            delEa = new Ea(getActivity());
            delEa.setTitle("I will do it at once...");
            delEa.getBuilder()
                    .setView(new ProgressBar(getActivity()))
                    .setCancelable(false);
            delEa.show();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            String message;
            delEa.dismiss();
            handler.sendEmptyMessage(0x02);
            View view = getActivity().findViewById(R.id.mainCoo);
            if (!aBoolean) message = "There are some problem on deleting, but I did me best...";
            else message = "Deleted";
            Snackbar.make(
                    view,
                    message,
                    Snackbar.LENGTH_SHORT
            ).show();
        }

        @Override
        protected Boolean doInBackground(Integer... integers) {
            int position = integers[0];
            CountRule countRule = V.countRules.get(position);
            String code = String.valueOf(countRule.getJustOneCode());
            String[] files = getActivity().getFilesDir().list();
            if (files == null) {
                return false;
            }
            for (String string : files) {
                if (string.equals(code + ".cr")) {
                    String stringBuilder = getActivity().getFilesDir().getPath() +
                            "/" +
                            string;
                    File file = new File(stringBuilder);
                    file.delete();
                    V.countRules.remove(position);
                    break;
                }
            }
            return true;
        }
    }

    static class AsyCheck extends AsyncTask<Integer, Integer, Integer> {

        private Ea checkEa;
        private ProgressBar[] bars=new ProgressBar[1];
        private WeakReference<MainActivity> weakReference;

        AsyCheck(MainActivity context) {
            this.weakReference = new WeakReference<>(context);
        }

        private MainActivity getActivity() {
            MainActivity mainActivity = weakReference.get();
            if (mainActivity == null || mainActivity.isFinishing())
                throw new NullPointerException();
            return mainActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            checkEa = new Ea(getActivity());
            checkEa
                    .setTitle("Checking update");
            @SuppressLint("InflateParams") View view1 = LayoutInflater.from(getActivity()).inflate(R.layout.prog, null);
            ProgressBar progressBar = view1.findViewById(R.id.the_prog);
            bars[0]=progressBar;
            progressBar.setIndeterminate(true);
            checkEa.getBuilder()
                    .setView(view1)
                    .setCancelable(false);
            checkEa.show();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            checkEa.dismiss();
            View view = getActivity().findViewById(R.id.mainCoo);
            Snackbar snackbar = Snackbar.make(view, "", Snackbar.LENGTH_SHORT);
            switch (integer) {
                case 0: {//Success
                    snackbar.setText("Update successful");
                    break;
                }
                case 1: {//Newest
                    snackbar.setText("Congratulations, this card is newest");
                    break;
                }
                case 2: {//Not found
                    snackbar.setText("Couldn't find this card on the server...\nIs it a local card?");
                    break;
                }
                case -1: {//Error
                    snackbar.setText("Sorry, there are some problems on updating");
                }
            }
            snackbar.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int value = values[0];
            if (value==-1){
                bars[0].setIndeterminate(true);
                return;
            }else if (value==101){
                bars[0].setIndeterminate(true);
                checkEa.getDialog().setTitle("Applying");
                return;
            }else if (value==102){
                checkEa.getDialog().setTitle("Writing");
                return;
            }else if(value==103){
                checkEa.getDialog().setTitle("Re-checking");
                return;
            }
            bars[0].setIndeterminate(false);
            bars[0].setProgress(value);
        }

        @Override
        protected Integer doInBackground(Integer... integers) {
            try {
                CountRule countRule = V.countRules.get(integers[0]);
                long targetJustOneCode = countRule.getJustOneCode();
                float thisVersion = countRule.getVersion();

                ArrayList<String[]> list = Download.downloadList();
                for (String[] strings : list) {
                    long justOneCode = Long.parseLong(strings[3]);
                    float internetVersion = Float.parseFloat(strings[4]);
                    if (justOneCode == targetJustOneCode) {
                        if (internetVersion > thisVersion) {
                            final int[] callbackCode = {0};

                            String url=V.hostHead + strings[0] + ".cr";
                            OkDown.get().download(
                                    url,
                                    new OkDown.DownloadLister() {
                                        @Override
                                        public void downloadProgress(int progress) {
                                            if (progress==-1) {
                                                publishProgress(-1);
                                                return;
                                            }
                                            publishProgress(progress);
                                        }

                                        @Override
                                        public void failed(Exception ex) {
                                            callbackCode[0]=-1;
                                            synchronized (OkDown.lock){
                                                OkDown.lock.notify();
                                            }
                                        }

                                        @Override
                                        public void done(byte[] bs) {
                                            try {
                                                publishProgress(101);
                                                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bs);
                                                ObjectInputStream hessianInput = new ObjectInputStream(byteArrayInputStream);
                                                CountRule countRule = (CountRule) hessianInput.readObject();
                                                V.countRules.add(countRule);
                                                publishProgress(102);
                                                String stringBuilder = getActivity().getFilesDir().getPath() +
                                                        "/" +
                                                        countRule.getJustOneCode() +
                                                        ".cr";
                                                File file=new File(stringBuilder);
                                                FileOutputStream fileOutputStream=new FileOutputStream(file);
                                                fileOutputStream.write(bs);
                                                fileOutputStream.close();
                                                publishProgress(103);
                                                ArrayList<MathAdapter.MathAdapterData> mathAdapterData = loadList(getActivity(), null, true);
                                                V.mathAdapter=new MathAdapter(mathAdapterData,getActivity());
                                                handler.sendEmptyMessage(0x01);
                                                callbackCode[0]=0;
                                            }catch (Exception e){
                                                callbackCode[0] =-1;
                                                failed(e);
                                            }
                                        }
                                    });
                            synchronized (OkDown.lock){
                                OkDown.lock.wait();
                            }
                            if (callbackCode[0] ==-1){
                                throw new IOException("");
                            }
                            return 0;//Success
                        } else {
                            return 1;//Newest
                        }
                    }
                }
                return 2;//Not found
            } catch (Exception e) {
                e.printStackTrace();
                return -1;//Error
            }
        }
    }

    static class AsyCheckAll extends AsyncTask<Void,Void,Integer>{

        private Ea checkEa;
        private WeakReference<MainActivity> weakReference;

        AsyCheckAll(MainActivity context) {
            this.weakReference = new WeakReference<>(context);
        }

        private MainActivity getActivity() {
            MainActivity mainActivity = weakReference.get();
            if (mainActivity == null || mainActivity.isFinishing())
                throw new NullPointerException();
            return mainActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            checkEa=new Ea(getActivity());
            @SuppressLint("InflateParams") View view=LayoutInflater.from(getActivity()).inflate(R.layout.prog,null);
            ProgressBar bar=view.findViewById(R.id.the_prog);
            bar.setIndeterminate(true);
            checkEa.getBuilder().setView(view);
            checkEa.setTitle("Preparing");
            checkEa.show();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            checkEa.dismiss();
            /*
             * 0        Success
             * -1       Failed
             */
            Snackbar snackbar=Snackbar.make(getActivity().findViewById(R.id.mainCoo),"", Snackbar.LENGTH_SHORT);
            if (integer==0){
                snackbar.setText("Update checked");
            }else if (integer==-1){
                snackbar.setText("Failed to get update");
            }
            snackbar.show();
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            try{
                ArrayList<MathAdapter.MathAdapterData> mathAdapterData = loadList(getActivity(), null, true);
                V.mathAdapter=new MathAdapter(mathAdapterData,getActivity());
                handler.sendEmptyMessage(0x01);
                return 0;
            }catch (Exception e){
                return -1;
            }
        }
    }
}