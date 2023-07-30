package org.example;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.Map;

public class JSONToMapConverter {
	public static Map<String, String> convertJSONToMap(String jsonString) throws ParseException {
		Map<String, String> resultMap = new HashMap<>();

		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject) parser.parse(jsonString);

		// Iterate over the JSONObject and add key-value pairs to the resultMap
		for (Object key : jsonObject.keySet()) {
			String strKey = (String) key;
			String strValue = (String) jsonObject.get(key);
			resultMap.put(strKey, strValue);
		}

		return resultMap;
	}
}
