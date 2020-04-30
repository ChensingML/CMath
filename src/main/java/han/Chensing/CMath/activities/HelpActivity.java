package han.Chensing.CMath.activities;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;

import han.Chensing.CMath.R;
import han.Chensing.CMath.adapters.EaAdapter;
import han.Chensing.CMath.widget.Ea;

public class HelpActivity extends AppCompatActivity {

    ListView listView;
    EditText search;

    static ArrayList<DataSet> sets=new ArrayList<>();

    static ArrayList<DataSet> nowSet=new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
        actionBar.setTitle("Help");
        actionBar.show();

        setContentView(R.layout.help);

        listView=findViewById(R.id.help_list);
        search=findViewById(R.id.help_search);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                listView.setAdapter(search(s.toString()));
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            DataSet dataSet = nowSet.get(position);
            Ea ea=new Ea(this);
            ea
                    .setTitle(dataSet.problem.replace('&','\n'))
                    .setMessage(dataSet.solve.replace('&','\n'))
                    .getBuilder().setPositiveButton("I see",null)
                    .show();
        });

        new AsyLoadHelp(this).execute();
    }

    private EaAdapter search(String searchWord){
        if (searchWord.isEmpty()){
            nowSet=sets;
            return getAdapter(this,sets);
        }
        ArrayList<DataSet> data=new ArrayList<>();
        int length=sets.size();
        for (int i=0;i!=length;i++){
            String
                    title=sets.get(i).problem.toLowerCase().replace(" ",""),
                    message=sets.get(i).solve.toLowerCase().replace(" ","");
            String lowerWord = searchWord.toLowerCase().replace(" ","");
            if (
                    title.lastIndexOf(lowerWord)!=-1
                    || message.lastIndexOf(lowerWord)!=-1){
                data.add(sets.get(i));
            }
        }
        nowSet.clear();
        nowSet.addAll(data);
        return getAdapter(this,data);
    }

    static EaAdapter getAdapter(Context context,ArrayList<DataSet> list){

        EaAdapter.EaData[] data=new EaAdapter.EaData[list.size()];

        for (int i=0;i!=data.length;i++){
            data[i]=new EaAdapter.EaData(
                    R.drawable.ic_help_info,
                    list.get(i).problem);
        }

        return new EaAdapter(context, data);
    }

    static class DataSet{

        String problem;
        String solve;

        DataSet(String problem, String solve){
            this.problem=problem;
            this.solve=solve;
        }
    }

    static class AsyLoadHelp extends AsyncTask<Void,Void,EaAdapter>{

        private WeakReference<HelpActivity> weakReference;

        AsyLoadHelp(HelpActivity context) {
            this.weakReference = new WeakReference<>(context);
        }

        private HelpActivity getActivity() {
            HelpActivity helpActivity = weakReference.get();
            if (helpActivity == null || helpActivity.isFinishing())
                throw new NullPointerException();
            return helpActivity;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressBar progressBar=getActivity().findViewById(R.id.help_progressBar);
            progressBar.setIndeterminate(true);
        }

        @Override
        protected void onPostExecute(EaAdapter adapter) {
            super.onPostExecute(adapter);
            ListView listView=getActivity().findViewById(R.id.help_list);
            listView.setAdapter(adapter);
            ProgressBar progressBar=getActivity().findViewById(R.id.help_progressBar);
            progressBar.setIndeterminate(false);
        }

        @Override
        protected EaAdapter doInBackground(Void... voids) {
            try{
                ArrayList<String> list=new ArrayList<>();
                String line;

                AssetManager assetManager=getActivity().getAssets();
                InputStream open = assetManager.open("help.txt");
                BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(open));
                while((line=bufferedReader.readLine())!=null){
                    list.add(line);
                }

                sets.clear();

                for (String string:list){
                    String[] ss=string.split("#");
                    sets.add(new DataSet(ss[0],ss[1]));
                }
                nowSet.addAll(sets);
                return getAdapter(getActivity(),sets);

            }catch (Exception e){
                return null;
            }
        }
    }
}
