
package han.Chensing.CMath;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class CountRule implements Serializable {

    public enum ShowPlace{
        IN_BUTTON,
        IN_SCREEN,
        IN_TEXT
    }


    public static final long serialVersionUID=1L;

    private long justOneCode;
    private String name;
    private String message;
    private String editor;
    private byte[] bitmap;
    private String introduction;
    private String[] formalParameters;
    private float version;
    private ShowPlace showPlace;

    private String packageName;
    private byte[] output;
    private String[] realParameters;

    public CountRule(
            String name,
            String message,
            String editor,
            byte[] bitmap,
            String introduction,
            String[] formalParameters,
            String packageName,
            byte[] output,
            long justOneCode,
            float version,
            ShowPlace showPlace){
        this.name=name;
        this.message=message;
        this.editor=editor;
        this.bitmap=bitmap;
        this.introduction=introduction;
        this.formalParameters=formalParameters;
        this.packageName=packageName;
        this.output=output;
        this.justOneCode=justOneCode;
        this.version=version;
        this.showPlace=showPlace;
    }

    public CountRule(String path) throws IOException, ClassNotFoundException {
        CountRule countRule = readFromFile(path);
        name= countRule.getName();
        message= countRule.getMessage();
        editor= countRule.getEditor();
        bitmap= countRule.getBitmap();
        introduction= countRule.getIntroduction();
        formalParameters= countRule.getFormalParameters();
        packageName= countRule.getPackageName();
        output= countRule.getOutput();
        justOneCode= countRule.getJustOneCode();
        version= countRule.getVersion();
        showPlace= countRule.getShowPlace();
    }

    private static CountRule readFromFile(String path) throws IOException, ClassNotFoundException {
        File file=new File(path);
        if (!file.exists()){
            throw new IOException("File not found!");
        }
        InputStream inputStream=new FileInputStream(file);
        long length=file.length();
        byte[] bytes=new byte[(int)length];
        if(inputStream.read(bytes)!=bytes.length) {
            inputStream.close();
            throw new IOException("File Error");
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream hessianInput=new ObjectInputStream(byteArrayInputStream);
        CountRule countRule =(CountRule) hessianInput.readObject();
        byteArrayInputStream.close();
        inputStream.close();
        return countRule;
    }

    public byte[] serS() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream=new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(this);
        byte[] bs=byteArrayOutputStream.toByteArray();
        objectOutputStream.close();
        byteArrayOutputStream.close();
        return bs;
    }

    //G/S


    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public byte[] getBitmap() {
        return bitmap;
    }

    public String getEditor() {
        return editor;
    }

    public String[] getFormalParameters() {
        return formalParameters;
    }

    public String getPackageName(){
        return packageName;
    }

    public byte[] getOutput() {
        return output;
    }

    public String getIntroduction() {
        return introduction;
    }

    public long getJustOneCode() {
        return justOneCode;
    }

    public float getVersion() {
        return version;
    }

    public ShowPlace getShowPlace() {
        return showPlace;
    }

    public String[] getRealParameters() {
        return realParameters;
    }

    public void setRealParameters(String[] realParameters) {
        this.realParameters = realParameters;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj==null) return false;
        if (obj instanceof CountRule)return false;
        CountRule countRule;
        try{
            countRule= (CountRule) obj;
        }catch (Exception ce){return false;}
        return countRule.getJustOneCode() == this.getJustOneCode();
    }
}
