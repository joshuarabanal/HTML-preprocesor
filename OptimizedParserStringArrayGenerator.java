package xml;


import android.support.annotation.Nullable;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import xml.unoptimized.NameValuePair;
import xml.unoptimized.Parser;

/**
 * Created by ra on 13/07/2017.
 * this class can be used to create a string array file to be used in the optimized xmlparser<br>
 *     <ol>
 *         <li> create a sample xml file that contains the most of the tag names and attributes as possible</li>
 *         <li>feed the flile into this generator using {@link #OptimizedParserStringArrayGenerator(InputStream, OutputStream, String[])} </li>
 *         <li> call {@link #read()} on your new generator and {@link #close()}</li>
 *         <li> the output file will be in the form of an android strin-array resource file with the name "output"</li>
 *         <li> if you were not able to get all of the attributes and tag names in one file you may use the output as a starting place to repeate steps 2-4</li>
 *     </ol>
 *     all that needs to be done is a sample xml file must be created first
 */

public class OptimizedParserStringArrayGenerator implements XmlCursor {
    private ArrayList<String> strings;
    private Parser p;
    private OutputStream out;

    /**
     *
     * @param xmlFileToRead the sample xml file containing attributes and tag names you want to parse
     * @param whereToSaveTheNewStringArray output file location
     * @param startingPlace a list of tags and attribute names you would like the parser to start out with (this can be null)
     */
    public OptimizedParserStringArrayGenerator(InputStream xmlFileToRead, OutputStream whereToSaveTheNewStringArray,@Nullable String[] startingPlace){
        out = whereToSaveTheNewStringArray;
        strings = new ArrayList<String>();
        if(startingPlace != null) {
            for (int i = 0; i < startingPlace.length; i++) {
                addString(startingPlace[i]);
            }
        }
        p = new Parser(xmlFileToRead,this,startingPlace);

    }
    public void read() throws Exception {
        p.read();
    }
    public void close(){
        PrintWriter p = new PrintWriter(out);
        p.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        p.println("<resources xmlns:tools=\"http://schemas.android.com/tools\" tools:ignore=\"MissingTranslation\">");
        p.println("<string-array name=\"music_xml_all_tag_names_and_attributes\">");
        p.println("<item>\\?xml</item>");
        for(int i = 0; i<strings.size(); i++){
            p.println(
                    "<item>"+strings.get(i)+"</item>"+
                    "<!-- "+i+" -->"
            );
        }
        p.println("</string-array>");
        p.println("</resources>");
        p.close();
    }
    private void addString(String s){
        if(s.equals("?xml") ){ return; }
        int slength = s.length();
        for(int i = 0; i<strings.size(); i++){
            if(strings.get(i).equals(s)) {
                return;
            }
        }
        strings.add(s);
    }

    @Override
    public void newElement(String name, NameValuePairList attributes, boolean autoClose) throws Exception {
        if(name.charAt(0) == '!'){ throw new Exception("no comments allowed"); }
        /**
        if(autoClose){
            if(attributes == null){
                Log.w("open element", "<"+name+"/>");
            }
            else{
                Log.w("open element", "<"+name+ " "+attributes.toString()+"/>");
            }
        }
        else {
            if(attributes !=null){
                Log.w("open element", "<"+name+ " "+attributes.toString()+">");
            }
            else{
                Log.w("open element", "<"+name+ ">");
            }
        }
        **/

            addString(name);
            if(attributes!= null){
                for(int i = 0; i<attributes.size(); i++){
                    addString(attributes.get(i).getName());
                }
            }
    }

    @Override
    public void closeElement(String name) throws Exception {
        //og.i("close element", "</"+name+">");
        addString(name);
    }

    @Override
    public void textElement(String text) {
        //og.i("text element", text);
    }
}
