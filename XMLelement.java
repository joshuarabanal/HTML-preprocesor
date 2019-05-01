package xml;


/**
 * Created by ra on 08/06/2017.
 */

public interface XMLelement {

    public void addChild(XMLelement f);
    public void closeElement() throws Exception;
}
