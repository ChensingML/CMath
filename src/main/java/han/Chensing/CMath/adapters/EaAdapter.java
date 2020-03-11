package han.Chensing.CMath.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import han.Chensing.CMath.R;

public class EaAdapter extends BaseAdapter {

    private Context context;
    private EaData[] data;

    public EaAdapter(Context context,EaData[] data) {
        super();
        this.context=context;
        this.data=data;
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int position) {
        return data[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        TextView textView;
        if (convertView == null) {
            convertView= LayoutInflater.from(context).inflate(R.layout.list_2,null);
        }
        imageView=convertView.findViewById(R.id.list_2_image);
        textView=convertView.findViewById(R.id.list_2_text);
        imageView.setImageResource(data[position].res_image);
        if (data[position].isTextInt)
            textView.setText(data[position].text2);
        else
            textView.setText(data[position].text);
        return convertView;
    }

    public static class EaData{

        private int res_image;
        private String text;
        private int text2;
        boolean isTextInt;

        public EaData(int res_image,int text){
            this.res_image=res_image;
            this.text2=text;
            isTextInt=true;
        }

        public EaData(int res_image,String text){
            this.res_image=res_image;
            this.text=text;
            isTextInt=false;
        }

        public String getText() {
            return text;
        }

        public int getText2() {
            return text2;
        }

        public int getRes_image() {
            return res_image;
        }
    }
}
