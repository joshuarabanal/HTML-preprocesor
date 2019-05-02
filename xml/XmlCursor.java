package xml;



import android.support.annotation.Nullable;

import java.util.List;

import xml.unoptimized.NameValuePair;


public interface XmlCursor {
	
	public void newElement(String name, @Nullable NameValuePairList attributes, boolean autoClose) throws Exception;
	public void closeElement(String name) throws Exception;
	public void textElement(String text);

}
