package JSON;

import java.nio.charset.MalformedInputException;
import java.util.ArrayList;

public class JSONObject {
	private ArrayList<String> paramKeys;
	private ArrayList<JSONObject> paramValues;
	private String value;
	
	public JSONObject(String value) {
		this.value = value;
	}
	
	public JSONObject() {
		paramKeys = new ArrayList<>();
		paramValues = new ArrayList<>();
	}
	
	public JSONObject(ArrayList<JSONObject> items) {
		this();
		for(int i = 0; i<items.size(); i++) {
			addParam(""+i, items.get(i));
		}
	}

	public void addParam(String name, JSONObject value) {
		if(get(name) != null) { throw new IndexOutOfBoundsException("duplicate entrys"); }
		if(paramKeys.size() != paramValues.size()) { throw new IndexOutOfBoundsException("internal error"); }
		else {
			paramKeys.add(name);
			paramValues.add(value);
		}
	}
	
	/**
	 * 
	 * @param paramName
	 * @return null if no object with that key name exists, or the object with that key name
	 */
	public JSONObject get(String paramName) {
		for(int i = 0; i<paramKeys.size(); i++) {
			if(paramKeys.get(i).equals(paramName)) {
				return paramValues.get(i);
			}
		}
		return null;
	}

}
