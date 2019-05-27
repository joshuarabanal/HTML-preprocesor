package xml.Optimized;

import android.support.annotation.Nullable;
import android.util.Log;



import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.MalformedInputException;
import java.util.ArrayList;

import Analytics.CrashReporter;
import xml.NameValuePairList;


public class OptimizedParser {
    private BufferedInputStream in;
    protected OptimizedXmlCursor cursor;
    private ArrayList<String> parentHirarchy;
    protected byte[] backedUpArray = new byte[200];
    protected int backedUpArrayIndex = 0;
    protected String[] allNameValuePairAndTagNameStrings;
    private byte[][] allNameValuePairAndTagNameBytes;

    public OptimizedParser(File f, OptimizedXmlCursor curs, @Nullable String[] allStrings)throws FileNotFoundException{
        initializeStrings(allStrings);
            in = new BufferedInputStream(new FileInputStream(f));
        cursor = curs;
        parentHirarchy = new ArrayList<String>();
    }
    public OptimizedParser(InputStream is, OptimizedXmlCursor curs, @Nullable String[] allStrings){
        initializeStrings(allStrings);
        in = new BufferedInputStream(is);
        cursor = curs;
        parentHirarchy = new ArrayList<String>();
    }
    private void initializeStrings(String[] strings){
        if(strings == null){
            allNameValuePairAndTagNameBytes = new byte[0][];
            allNameValuePairAndTagNameStrings = new String[0];
            return;
        }
        allNameValuePairAndTagNameStrings = strings;
        allNameValuePairAndTagNameBytes = new byte[strings.length][];
        for(int i = 0; i<allNameValuePairAndTagNameStrings.length; i++){
            allNameValuePairAndTagNameBytes[i] = allNameValuePairAndTagNameStrings[i].getBytes();
        }

    }


    //cursor functions
    protected void textElement(int end) throws IOException {
        int start = 0;  
        while(isWhiteChar(backedUpArray[start])){ 
            start++; 
        }
        int stop = end; 
        while(isWhiteChar(backedUpArray[stop])&& stop>=start){ 
            stop--; 
        }

        if(start <stop){
            cursor.textElement(new String(backedUpArray,start,(stop-start)+1));
        }
        //og.i("text element", "end = "+end+":"+new String(backedUpArray,0,end));
        shiftArray(end);
    }
    protected void closeElement(int nameStart, int length) throws Exception {
        cursor.closeElement(getStringIndex(nameStart, length));
    }
    protected void newElement(int start, int length, NameValuePairList attrs, boolean autoClose ) throws Exception {
        int name = getStringIndex(start, length);
        cursor.newElement(name, attrs, autoClose);


    }



