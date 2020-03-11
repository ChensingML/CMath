package han.Chensing.CMath.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import han.Chensing.CMath.V;

public class Download {

    private static ArrayList<String[]> download(boolean justVersion) throws IOException{
        URL url = new URL(V.hostHead + "List.txt");
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(500);
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        ArrayList<String[]> list = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            char c = line.charAt(0);
            if (c == '#') continue;
            else if (c=='*'){
                if (justVersion) {
                    list.add(new String[]{line.substring(1)});
                    return list;
                }
                continue;
            }
            if (!justVersion)
                list.add(line.split("&"));
        }
        return list;
    }

    public static ArrayList<String[]> downloadList() throws IOException {
        return download(false);
    }

    public static int downloadVersion() throws IOException{
        return Integer.parseInt(download(true).get(0)[0]);
    }

    public static class SerException extends Throwable{

    }

}
