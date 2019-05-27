package xml.unoptimized;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.util.Log;
import xml.XmlCursor;

public class HtmlParser extends Parser2 {
	

	public HtmlParser(File f, XmlCursor curs, String[] allStrings) throws FileNotFoundException {
		super(f, curs, allStrings);
		// TODO Auto-generated constructor stub
		this.isHTML = true;
	}

	public HtmlParser(InputStream is, XmlCursor curs, String[] allStrings) {
		super(is, curs, allStrings);
		// TODO Auto-generated constructor stub
		this.isHTML = true;
	}
	@Override
	protected void handleScriptTag(int start) throws Exception {
		// TODO Auto-generated method stub
		//Log.i("current shifted cursor", new String(backedUpArray, backedUpArrayIndex, backedUpArray.length-backedUpArrayIndex));
		//Log.i("current cursor", new String(backedUpArray, 0, backedUpArray.length));
		//Log.i("start cursor", new String (backedUpArray,start, backedUpArray.length-start));
		int originalStart = backedUpArrayIndex;
		int a='-';
		
		while(
				(a = readSingleByte()) != '<' ||
				(a = readSingleByte()) != '/' ||
				(a = readSingleByte()) != 's' ||
				(a = readSingleByte()) != 'c' ||
				(a = readSingleByte()) != 'r' ||
				(a = readSingleByte()) != 'i' ||
				(a = readSingleByte()) != 'p' ||
				(a = readSingleByte()) != 't' ||
				(a = readSingleByte()) != '>' 
		) {
			
			if(a =='\'' || a == '"') {
				int oldIndex = backedUpArrayIndex-1;
				int retuChar = skipOverStringLiteral(oldIndex);
			}
		}
		closeElement(backedUpArrayIndex-7, 6);
			
	}

}