    private int backedUpEndIndex = 0;
    /**
     * 
     * @return int representation of the byte read
     * @throws IOException
     */
    protected int readSingleByte() throws IOException {
        if(backedUpArrayIndex+1>=backedUpArray.length){//if the array needs to grow
            byte[] newArray = new byte[backedUpArray.length+200];
            for(int i = 0; i<backedUpArray.length; i++){
                newArray[i] = backedUpArray[i];
            }
            backedUpArray = newArray;
        }

        if(backedUpArrayIndex >= backedUpEndIndex ){
            int howMany = in.read(backedUpArray, backedUpEndIndex,backedUpArray.length- backedUpEndIndex);
            if(howMany<0){
                return -1;
            }
            backedUpEndIndex+=howMany;
        }

        return 0xff & backedUpArray[backedUpArrayIndex++];
    }
    protected void shiftArray(int newZero) throws IOException {
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

    private boolean isNumber(byte b){
        int val = b - '0';
        if(val>-1 && val<10){ return true; }
        else if(val == -3){ return true; }
        return false;
    }
    private String getString(int start, int length){

        byte[] string;
        for(int s = 0; s<allNameValuePairAndTagNameStrings.length; s++){//find string in predetermined strings
            string = allNameValuePairAndTagNameBytes[s];
            if(string.length != length){ continue; }
            else{
                boolean goodString = true;
                for(int i = 0; i<length; i++){
                    if(backedUpArray[start+i] != string[i]){  goodString = false; break; }
                }
                if(goodString){ return allNameValuePairAndTagNameStrings[s]; }
            }
        }

        return new String(backedUpArray,start,length);
    }
    protected int getStringIndex(int start, int length) throws IOException {

            byte[] s;
            for(int string = 0; string< allNameValuePairAndTagNameBytes.length; string++){//find string in predetermined strings

                s = allNameValuePairAndTagNameBytes[string];
                if(s.length != length){ continue; }
                else{
                    boolean goodString = true;
                    for(int i = 0; i<length; i++){
                        if(backedUpArray[start+i] != s[i]){  goodString = false; break; }
                    }
                    if(goodString){ return string; }
                }
            }



        throw new IOException(
                "failed to find string in the backed up strings:'"+
                        new String(backedUpArray,start,length)+
                        "'\n, ATTRIBUTE_length:"+length+
                        "\n"+new String(backedUpArray,0, backedUpArray.length)+
                        "\nvisible region:"+new String(backedUpArray, 0, backedUpArrayIndex)
        );
    }
    private void closeElement(int start) throws Exception{
        if(backedUpArray[start] != '<' || backedUpArray[start+1] != '/'){
            throw new IndexOutOfBoundsException(
                    "cannot close element:"+
                            "array["+start+"] = "+(char)backedUpArray[start]+","+
                            "array["+start+"+1] = "+(char)backedUpArray[start+1]
            );
        }
            start+=2; //start is now first char of elem name
            
        char c;
        backedUpArrayIndex = start;
        while((c = (char) readSingleByte()) != '>'){
            //Log.i("read char", ((char)backedUpArray[backedUpArrayIndex])+"=="+c );
        }


        //float name = getStringIndex(start, backedUpArrayIndex-(start+1) );//new String(backedUpArray,start, backedUpArrayIndex-(start+1));
        try {
            closeElement(start, backedUpArrayIndex-(start+1) );
        }catch (Exception e){
            CrashReporter.log("backed up array index:'"+((char)backedUpArray[backedUpArrayIndex])+"'" );
            CrashReporter.log("exception thrown at:"+new String(backedUpArray,0, backedUpArrayIndex+1));
            CrashReporter.log("backed up array:\n"+new String(backedUpArray));
            throw e;
        }
        //og.i("close element",ATTRIBUTE_name);
        shiftArray(backedUpArrayIndex);

    }
    private int parseNameValuePair(int start, NameValuePairList attrs) throws IOException {
        //NameValuePair retu = new NameValuePair();
        int nameStart,nameEnd, valueStart, valueEnd;
        //og.i("parse NameValuePair","bytes["+start+"] = "+(char)backedUpArray[start]);
        int b = backedUpArray[start];
        while(b == ' '){ start++; b =  readSingleByte(); }//skip white space
        while((b = readSingleByte()) != ' ' && b != '='){ }
        if(backedUpArray[start] == ' '){ throw new IndexOutOfBoundsException("needs to start with non white space");}
        nameStart = start;
        nameEnd = backedUpArrayIndex-start-1;
        //retu.setName( getStringIndex(start,backedUpArrayIndex-start-1));//new String(backedUpArray,start, backedUpArrayIndex-start-1));

        if(b != '='){
            while((b = readSingleByte())!='='){}
            b = readSingleByte();
            //og.("moving to value", ""+((char)b));
        }
        else{
            b = readSingleByte();
        }

        while ( (b) ==' '){b = readSingleByte();  }
        start = backedUpArrayIndex-1;
        //og.i("values start", ""+(char)backedUpArray[start]);
        if(b == '\'' || b == '"'){
            skipOverStringLiteral(start);
            start++;
            valueStart = start; valueEnd = backedUpArrayIndex-(start+1);
            //retu.setValue( getString(start, backedUpArrayIndex-(start+1)));
            //attrs.add(retu);
            attrs.add(backedUpArray, nameStart,nameEnd, valueStart, valueEnd);
            readSingleByte();
            return backedUpArrayIndex -1;
        }
        while( (b = readSingleByte()) != ' ' && b != '>' && b !='/'){}
        //og.i("set value", new String(backedUpArray,start, backedUpArrayIndex-(start+1)));
        valueStart = start; valueEnd = backedUpArrayIndex-(start+1);
        //retu.setValue(getString(start, backedUpArrayIndex-(start+1)));
        //og.i("return char","'"+(char)b+"'");
        attrs.add(backedUpArray, nameStart,nameEnd, valueStart, valueEnd);
        //attrs.add(retu);
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
        while((b = readSingleByte()) == ' ' || b == '\n' || b == '\r'){ start++;}
        if(b!='>'){
            while((b = readSingleByte()) != ' ' && b != '\n' && b != '\r'){
                if(b == '>'){
                    if(backedUpArray[start] == '!'){
                        throw new IndexOutOfBoundsException("comments and doctypes cannot be called here se read for correct calls");
                    }
                    if(backedUpArray[backedUpArrayIndex-2] == '/'){//auto close
                        newElement(start, backedUpArrayIndex - (start +2), null, true);
                        //cursor.newElement(getStringIndex(start, backedUpArrayIndex - (start +2)), null, true);
                    }
                    else{//here
                        newElement(start, backedUpArrayIndex - (start +1), null, true);
                        //cursor.newElement(getStringIndex(start, backedUpArrayIndex - (start + 1)), null, false);
                    }
                    shiftArray(backedUpArrayIndex);
                    return 0;
                }

            }
        }
        if(backedUpArray[start] == '!'){// no more comments or doctypes can be called within new element
            
                throw new IndexOutOfBoundsException("this function should no longer be called here");
                //return readComment(start);
         
        }
        if(
                backedUpArray[start] == '?' &&
                backedUpArray[start+1] == 'x' &&
                backedUpArray[start+2] == 'm' &&
                backedUpArray[start+3] == 'l'
        ){
            return readXmlDocType(start);
        }
        int elemNameStart = start;
        int elemNameEnd = backedUpArrayIndex;
        if( backedUpArray[backedUpArrayIndex-1] == ' ' ||backedUpArray[backedUpArrayIndex-1] == '\n' || backedUpArray[backedUpArrayIndex-1] == '\r'){
            elemNameEnd--;
        }
        //int elementName = getStringIndex(start, backedUpArrayIndex-start-1);//new String(backedUpArray, start, backedUpArrayIndex-start-1);
        //og.i("element ATTRIBUTE_name",elementName);
        //NameValuePairList attrs = new NameValuePairList();
        NameValuePairList attrs = new NameValuePairList(allNameValuePairAndTagNameStrings);
        start = backedUpArrayIndex-1;
        boolean autoClose = false;
        while((b = readSingleByte()) !='>'){//find NameValuePairs
            if(b == '/'){
                autoClose = true;
            }
            else if( !isWhiteChar(b) ){
                start = parseNameValuePair(backedUpArrayIndex-1,attrs);
                b = backedUpArray[backedUpArrayIndex-1];
                if(b == '/'){ autoClose = true; }
                if(b == '>'){ break; }
            }
        }
        //og.i("<"+elementName+">", attrs.toString());
            newElement(elemNameStart, elemNameEnd-elemNameStart, attrs, autoClose);
            //cursor.newElement(elementName, attrs, autoClose);

        //og.i("finished open elem", ""+(char)+backedUpArray[backedUpArrayIndex-1]);
        shiftArray(backedUpArrayIndex);
        return 0;
    }
    private int readXmlDocType(int start) throws IOException {
        backedUpArrayIndex = start;
        if(
                readSingleByte()  != '?' ||
                readSingleByte()  != 'x' ||
                readSingleByte()  != 'm' ||
                readSingleByte()  != 'l'
        ){
            throw new IndexOutOfBoundsException("not an xml doctype");
        }
        while(true){
                    start++;
                    if(readSingleByte() =='?' && readSingleByte()  == '>'){
                        shiftArray(backedUpArrayIndex);
                        return backedUpArrayIndex;
                    }
        }
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
    private int readDOCTYPE(int start)throws Exception {
        String compare = "!DOCTYPE";
        backedUpArrayIndex = start;
        char c;
        for(int i = 0; i<compare.length(); i++){//compare to doctype declaration
            if(compare.charAt(i) != (c = (char) readSingleByte())){
                CrashReporter.log("comparing chars:"+compare.charAt(i)+"=="+c);
                throw new Exception("doctype error:"+new String(backedUpArray, start, backedUpArrayIndex));
            }
        }
        //og.i("correct doctype", new String(backedUpArray, start, backedUpArrayIndex-start));
        int b = readSingleByte();
        while(b == ' ' || b == '\n' ){ b = readSingleByte(); }//skip any white space
        while(b != '>'){
            if(b == '\'' || b == '"'){
                b = skipOverStringLiteral(backedUpArrayIndex-1);
            }
            else { b = readSingleByte(); }
        }
        //b = readSingleByte();
        //String retu = new String(backedUpArray, start, backedUpArrayIndex-start);
        shiftArray(backedUpArrayIndex);
        return backedUpArrayIndex;
    }
    /**
     * call this function to actually start the parsing
     */
    public void read() throws Exception{
        long time = System.currentTimeMillis();
        backedUpArrayIndex = 0;
        
        try {
            int textStart = -1;
            int b = -1;
            while ((b = readSingleByte()) >=0) {
                //og.i("byte", Character.toString((char)b));
                if (b == '<') {
                    if(backedUpArrayIndex > 1){// if this is not the first time calling the loop and there is text to place in the elements
                        textElement(backedUpArrayIndex-2);
                    }
                    int nextByte = readSingleByte();
                    if (nextByte == '/') {
                        //depreciated_cursor_bad.textElement(new String(xmlData, textStart, i - textStart));
                        //i = closeElement(i);
                        closeElement(backedUpArrayIndex-2);
                    }
                    else if(nextByte =='!'){
                        int origIndex = backedUpArrayIndex;
                        int determiningChar = readSingleByte();
                        int nextDeterminingChar = readSingleByte();
                        if(determiningChar == '-' && nextDeterminingChar == '-'){ //comment <!--
                            //this is a comment
                            //Log.i("staring comment find", new String(backedUpArray,0,backedUpArrayIndex));
                            while(
                                    (readSingleByte() != '-' || readSingleByte() != '-' || readSingleByte() != '>')
                            ){
                                //Log.i("backed up buffer", new String(backedUpArray,0,backedUpArrayIndex+1));
                            }
                            //CrashReporter.log("backed up buffer:"+new String(backedUpArray));
                            //CrashReporter.log("stream:"+in.toString());
                            //CrashReporter.log("reading index:"+backedUpArrayIndex);
                            
                            if(
                                    backedUpArray[backedUpArrayIndex-1] != '>' || backedUpArray[backedUpArrayIndex-2] != '-' || backedUpArray[backedUpArrayIndex-3] != '-'
                            ){
                                CrashReporter.log(
                                    "possible coment:"+new String(backedUpArray,0,backedUpArrayIndex) 
                                );
                                CrashReporter.log("char1:"+ ((char)backedUpArray[backedUpArrayIndex]) );
                                CrashReporter.log("read test"+((char)readSingleByte())+"=="+((char)backedUpArray[backedUpArrayIndex]));
                                throw new IndexOutOfBoundsException("failed to get comment");
                            }
                            //Log.i("new text element", new String(backedUpArray,0,backedUpArrayIndex));
                            textElement(backedUpArrayIndex-1);
                            //throw new IndexOutOfBoundsException("dont know what is next");
                        }
                        else if(
                            determiningChar == 'D' && nextDeterminingChar == 'O'
                        ){ // <!DOCTYPE
                            if(
                                    readSingleByte() == 'C' &&
                                    readSingleByte() == 'T' &&
                                    readSingleByte() == 'Y' &&
                                    readSingleByte() == 'P' &&
                                    readSingleByte() == 'E'
                            ){
                                readDOCTYPE(backedUpArrayIndex-8);
                            }
                            else{
                                CrashReporter.log("current index:"+((char)backedUpArray[backedUpArrayIndex]));
                                CrashReporter.log("buffer so far:"+new String(backedUpArray, 0, backedUpArrayIndex+1));
                                throw new IndexOutOfBoundsException("doctype read error");
                            }
                        }
                        else if(determiningChar == '[' && nextDeterminingChar == 'C'){// comment <![CDATA
                            if(
                                    readSingleByte() == 'D' &&
                                            readSingleByte() == 'A' &&
                                            readSingleByte() == 'T' &&
                                            readSingleByte() == 'A'
                            ) {
                                while (
                                        (readSingleByte() != ']' && readSingleByte() != '>' )
                                ) {

                                }
                                textElement(backedUpArrayIndex);
                            }
                        }
                        else{
                            CrashReporter.log("buffer:"+new String(backedUpArray));
                            throw new IndexOutOfBoundsException("unknown element");
                        }
                    }
                    else {
                        //i = openElement(i);
                        //textStart = i + 1;
                        openElement(backedUpArrayIndex-2);
                    }

                }
            }
        }
        catch(Exception e){
            throw e;
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
     * @param String the first character index of the string
     * @return returns last byte read in the string literal
     */
    protected byte skipOverStringLiteral(int String) throws IOException {

    	backedUpArrayIndex = String;
    	
        int c = readSingleByte();
        if(c == '"'){
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
        else if(c  == '\''){
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
        
        else {
        	Log.i("char", ""+((char)backedUpArray[String]));
        	Log.i("parsing section", new String(backedUpArray, String, backedUpArray.length-String));
        	throw new IndexOutOfBoundsException("not a string literal");
        }

    }

    protected static boolean isWhiteChar(int c){
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

}
