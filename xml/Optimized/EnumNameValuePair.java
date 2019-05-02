package xml.Optimized;

import java.nio.charset.MalformedInputException;

/**
 * Created by ra on 29/05/2017.
 */

public class EnumNameValuePair {
    private int name;
    private String value;
    public EnumNameValuePair(int name, String value){
        this.name = name;
        this.value = value;
    }
    public EnumNameValuePair(){
        this.value = null;
        this.name = 0;
    }

    public void setName(int name) {
        this.name = name;
    }

    public void setValue(String value) {
        if(value.indexOf("=\"") == 0){
            throw new IndexOutOfBoundsException("value incorect:"+value);
        }
        this.value = value;
    }

    public int getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name+" = "+value;
    }
}
