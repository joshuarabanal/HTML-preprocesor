package xml.html;

import android.support.annotation.Nullable;

import java.io.InputStream;
import java.util.ArrayList;

import xml.NameValuePairList;
import xml.unoptimized.NameValuePair;
import xml.Optimized.OptimizedParser;
import xml.Optimized.OptimizedXmlCursor;

public class HTMLparser  extends OptimizedParser {
    public HTMLparser(InputStream is, OptimizedXmlCursor curs, @Nullable String[] allStrings) {
        super(is, curs, allStrings);
    }

    protected void newElement(int start, int end, NameValuePairList attrs, boolean autoClose) throws Exception {
        int name = getStringIndex(start, backedUpArrayIndex - (start +2));
        cursor.newElement(name, null, autoClose);
        if(allNameValuePairAndTagNameStrings[name].equals("script")){
            throw new UnsupportedOperationException("this class is not yet finished, if found in the far future, delete it");
        }

    }
}
