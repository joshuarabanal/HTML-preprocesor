package xml.unoptimized;

import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import Analytics.CrashReporter;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import xml.NameValuePairList;
import xml.unoptimized.NameValuePair;
import xml.Optimized.OptimizedXmlCursor;
import xml.XmlCursor;

public class Parser2 extends xml.Optimized.OptimizedParser {
    private XmlCursor cursorUnoptimized;

    public Parser2 (File f, XmlCursor curs, @Nullable String[] allStrings) throws FileNotFoundException{
        super(new FileInputStream(f), null, allStrings);
        this.cursorUnoptimized = curs;
    }
    public Parser2(InputStream is, XmlCursor curs, @Nullable String[] allStrings) {
        super(is, null, allStrings);
        this.cursorUnoptimized = curs;
    }

    //cursor functions
    @Override
    protected void textElement(int end) throws IOException {
        int start = 0;  
        while(isWhiteChar(backedUpArray[start])){ 
            //Log.i("shifting start", ""+((char)backedUpArray[start]) );
            start++; 
        }
        int stop = end; 
        while(isWhiteChar(backedUpArray[stop])&& stop>=start){ 
            //Log.i("shifting stop", "'"+((char)backedUpArray[stop])+"', is white char:"+isWhiteChar(backedUpArray[stop]) );
            stop--; 
        }

        if(start <stop){
            cursorUnoptimized.textElement(new String(backedUpArray,start,(stop-start)+1));
        }
        //og.i("text element", "end = "+end+":"+new String(backedUpArray,0,end));
        shiftArray(end);
    }

    @Override
    protected void closeElement(int start, int length) throws Exception {
        if(length<=0){
            CrashReporter.log("string length:"+(length));
            CrashReporter.log(
                    "start:"+((char)backedUpArray[start])+", stop:"+((char)backedUpArray[start+length]+", backed up index:"+((char)backedUpArray[backedUpArrayIndex]))
            );            
            throw new IndexOutOfBoundsException("length<=0");
        }
        String name = new String(backedUpArray, start, length);
        if(name.length() ==0 || name.contains(">") || name.contains("<") || name.contains(" ") || name.contains("\n") || name.contains("\r")){
            CrashReporter.log("elem name:"+name);
            throw new IndexOutOfBoundsException("malformed close elem");
        }
        cursorUnoptimized.closeElement(
                name
        );
    }
    @Override
    protected void newElement(int start, int length, NameValuePairList attrs, boolean autoClose ) throws Exception {
        //int name = getStringIndex(start, backedUpArrayIndex - (start +2));
        if(length == 0){
            throw new IndexOutOfBoundsException(new String(backedUpArray,start,1));
        }
       
        
       String name = new String(backedUpArray, start, length );
        if( name.contains(">") || name.contains("<") || name.contains(" ") || name.contains("\n") || name.contains("\r") ){
            CrashReporter.log("new elem name:"+name);
            throw new IndexOutOfBoundsException();
        }
        cursorUnoptimized.newElement(name, attrs, autoClose);
    }
}
