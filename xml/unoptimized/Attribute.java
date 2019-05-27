package xml.unoptimized;


import xml.unoptimized.NameValuePair;

public class Attribute implements NameValuePair {
	private String name;
	private String value;
        public Attribute(String name, String value){
            this();
            setName(name);
            if(value != null && value.length()>0) {
            	setValue(value);
            }
        }
	public Attribute(){
		
	}
	public void setName(String name){
		if(
				name.contains("!")||
				name.contains("=")||
				name.contains("<")||
				name.contains(">")||
                                name.indexOf("\"") == 0
			){
			throw new IndexOutOfBoundsException("ATTRIBUTE_name contains bad character: "+name);
		}
		this.name = name.trim();
	}
	public void setValue(String val){
		val = val.trim();
		if(
				(val.charAt(0) == '"' && val.charAt(val.length()-1) == '"')
				||
				(val.charAt(0) =='\'' && val.charAt(val.length()-1) == '\'')
		){
			val = val.substring(1,val.length()-1);
		}
		this.value = val;
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public int getNameIndex() {
		throw new UnsupportedOperationException("not in an unoptimized environment");
	}

	@Override
	public String getValue() {
		// TODO Auto-generated method stub
		return value;
	}

	@Override
	public int getValueIndex() {
		throw new UnsupportedOperationException("not in an unoptimized environment");
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return " "+name+"="+value+" ";
	}

}
