package xml.Optimized;

import java.util.List;

import xml.NameValuePairList;

/**
 * Created by ra on 29/05/2017.
 */

public interface OptimizedXmlCursor {
    public void newElement(float name, NameValuePairList attributes, boolean autoClose) throws Exception;
    public void closeElement(float name) throws Exception;
    public void textElement(String text);
}
