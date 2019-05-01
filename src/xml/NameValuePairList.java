package xml;

import java.util.ArrayList;

import Analytics.CrashReporter;
import xml.unoptimized.NameValuePair;

public class NameValuePairList {
    private ArrayList<NameValuePair> nvps;
    private String[] values;
    public NameValuePairList(String[] allPossibleValues){
        values = allPossibleValues;
        nvps = new ArrayList<NameValuePair>();
    }

    public void clear(){
        nvps.clear();
    }
    public xml.unoptimized.NameValuePair remove(int index){
        return nvps.remove(index);
    }
    public int size(){
        return nvps.size();
    }
    public xml.unoptimized.NameValuePair get(int i){
        return nvps.get(i);
    }
    public String getAttributeValue(int nameIndex){
        for(int i = 0; i<nvps.size(); i++){
            if(nvps.get(i).name  == nameIndex){//if the nvp has no name index the name index will be -1
                return nvps.get(i).getValue();
            }
        }
        return null;
    }
    public String getAttributeValue(String name){
        for(int i = 0; i<nvps.size(); i++){
            if(nvps.get(i).getName().equals(name)){
                return nvps.get(i).getValue();
            }
        }
        return null;
    }
    public void add(int name, String value){
        this.add(new NameValuePair(name,value));
    }
    public void add(xml.unoptimized.NameValuePair nvp){
        nvps.add(new NameValuePair(nvp.getName(), nvp.getValue()));
    }
    public void add(byte[] buffer, int nameStart, int nameEnd, int valueStart, int valueEnd){
        int nameIndex = getIndex(buffer, nameStart,nameEnd);
        int valueIndex = getIndex(buffer,valueStart,valueEnd);
        if(nameIndex == -1 || valueIndex == -1){
            if(nameIndex ==-1 && valueIndex == -1){
                String nameString = new String(buffer, nameStart, nameEnd);
                String valueString = new String(buffer, valueStart, valueEnd);
                nvps.add(new NameValuePair(nameString,valueString));
            }
            else if(valueIndex == -1){
                nvps.add(
                        new NameValuePair(
                                nameIndex,
                                new String(buffer, nameStart,nameEnd)
                        )
                );
            }
            else if(nameIndex ==-1){
                nvps.add(
                        new NameValuePair(
                            new String(buffer, nameStart,nameEnd),
                            valueIndex
                        )
                );
            }
        }
        else{
            nvps.add(
                    new NameValuePair(nameIndex, valueIndex)
            );
        }

    }
    private int getIndex(byte[] buffer, int start, int end){
        String s;
        int stringLength = end-start;
        for(int string = 0; string< values.length; string++){//find string in predetermined strings

            s = values[string];
            if(s.length() != stringLength){ continue; }
            else{
                boolean goodString = true;
                for(int i = 0; i<stringLength; i++){
                    if(buffer[start+i] != s.charAt(i)){  goodString = false; break; }
                }
                if(goodString){ return string; }
            }
        }
        return -1;
    }

    class NameValuePair implements xml.unoptimized.NameValuePair {
        int name = -1,value = -1;
        String nameString, valueString;
        NameValuePair(int name, int value){
            this.name = name;
            this.value = value;
        }
        NameValuePair( String name, int value){
            this.nameString = name;
            this.value = value;
        }
        NameValuePair( int name, String value){
            this.name = name;
            this.valueString = value;
        }
        NameValuePair(String name, String value){
            this.nameString = name;
            this.valueString = value;
        }

        public String getName() {
            if(nameString != null){
                return nameString;
            }
            else{
                return new String(values[name]);
            }
        }
        public String getValue(){
            if(valueString != null){
                return valueString;
            }
            else{
                return new String(values[value]);
            }
        }
        public int getNameIndex(){
            if(name == -1){
                CrashReporter.log("name error:"+nameString);
                throw new IndexOutOfBoundsException("name not set correctly");
            }
            return name;
        }
        public int getValueIndex(){
            if( value == -1){
                CrashReporter.log("value error:"+valueString);
                throw new IndexOutOfBoundsException("value not set correctly");
            }
            return value;
        }
    }


}
