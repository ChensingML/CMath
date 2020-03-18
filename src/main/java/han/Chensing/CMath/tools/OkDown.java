package han.Chensing.CMath.tools;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class OkDown {

    private static OkDown okDown;
    private static OkHttpClient okHttpClient;
    public static final Object lock=new Object();

    public static OkDown get(){
        if (okDown==null){
            okDown=new OkDown();
        }
        return okDown;
    }

    private OkDown(){
        okDown=this;
        okHttpClient=new OkHttpClient();
    }

    public void download(
            String url,
            DownloadLister lister){
        Request request=new Request.Builder()
                .get()
                .url(url)
                .addHeader("Accept-Encoding", "identity")
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                lister.failed(e);
            }

            @Override
            public void onResponse(Response response) {
                try {
                    ResponseBody body = response.body();
                    InputStream inputStream= body.byteStream();
                    long fileLength= body.contentLength();
                    if (fileLength!=-1) {
                        long every = fileLength / 100;
                        byte[] bytes = new byte[(int) fileLength];
                        int jd = 0;
                        for (long i = 0, i2 = 0; i != fileLength; i++, i2++) {
                            int b = inputStream.read();
                            bytes[(int) i] = (byte) b;
                            if (i2 == every) {
                                lister.downloadProgress(++jd);
                                i2=0;
                            }
                        }
                        lister.done(bytes);
                    }else {
                        lister.downloadProgress(-1);
                        /*ArrayList<Byte> byteArrayList=new ArrayList<>();
                        byte b;
                        while ((b=(byte) inputStream.read())!=-1){
                            byteArrayList.add(b);
                        }
                        byte[] bytes=new byte[byteArrayList.size()];
                        for (int i=0;i!=bytes.length;i++){
                            bytes[i]=byteArrayList.get(i);
                        }*/
                        ArrayList<Byte> integers=new ArrayList<>();
                        byte[] buffer=new byte[1024];
                        int length;
                        do {
                            length=inputStream.read(buffer);
                            for (int i = 0; i < length; i++) {
                                integers.add(buffer[i]);
                            }
                        }while (length!=-1);
                        byte[] bytes=new byte[integers.size()];
                        for (int i = 0; i < bytes.length; i++) {
                            bytes[i]= integers.get(i);
                        }
                        lister.done(bytes);
                    }

                    inputStream.close();

                    synchronized (lock){
                        lock.notify();
                    }
                }catch (Exception e){
                    lister.failed(e);
                }
            }
        });
    }

    public interface DownloadLister{
        void downloadProgress(int progress);
        void failed(Exception ex);
        void done(byte[] bs);
    }
}
