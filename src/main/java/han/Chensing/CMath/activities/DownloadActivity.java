package han.Chensing.CMath.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import han.Chensing.CMath.CountRule;
import han.Chensing.CMath.MainActivity;
import han.Chensing.CMath.R;
import han.Chensing.CMath.V;
import han.Chensing.CMath.adapters.MathAdapter;
import han.Chensing.CMath.tools.Download;
import han.Chensing.CMath.tools.OkDown;

public class DownloadActivity extends AppCompatActivity {

    Handler handler;
    ListView listView;
    SwipeRefreshLayout refreshLayout;
    static ArrayList<String[]> arrayList;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.down_load);

        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null)actionBar.setTitle("Cards store");
        else {
            android.app.ActionBar actionBar1=getActionBar();
            if (actionBar1!=null)actionBar1.setTitle("Cards store");
        }

        handler=new Handler(getMainLooper());

        listView=findViewById(R.id.downLv);
        refreshLayout=findViewById(R.id.downWrl);

        refreshLayout.setColorSchemeColors(getResources().getColor(R.color.c_text));

        refreshLayout.setOnRefreshListener(() -> refresh(false));

        listView.setOnItemClickListener((parent, view, position, id) -> {
            final StringBuilder stringBuilder=new StringBuilder();
            final String[] data = arrayList.get(position);
            int location=location(data[3]);
            boolean collected=location!=-1;
            if (!collected) {
                stringBuilder
                        .append("Would you like to collect ")
                        .append(data[0])
                        .append(" by ")
                        .append(data[2])
                        .append(" ?");
            }else {
                stringBuilder
                        .append("You had early collect this card, re-collect?");
            }
            AlertDialog.Builder builder=new AlertDialog.Builder(DownloadActivity.this);
            builder.setPositiveButton("Yes", (dialog, which) -> {
                dialog.dismiss();
                if (collected){
                    V.countRules.remove(location);
                }
                new AsyDownCountRule(DownloadActivity.this).execute(data[0]);
            });
            builder.setNeutralButton("No", (dialog, which) -> dialog.dismiss());
            builder
                    .setTitle("Collect?")
                    .setMessage(stringBuilder.toString())
                    .show();
        });

        refresh(true);
    }

    private void refresh(final boolean isFirst){
        new AsyRefresh(this).execute(isFirst);
    }


    static class AsyRefresh extends AsyncTask<Boolean,Integer,Integer>{

        private Throwable throwable;
        private MathAdapter mathAdapter;
        private AlertDialog show;
        private WeakReference<DownloadActivity> weakReference;


        AsyRefresh(DownloadActivity context){
            this.weakReference=new WeakReference<>(context);
        }

        private DownloadActivity getActivity(){
            DownloadActivity downloadActivity = weakReference.get();
            if (downloadActivity==null||downloadActivity.isFinishing()) throw new NullPointerException();
            return downloadActivity;
        }


        @Override
        protected Integer doInBackground(Boolean... isFirst) {
            try {
                ArrayList<String[]> list=Download.downloadList();
                arrayList=list;
                ArrayList<MathAdapter.MathAdapterData> data = new ArrayList<>();
                for (String[] ss : list) {
                    MathAdapter.MathAdapterData mathAdapterData = new MathAdapter.MathAdapterData(ss[0],ss[1],ss[2],collected(ss[3]),false);
                    data.add(mathAdapterData);
                }
                mathAdapter = new MathAdapter(data, getActivity());
            } catch (IOException e) {
                return 401;
            } catch (Exception e) {
                throwable=e;
                return 402;
            }
            return 0;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SwipeRefreshLayout swipeRefreshLayout=getActivity().findViewById(R.id.downWrl);
            if (!swipeRefreshLayout.isRefreshing()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                @SuppressLint("InflateParams") View view = LayoutInflater.from(getActivity()).inflate(R.layout.prog, null);
                ProgressBar progressBar = view.findViewById(R.id.the_prog);
                progressBar.setIndeterminate(true);
                builder.setView(view);
                builder.setTitle("Loading store...");
                builder.setCancelable(false);
                show = builder.show();
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            SwipeRefreshLayout swipeRefreshLayout=getActivity().findViewById(R.id.downWrl);
            if (!swipeRefreshLayout.isRefreshing())show.dismiss();
            if (integer != 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                String title="",message="";
                if (integer == 401) {
                    title = "Emm...";
                    message = "We can't connect to the Internet, check your Internet and try again, please.";
                }else if (integer==402) {
                    title = "Oops";
                    message = "There are some problem with program, cause:"+throwable.toString();
                }
                builder
                        .setCancelable(false)
                        .setPositiveButton("Back", (dialog, which) -> {
                            dialog.dismiss();
                            getActivity().finish();
                        })
                        .setTitle(title)
                        .setMessage(message)
                        .show();
                return;
            }
            swipeRefreshLayout.setRefreshing(false);
            ListView listView=getActivity().findViewById(R.id.downLv);
            listView.setAdapter(mathAdapter);
        }
    }

    public static class AsyDownCountRule extends AsyncTask<String,Integer,Boolean> {


        private AlertDialog alertDialog;
        private WeakReference<Activity> weakReference;


        public AsyDownCountRule(Activity context){
            this.weakReference=new WeakReference<>(context);
        }

        private Activity getActivity(){
            Activity downloadActivity = weakReference.get();
            if (downloadActivity==null||downloadActivity.isFinishing()) throw new NullPointerException();
            return downloadActivity;
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            final int[] errorNumber = {0};
            final Throwable[] throwable = {null};
            try {
                String url=V.hostHead + strings[0] + ".cr";
                OkDown.get().download(
                        url,
                        new OkDown.DownloadLister() {
                            @Override
                            public void downloadProgress(int progress) {
                                if (progress==-1){
                                    publishProgress(103);
                                    return;
                                }
                                publishProgress(progress);
                            }

                            @Override
                            public void failed(Exception ex) {
                                throwable[0] = ex;
                                errorNumber[0] = 402;
                                synchronized (OkDown.lock){
                                    OkDown.lock.notify();
                                }
                            }

                            @Override
                            public void done(byte[] bs) {
                                try {
                                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bs);
                                    ObjectInputStream hessianInput = new ObjectInputStream(byteArrayInputStream);
                                    CountRule countRule = (CountRule) hessianInput.readObject();
                                    V.countRules.add(countRule);
                                    publishProgress(101);
                                    String stringBuilder = getActivity().getFilesDir().getPath() +
                                            "/" +
                                            countRule.getJustOneCode() +
                                            ".cr";
                                    File file=new File(stringBuilder);
                                    FileOutputStream fileOutputStream=new FileOutputStream(file);
                                    fileOutputStream.write(bs);
                                    fileOutputStream.close();
                                    publishProgress(102);
                                    synchronized (OkDown.lock){
                                        OkDown.lock.notify();
                                    }
                                }catch (Exception e){
                                    failed(e);
                                }
                            }
                        });

                synchronized (OkDown.lock){
                    OkDown.lock.wait();
                }
            } catch (Exception ex) {
                throwable[0] = ex;
                errorNumber[0] = 403;
            }
            if (errorNumber[0] != 0) {
                throwable[0].printStackTrace();
                publishProgress(errorNumber[0]);
                return false;
            }
            return true;
        }

        private void showErrorDialog(final String message) {
            alertDialog.dismiss();
            AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
            builder2
                    .setTitle("Oh no!")
                    .setMessage(message)
                    .show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            ProgressBar progressBar=alertDialog.findViewById(R.id.the_prog);
            if (progressBar==null) throw new NullPointerException();
            int value=values[0];
            if (value<=100) {
                progressBar.setProgress(value);
            }else if (value==101){
                alertDialog.setTitle("Applying");
                progressBar.setIndeterminate(true);
            } else if (value==102) {
                alertDialog.dismiss();
                MainActivity.handler.sendEmptyMessage(0x01);
                MainActivity.handler.sendEmptyMessage(0x02);
            }else if (value==103) {
                progressBar.setIndeterminate(true);
            }else if (value>400){
                alertDialog.dismiss();
                if (value==401)
                    showErrorDialog("There are some errors on collecting, check your Internet?");
                else if (value==402)
                    showErrorDialog("There are some problems on applying the card, you can try again; if problems continue, beat editor, please.\n <=");
                else if (value==403)
                    showErrorDialog("Some other error happen, try again?");
                else if (value==404){
                    progressBar.setIndeterminate(true);
                }
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            @SuppressLint("InflateParams") View view1 = LayoutInflater.from(getActivity()).inflate(R.layout.prog, null);
            ProgressBar progressBar = view1.findViewById(R.id.the_prog);
            progressBar.setMax(100);
            AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
            alertDialog = builder1
                    .setTitle("Collecting")
                    .setView(view1)
                    .setCancelable(false)
                    .show();
        }

        @Override
        protected void onPostExecute(Boolean boo) {
            super.onPostExecute(boo);
            if (boo) {
                View view ;
                if (getActivity() instanceof MainActivity)
                    view=getActivity().findViewById(R.id.mainCoo);
                else
                    view=getActivity().findViewById(R.id.downRl);
                if (getActivity() instanceof DownloadActivity) {
                    Snackbar.make(view, "Collect successful!", Snackbar.LENGTH_SHORT).show();
                    new AsyRefresh((DownloadActivity) getActivity()).execute(true);
                }
            }
        }
    }


    private static boolean collected(String justOneCode){
        return location(justOneCode) != -1;
    }

    private static int location(String justOneCode){
        int len=V.countRules.size();
        int code=Integer.parseInt(justOneCode);
        for (int i=0;i!=len;i++){
            if (V.countRules.get(i).getJustOneCode()==code)
                return i;
        }
        return -1;
    }
}
