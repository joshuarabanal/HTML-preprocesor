package JSON;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.util.Log;

public class JsonParser {
	private InputStream in;
	private String[] tags;

	public JsonParser(InputStream in, String[] knownTagsandValues) {
		this.in = in;
		this.tags = knownTagsandValues;
	}
	public JSONObject parse() throws IOException {
		int reading;
		while((reading = in.read()) != '{' && reading != -1) {
			Log.i("read char", ""+((char)reading) );
		}
		return parseObject();
	}
	
	private JSONObject parseObject() throws IOException {
		int reading;
		while((reading = in.read()) != -1) {
			if(reading == '{') {//new object
				return newObject();
			}
			else if(reading == '[') {//new array
				return newArray();
			}
			else if(reading == '"') {//new string
				return new JSONObject(getString('"'));
				
			}
			else if(reading == '\'') {//new String
				return new JSONObject(getString('\''));
			}
		}
		throw new IndexOutOfBoundsException("failed to find object");
	}
	private JSONObject newObject() throws IOException {
		int reading;
		String name = null;
		JSONObject  retu = null;
		while((reading = in.read()) != -1) {
			if(reading == '\'' || reading == '"') {
				String string = (reading == '"'? getString('"') : getString('\"') );
				if(name == null) { name = string; }
				else{
					if(retu == null) { retu = new JSONObject(); }
					retu.addParam(name, new JSONObject(string));
				}
			}
			else if( reading == '{' ) {
				JSONObject value = newObject();
				if(name == null) { throw new NullPointerException("param without name"); }
				retu.addParam(name, value);
			}
			else if( reading == '}') {
				if(retu == null && name != null) {
					retu = new JSONObject(name);
				}
				if(retu == null) {
					throw new NullPointerException("malformed Object");
				}
				return retu;
			}
		}
		throw new NullPointerException("malformed object error");
		
		
	}
	private JSONObject newArray() throws IOException {
		int reading;
		ArrayList<JSONObject> items = new ArrayList<>();
		
		while((reading = in.read()) != ']') {
			if(reading == '"') {//new String Object
				String string = getString('"');
				items.add(new JSONObject(string));
			}
			else if(reading == '{') {//new general Object
				items.add(newObject());
			}
			else if(reading == '[') {
				items.add(newArray());
			}
			else if(reading == ']') {
				return new JSONObject(items);
			}
		}
		throw new NullPointerException("malformed array error");
	}
	
	private String getString(char endDelimiter) throws IOException {
		StringBuilder sb = new StringBuilder();
		int reading;
		while( (reading = in.read()) != endDelimiter ) {
			sb.append((char) reading);
			if(reading == '\\'){//jump over string literals
				sb.append((char)in.read());
			}
					
		}
		return sb.toString();
	}
	
	
	public static void main(String[] args) throws Exception {
		File file = new File("");
		JsonParser jsp = new JsonParser(new FileInputStream(file), new String[] {});
		JSONObject jso = jsp.parse();
		Log.i("JSON Object", ""+jso);
	}
}
