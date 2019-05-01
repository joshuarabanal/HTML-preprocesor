package xml.unoptimized;

import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import xml.NameValuePairList;
import xml.XmlCursor;


public class Parser {

   
    private BufferedInputStream in;
    private XmlCursor cursor;
    private ArrayList<String> parentHirarchy;
    byte[] backedUpArray = new byte[200];
    int backedUpArrayIndex = 0;
    private String[] allAttributeAndTagNames;
    private boolean autoCloseStream = true;
   private String logFile;




    public Parser(File f, XmlCursor curs, @Nullable String[] allStrings) throws FileNotFoundException{
        this(new FileInputStream(f),curs,allStrings);
        logFile  = f.toString();

    }
    public Parser(InputStream is, XmlCursor curs, @Nullable String[] allStrings){
        allAttributeAndTagNames = allStrings;
        in = new BufferedInputStream(is);
        cursor = curs;
        parentHirarchy = new ArrayList<String>();
        logFile = is.toString();
    }
    public void disableAutoCloseStream(){
        autoCloseStream = false;
    }

    private int backedUpEndIndex = 0;
    private int readSingleByte() throws IOException {
        if(backedUpArrayIndex >= backedUpEndIndex ){
           int howMany = in.read(backedUpArray, backedUpEndIndex,backedUpArray.length- backedUpEndIndex);
            if(howMany<0){
                //og.i("end of file","end of file reached");
                return -1;
            }
            backedUpEndIndex+=howMany;
        }
        
        if(backedUpArrayIndex>=backedUpArray.length){
            String text = new String(backedUpArray, 0, 100);
            //og.i("emergency text element", text);
            if(text.contains("<") && text.indexOf("<!--")!=0){//if there is a problem and this is not a comment
               
                throw new IndexOutOfBoundsException("malformed text element:"+text);
            }
            
            textElement(100); 
            return readSingleByte();
            //throw new IndexOutOfBoundsException("index:"+backedUpArrayIndex+", length:"+backedUpArray.length);
        }
        int retu =  0xFF&backedUpArray[backedUpArrayIndex++];
        //if(retu<0){ og.i("end of file?",""+retu); }
        return retu;
    }
    private void shiftArray(int newZero) throws IOException {
        if(newZero <=0){
            if(newZero==0){ return; }
            throw new IndexOutOfBoundsException("new zero less than 0: "+newZero);
        }

        backedUpArrayIndex -= newZero;
        backedUpEndIndex -=newZero;
        if(backedUpArrayIndex<0 || backedUpEndIndex <0){
            throw new IOException("ATTRIBUTE_index Out of bounds:"+backedUpEndIndex+","+backedUpArrayIndex+","+newZero);
        }
        System.arraycopy( backedUpArray, newZero, backedUpArray, 0, backedUpEndIndex );



        if(backedUpArrayIndex <0){ throw new IndexOutOfBoundsException("backed up array ATTRIBUTE_index<0 = "+backedUpArrayIndex); }

    }
    private void textElement(int end) throws IOException {
        String retu = new String(backedUpArray,0,end).trim();
        if(retu.length()>0){ cursor.textElement(retu);}

        shiftArray(end);
    }
    private boolean isNumber(byte b){
        if(
                b == '0'
                || b == '1'
                || b == '2'
                || b == '3'
                || b == '4'
                || b == '5'
                || b == '6'
                || b == '7'
                || b == '8'
                || b == '9'
                ){
            return true;
        }
        return false;
    }
    private String getString(int start, int length) throws IOException {
        if(allAttributeAndTagNames != null){

            for(String s: allAttributeAndTagNames){
                if(s.length() != length){ continue; }
                else{
                    boolean goodString = true;
                    for(int i = 0; i<length; i++){
                        if(backedUpArray[start+i] != s.charAt(i)){  goodString = false; break; }
                    }
                    if(goodString){ return s; }
                }
            }
        }
        return new String(backedUpArray,start,length);
    }
    private void closeElement(int start) throws Exception{
        if(backedUpArray[start] != '<' || backedUpArray[start+1] != '/'){
            throw new IndexOutOfBoundsException(
                    "cannot close element:"+
                            "array["+start+"] = "+(char)backedUpArray[start]+","+
                            "array["+start+"+1] = "+(char)backedUpArray[start+1]
            );
        }

        start = start+2;
        while(readSingleByte() != '>'){}

        String name = getString(start, backedUpArrayIndex-(start+1) );//new String(backedUpArray,start, backedUpArrayIndex-(start+1));
        if(name.contains("<") || name.contains(">") || name.contains(" ")){
            throw new UnsupportedEncodingException("malformed tag ATTRIBUTE_name:"+name+", start:"+start+", arrayIndex:"+backedUpArrayIndex);
        }
        cursor.closeElement(name);
        //og.i("close element",ATTRIBUTE_name);
        shiftArray(backedUpArrayIndex);

    }
    private int parseAttribute(int start, NameValuePairList attrs) throws IOException {
        Attribute retu = new Attribute();
        //og.i("parse attribute","bytes["+start+"] = "+(char)backedUpArray[start]);
        int b = backedUpArray[start];
        while(b == ' '){ start++; b =  readSingleByte(); }//skip white space
        //og.i("skip white space", (char)b+"=="+backedUpArray[start]);
        while((b = readSingleByte()) != ' ' && b != '='){ }
        //og.i("ATTRIBUTE_name found, b = "+(char)b,new String(backedUpArray,start, backedUpArrayIndex-start-1));
        retu.setName( getString(start,backedUpArrayIndex-start-1));//new String(backedUpArray,start, backedUpArrayIndex-start-1));
        //og.i("attribute name", retu.getName());
        if(b != '='){ while((b = readSingleByte())!='=' ){
            if(b == -1){
                //og.("buffer", new String(backedUpArray,0, backedUpEndIndex));
                //og.("log file", logFile);
                throw new IOException("i dont know");
            }
        }}
        while ( (b=readSingleByte()) ==' '){  }
        start = backedUpArrayIndex-1;
        //og.i("values start", ""+(char)backedUpArray[start]);
        if(b == '\'' || b == '"'){
            skipOverStringLiteral(start);
            //og.i("skipped str literal", new String(backedUpArray,start, backedUpArrayIndex-(start)));
        }
        while( (b = readSingleByte()) != ' ' && b != '>' && b !='/'){}
        //og.i("set value", new String(backedUpArray,start, backedUpArrayIndex-(start+1)));
        retu.setValue(
                getString(start, backedUpArrayIndex-(start+1))
        );
        //og.i("return char","'"+(char)b+"'");
        attrs.add(retu);
        return backedUpArrayIndex-1;

    }
    private int openElement(int start) throws Exception {
        if(backedUpArray[start] != '<') {
            throw new IndexOutOfBoundsException(
                    "cannot open element, start not set as'<':array["
                            +start+
                            "] = "+
                            (char)backedUpArray[start]);
        }

backedUpArrayIndex = start;   
        int b;
        start++;
        while((b = readSingleByte()) == ' ' || b == '\n' || b == '\r'){
            if(b == '>'){ backedUpArrayIndex--; break; } 
            start++;
        }
        while((b = readSingleByte()) != ' ' && b != '\n' && b != '\r'){
            if(b == '>'){
                String name = getString(start,backedUpArrayIndex-(start+1));
                if(backedUpArray[start] == '!'){
                    if(name.equals("!DOCTYPE")){
                        return readDOCTYPE(start);
                    }
                    else if(name.indexOf("!--") == 0){
                        return readComment(start);
                    }
                }
                boolean autoclose = false;
                if(name.charAt(name.length()-1) == '/'){
                    name = name.substring(0, name.length()-1);
                    autoclose = true;
                }
                cursor.newElement(name, null,autoclose);//new String(backedUpArray, start, backedUpArrayIndex-(start+1)), null, false);
                shiftArray(backedUpArrayIndex);
                return 0;
            }

        }
        String elementName = getString(start, backedUpArrayIndex-start-1);//new String(backedUpArray, start, backedUpArrayIndex-start-1);
        
        if(elementName.equals("!DOCTYPE")){
            return readDOCTYPE(start);
        }
        else if(elementName.indexOf("!--") == 0){
            return readComment(start);
        }
        else if((char)backedUpArray[start] == '!'){
            throw new Exception("my oh my");
        }
        shiftArray(backedUpArrayIndex);
        //og.i("element ATTRIBUTE_name",elementName);
        //ArrayList<NameValuePair> attrs = new ArrayList<NameValuePair>();
        NameValuePairList attrs = new NameValuePairList(allAttributeAndTagNames);
        start = backedUpArrayIndex;
        boolean autoClose = false;
        while((b = readSingleByte()) !='>'){//find Attributes
            if(b == '/'){
                autoClose = true;
            }
            else if( !isWhiteChar(b) ){
                start = parseAttribute(start,attrs);
                b = backedUpArray[start];
                if(b == '/'){ autoClose = true; }
                if(b == '>'){ break; }
                //og.i("new attribute",attrs.get(attrs.size()-1).getName()+"__,__"+attrs.get(attrs.size()-1).getValue());
                shiftArray(start);//TODO check
                start = 0;
            }
        }
        //og.i("<"+elementName+">", attrs.toString());
        if(!elementName.equals("?xml")){
            cursor.newElement(elementName, attrs, autoClose);
        }
        else{
            //og.i("skipping", elementName+":"+attrs);
        }
      
        shiftArray(backedUpArrayIndex);
        return 0;
    }
    private int readComment(int start) throws Exception {
        String compare = "!--";
        backedUpArrayIndex = start;
        for(int i = 0; i<compare.length(); i++){//compare to doctype declaration
            if(compare.charAt(i) != readSingleByte()){ throw new Exception("doctype error:"+new String(backedUpArray, start, backedUpArrayIndex)); }
        }
        while(true){
            if(readSingleByte() == '-' && readSingleByte() == '-' && readSingleByte()=='>'){ break; }
        }
        //String retu = new String(backedUpArray, start, backedUpArrayIndex-start);
        shiftArray(backedUpArrayIndex);
        //throw new Exception("unfinished code:"+retu);
        return backedUpArrayIndex;
    }
    private int readDOCTYPE(int start) throws Exception {
        if(true){
            backedUpArrayIndex = start;
            char b = (char)readSingleByte();
            while(b !='>'){
                if(b == '"' || b == '\''){ 
                    b = (char) skipOverStringLiteral(backedUpArrayIndex-1);
                    b = (char) backedUpArray[backedUpArrayIndex];
                    if(b== '"'){ int i  = 1/0;}
                }
                else{ b = (char)readSingleByte(); }
            }
            readSingleByte();
            //og.i("doctype", new String(backedUpArray,start,backedUpArrayIndex));
            shiftArray(backedUpArrayIndex);
            //og.i("new backed", new String(backedUpArray,0,3));
            return backedUpArrayIndex;
        }
        return -100;
        //this all is now depreciated
        /**
        //og.("read", "doctype");
        String compare = "!DOCTYPE";
        backedUpArrayIndex = start;
        for(int i = 0; i<compare.length(); i++){//compare to doctype declaration
            if(compare.charAt(i) != readSingleByte()){ throw new Exception("doctype error:"+new String(backedUpArray, start, backedUpArrayIndex)); }
        }
        //og.i("correct doctype", new String(backedUpArray, start, backedUpArrayIndex-start));
        int b = readSingleByte();
        while(b == ' ' || b == '\n' ){ b = readSingleByte(); }//skip any white space
        while(b != '>'){
            if(b == '\'' || b == '"'){
                b = skipOverStringLiteral(backedUpArrayIndex-1); 
                //og.i("skip string literal", "byte:"+b);
            }
            else { b = readSingleByte(); }
        }
        //b = readSingleByte();
        String retu = new String(backedUpArray, start, backedUpArrayIndex-start);
        shiftArray(backedUpArrayIndex);
        return backedUpArrayIndex;
        **/
    }
    /**
     * call this function to actually start the parsing
     */
    public void read() throws Exception{
        long time = System.currentTimeMillis();
        backedUpArrayIndex = 0;
        int i = 0;
        try {
            int b = -1;
            while ((b = readSingleByte()) >=0) {
                //og.i("byte", Character.toString((char)b));
                if (b == '<') {
                    textElement(backedUpArrayIndex-1);
                    int nextByte = readSingleByte();
                    if (nextByte == '/') {
                        //depreciated_cursor_bad.textElement(new String(xmlData, textStart, i - textStart));
                        //i = closeElement(i);
                        closeElement(backedUpArrayIndex-2);
                    } else {
                        //i = openElement(i);
                        //textStart = i + 1;
                        openElement(backedUpArrayIndex-2);
                    }

                }

            }
            
        }
        catch(Exception e){
            in.close();
            throw e;
        }
        if(autoCloseStream){
            in.close();
        }
    }

