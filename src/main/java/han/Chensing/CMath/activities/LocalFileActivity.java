package han.Chensing.CMath.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

import han.Chensing.CMath.CountRule;
import han.Chensing.CMath.MainActivity;
import han.Chensing.CMath.R;
import han.Chensing.CMath.V;
import han.Chensing.CMath.adapters.EaAdapter;
import han.Chensing.CMath.widget.Ea;

public class LocalFileActivity extends AppCompatActivity {

    static Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_file);

        handler=new Handler(getMainLooper());

        String defaultPath = Environment.getExternalStorageDirectory().getPath();
        Stack<String> pathStack = new Stack<>();
        pathStack.push(defaultPath);
        ListView listView = findViewById(R.id.local_list);
        listView.setAdapter(getPathAdapter(defaultPath));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {//Back
                pathStack.pop();
                if (pathStack.empty()) {//A empty stack, finish
                    finish();
                } else {//Not a empty stack, back to last folder
                    listView.setAdapter(getPathAdapter(pathStack.peek()));
                }
            } else {//File or folder
                EaAdapter pathAdapter = getPathAdapter(pathStack.peek());
                EaAdapter.EaData item = (EaAdapter.EaData) (pathAdapter.getItem(position));
                if (item.getRes_image() == R.drawable.ic_folder) {                            //Folder
                    pathStack.push(pathStack.peek() + "/" + item.getText());          //Put child path father path
                    listView.setAdapter(getPathAdapter(pathStack.peek()));
                } else if (item.getRes_image() == R.drawable.ic_otherfile) {                    //Other file
                    Snackbar.make(listView, "Not a support file", Snackbar.LENGTH_SHORT).show();
                } else if (item.getRes_image() == R.drawable.ic_card) {                          //Card file
                    //TODO Yes,it is.
                    String path = pathStack.peek() + "/" + item.getText();
                    new AsyApplyFileCountRule(this).execute(path);
                }
            }
        });
    }

    private EaAdapter getPathAdapter(String parentPath) {
            File[] files = new File(parentPath).listFiles();
            ArrayList<EaAdapter.EaData> data = new ArrayList<>();
            data.add(new EaAdapter.EaData(R.drawable.ic_back, R.string.file_back));
            if (files == null) {
                return new EaAdapter(this, data.toArray(new EaAdapter.EaData[0]));
            }
            Arrays.sort(files, (o1, o2) -> {
                if (o1.isDirectory() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDirectory())
                    return 1;
                return o1.getName().compareTo(o2.getName());
            });
            for (File file : files) {
                String fileName = file.getName();
                if (file.isDirectory()) {
                    data.add(new EaAdapter.EaData(R.drawable.ic_folder, fileName));
                } else {
                    int lio = fileName.lastIndexOf(".");
                    if (lio != -1) {
                        if (fileName.substring(lio).equals(".cr")) {
                            data.add(new EaAdapter.EaData(R.drawable.ic_card, fileName));
                        } else {
                            data.add(new EaAdapter.EaData(R.drawable.ic_otherfile, fileName));
                        }
                    } else {
                        data.add(new EaAdapter.EaData(R.drawable.ic_otherfile, fileName));
                    }
                }
            }
        return new EaAdapter(this, data.toArray(new EaAdapter.EaData[0]));
    }

    static class AsyApplyFileCountRule extends AsyncTask<String, Integer, Integer> {

        private final Object lock = new Object();
        private int yourSelect=6;
        private Ea ea;


        private WeakReference<LocalFileActivity> weakReference;

        AsyApplyFileCountRule(LocalFileActivity context) {
            this.weakReference = new WeakReference<>(context);
        }

        private Activity getActivity() {
            LocalFileActivity localFileActivity = weakReference.get();
            if (localFileActivity == null || localFileActivity.isFinishing())
                throw new NullPointerException();
            return localFileActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            create();
            ea.show();
        }

        private void create(){
            ea = new Ea(getActivity());
            ea.setTitle("Applying");
            @SuppressLint("InflateParams") View view = LayoutInflater.from(getActivity()).inflate(R.layout.prog, null);
            ProgressBar progressBar = view.findViewById(R.id.the_prog);
            progressBar.setIndeterminate(true);
            ea.getBuilder().setView(view);
            ea.getBuilder().setCancelable(false);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            switch (integer){
                case 401:{//Import error

                }
                case 100:{//Successful
                    ea.dismiss();
                    Snackbar.make(
                            getActivity().findViewById(R.id.local_list),
                            "Import successful",
                            Snackbar.LENGTH_SHORT).show();
                    break;
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            ea.dismiss();
            int value = values[0];
            //Override
            if (value == 301) {
                Ea selectEa = new Ea(getActivity());
                selectEa
                        .setTitle("Apply?")
                        .setMessage("This card is the same as the one you collected, Overwrite it?");
                selectEa.getBuilder()
                        .setCancelable(false)
                        .setNeutralButton("Override!", (dialog, which) -> {
                            dialog.dismiss();
                            create();
                            ea.show();
                            yourSelect = 1;
                            synchronized (lock) {
                                lock.notify();
                            }
                        })
                        .setPositiveButton("Let me think", (dialog, which) -> {
                            ea.dismiss();
                            dialog.dismiss();
                            yourSelect = 0;
                            synchronized (lock) {
                                lock.notify();
                            }
                        });
                selectEa.show();
            }
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        protected Integer doInBackground(String... strings) {
            try {
                String path = strings[0];
                CountRule countRule = new CountRule(path);         //Load CountRule
                int willDelete=-1;
                long justOneCode = countRule.getJustOneCode();
                int size = V.countRules.size();
                for (int i = 0; i!= size;i++) {
                    CountRule rule=V.countRules.get(i);
                    long justOneCodeOnList = rule.getJustOneCode();
                    if (justOneCode == justOneCodeOnList) {           //Override? Ask the user
                        publishProgress(301);
                        synchronized (lock) {
                            lock.wait();
                        }
                        if (yourSelect == 0) {//Don't override
                            return 1;//User canceled
                        }//Override!
                        willDelete=i;
                        break;
                    }
                }
                FileInputStream fileInputStream=new FileInputStream(path);
                byte[] bytes=new byte[fileInputStream.available()];
                fileInputStream.read(bytes);
                fileInputStream.close();

                String appPath = getActivity().getFilesDir().getPath() +
                        "/" +
                        countRule.getJustOneCode() +
                        ".cr";
                FileOutputStream fileOutputStream=new FileOutputStream(appPath);
                fileOutputStream.write(bytes);
                fileOutputStream.close();

                if (yourSelect==1){//Override
                    V.countRules.remove(willDelete);
                }
                V.countRules.add(countRule);
                MainActivity.handler.sendEmptyMessage(0x02);

                return 100;
            } catch (Exception e) {
                e.printStackTrace();
                return 401;
            }
        }
    }
}
