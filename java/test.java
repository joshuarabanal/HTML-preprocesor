
import java.io.File;

import android.util.Log;
import xml.NameValuePairList;
import xml.XmlCursor;
import xml.unoptimized.Parser2;

public class test {

	
	public static void main(String[] args) throws Exception {
		File f = new File("C:\\Users\\Joshua\\Google Drive\\program stuff\\music xml\\website\\PublicFilesDirectory\\index.html");
		String[] wellKnownTags = {
		        "html", 
		        "div","h1","h2", "h3", "link",//5
		        "float", "id", "style", "href", "type",//10
		        "rel", "stylesheet","id","a", "span","p", "class"
		    };
		Parser2 p = new Parser2(f, new XmlCursor() {

			@Override
			public void newElement(String name, NameValuePairList attributes, boolean autoClose) throws Exception {
				// TODO Auto-generated method stub
				
				Log.i("open ", name+":"+attributes);
				if(autoClose) {
					Log.i("close", name);
				}
			}

			@Override
			public void closeElement(String name) throws Exception {
				// TODO Auto-generated method stub
				Log.i("close", name);
				
			}

			@Override
			public void textElement(String text) {
				// TODO Auto-generated method stub
				Log.i("text", text);
				
			}
			
		}, wellKnownTags );
        p.read();
	}
}