    /**
     *
     * @param text
     * @param escape
     * @return the next readable character after the escape character or -1 for error.
     */
    private static int skipEscapeChar(byte[] text, int escape){
        if(text[escape] != '\\'){
        }
        escape++;
        if(escape>=text.length){
            return -1;
        }
        if(text[escape] == '\\'){

            return escape+1;
        }
        else{
            escape++;
            if(escape>=text.length){
                return -1;
            }
            return escape;
        }

    }
    /**
     *
     * @param String
     * @return returns last byte read in the string literal
     */
    private byte skipOverStringLiteral(int String) throws IOException {


        byte c;
        if(backedUpArray[String] == '"'){
            while(true){
                int b = readSingleByte();
                if(b == -1){
                    return -1;
                }
                if(b== '\\'){ //ecape characters must be skipped
                    int escape = readSingleByte();
                    if(escape == -1){	return -1; }
                    if(escape == '\\'){ escape = readSingleByte(); }
                    if(escape == -1){
                        return -1;
                    }
                }

                if(b == '"'){ break; }
            }
            return backedUpArray[backedUpArrayIndex];
        }
        else if(backedUpArray[String] == '\''){
            String++;
            while(true){
                int b = readSingleByte();
                if(b == -1){
                    return -1;
                }
                if(b== '\\'){ //ecape characters must be skipped
                    int escape = readSingleByte();
                    if(escape == -1){	return -1; }
                    if(escape == '\\'){ escape = readSingleByte(); }
                    if(escape == -1){
                        return -1;
                    }
                }

                if(b == '\''){ break; }
            }
            return backedUpArray[backedUpArrayIndex];
        }
        else{
            if(String-1>0){
                //og.("item before",""+(char)backedUpArray[String-1]);
            }
            throw new IndexOutOfBoundsException("not a string literal:"+(char)backedUpArray[String]);}

    }

    private static boolean isWhiteChar(int c){
        if(
                c == ' ' ||
                c == '\n' ||
                c == '\r' ||
                c == '\t'
        ){
            return true;
        }
        else{
            return false;
        }
    }

    private static int skipOverWhiteSpace(byte[] data, int index){

        while( isWhiteChar(data[index]) )
        {	index++;	}

        return index;
    }
 public static String getAttributeValue(List<NameValuePair> attributes, String rel) {
     for(int i = 0; i<attributes.size(); i++){
         if(attributes.get(i).getName().equals(rel)){
             return attributes.get(i).getValue();
         }
     }
     return null;
 }
}
