package han.Chensing.CMath.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import han.Chensing.CMath.R;

public class MathAdapter extends BaseAdapter {

    private ArrayList<MathAdapterData> data;
    private Context context;

    public MathAdapter(ArrayList<MathAdapterData> data,Context context){
        this.data=data;
        this.context=context;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int potion, View view, ViewGroup viewGroup) {
        MathAdapterItem mathAdapterItem;
        if (view==null){
            view= LayoutInflater.from(context).inflate(R.layout.list_1,null);
            mathAdapterItem= new MathAdapterItem();
            mathAdapterItem.mainText=view.findViewById(R.id.list1_name);
            mathAdapterItem.editor=view.findViewById(R.id.list1_editor);
            mathAdapterItem.secondText=view.findViewById(R.id.list1_second);
            mathAdapterItem.hasUpdate=view.findViewById(R.id.list_1_update);
            mathAdapterItem.relativeLayout=view.findViewById(R.id.list_1_rl);
            view.setTag(mathAdapterItem);
        }else {
            mathAdapterItem=(MathAdapterItem)view.getTag();
        }
        MathAdapterData data=(MathAdapterData) getItem(potion);
        mathAdapterItem.mainText.setText(data.strMainText);
        mathAdapterItem.secondText.setText(data.strSecondText);
        mathAdapterItem.editor.setText(data.strEditor);
        if (data.hasUpdate){
            mathAdapterItem.hasUpdate.setVisibility(View.VISIBLE);
        }else {
            mathAdapterItem.hasUpdate.setVisibility(View.GONE);
        }
        if (data.isCollected){
            mathAdapterItem.relativeLayout.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.h_shape_selected));
        }
        return view;
    }


    static class MathAdapterItem{

        TextView mainText;
        TextView secondText;
        TextView editor;
        ImageView hasUpdate;
        RelativeLayout relativeLayout;

    }

    public static class MathAdapterData{

        String strMainText;
        String strSecondText;
        String strEditor;
        boolean isCollected;
        boolean hasUpdate;

        public MathAdapterData(String strMainText, String strSecondText, String strEditor,boolean isCollected,boolean hasUpdate) {
            this.strMainText=strMainText;
            this.strSecondText=strSecondText;
            this.strEditor=strEditor;
            this.isCollected=isCollected;
            this.hasUpdate=hasUpdate;
        }
    }
}

