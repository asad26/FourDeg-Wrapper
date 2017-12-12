/*
 * Created by Asad Javed on 28/08/2017
 * Aalto University project
 * 
 * Last modified 04/10/2017
 */

package fi.aalto.fourdeg.Wrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FourDegData {

	private static ApiForOmi omiObject;
	private static String omiURL;
	public int count;

	/* Constructor for this class */
	public FourDegData(Properties prop) {
		omiObject = new ApiForOmi();
		omiURL = prop.getProperty("omi_node");
		count = 0;
	}

	/* Get JSON array index one at a time and send for parsing to create ODF */
	public void parseArray(String st) throws JSONException {
		JSONArray jArray = new JSONArray(st);
		for (int i = 0; i < jArray.length(); i++) {
			if (jArray.get(i) instanceof JSONObject) {
				createCompleteOdf((JSONObject) jArray.get(i));
				count ++;
			}
		}
	}

	/* Get items from fourdeg JSON array, create ODF object, and send to the sand box one at a time */
	private static void createCompleteOdf(JSONObject jObject) {
		String infoItems = "";
		String objectId = null;
		String pubDate = null;
		String MAC = null;

		try {
			pubDate = jObject.getString("last_communication");
			Iterator<?> iterator = jObject.keys();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();

				if (key.equals("name")) {
					objectId = jObject.getString(key).replace(" ", "");
					objectId.replace(" ", "");
				}
				else if (key.equals("mac")){
					String newKey="MAC";
					infoItems = infoItems + omiObject.createInfoItem(newKey, jObject.getString(key), pubDate);
					MAC = jObject.getString(key);
				}
				else if (key.equals("current_set_point")){
					String newKey="Set-Point";
					infoItems = infoItems + omiObject.createInfoItem(newKey, jObject.getString(key), pubDate);
				}
				else if (key.equals("current_temperature")){
					String newKey="Temperature";
					infoItems = infoItems + omiObject.createInfoItem(newKey, jObject.getString(key), pubDate);
				}
				else if (key.equals("current_valve_position")){
					String newKey="Valve-Position";
					infoItems = infoItems + omiObject.createInfoItem(newKey, jObject.getString(key), pubDate);
				}
				else if (key.equals("current_battery_remaining")){
					String newKey="Battery-Remaining";
					infoItems = infoItems + omiObject.createInfoItem(newKey, jObject.getString(key), pubDate);
				}
				else {}

			}
		} catch (JSONException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}

		String URL = "http://localhost:8080/Objects/Otakaari-3/Thermostats/" + objectId + "/MAC/value";
		String macValue = getMacValue(URL);
		System.out.println(macValue);

//		if (!macValue.equals(MAC)) {	// Checks if MAC is different and then send the data
//			String roomObject = omiObject.createOdfObject(objectId, infoItems);
//			String thermoObject = omiObject.createOdfObject("Thermostats", roomObject);
//			String topObject = omiObject.createOdfObject("Otakaari-3", thermoObject);
//			String finalMessage = omiObject.createWriteMessage(omiObject.createOdfObjects(topObject));
//			sendData(omiURL, finalMessage);
//		}
	}

	/* Method call to get data in JSON string format */
	public String getJsonData(String url, String token) {

		HttpURLConnection httpcon = null;
		BufferedReader br = null;
		StringBuffer response = null;
		try {
			httpcon = (HttpURLConnection) ((new URL(url).openConnection()));
			httpcon.setDoOutput(false);
			String authorization="Token " + token;
			httpcon.setRequestProperty("Authorization", authorization);
			httpcon.setRequestProperty("Accept", "application/json");
			httpcon.setRequestMethod("GET");
			//System.out.println(httpcon.getResponseMessage());
			br = new BufferedReader(new InputStreamReader(httpcon.getInputStream(), "UTF-8"));
			response = new StringBuffer();

			String inputLine;
			while ((inputLine = br.readLine()) != null) {
				response.append(inputLine);
			}
			br.close();
		} catch (Throwable e) {
			System.out.println(e.getMessage());
			System.exit(1);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {}
			}

			if (httpcon != null) {
				httpcon.disconnect();
			}
		}

		return response.toString();
	}

	/* Method call to send OMI write envelope to the sand box */
	private static void sendData(String url, String finalMessage) {
		HttpURLConnection httpcon = null;
		OutputStream os =  null;
		try {
			httpcon = (HttpURLConnection) ((new URL(url).openConnection()));
			httpcon.setDoOutput(true);
			httpcon.setRequestProperty("Content-Type", "text/xml");
			httpcon.setRequestMethod("POST");
			httpcon.setUseCaches(false);
			byte[] outputBytes = finalMessage.getBytes("UTF-8");
			os = httpcon.getOutputStream();
			os.write(outputBytes);
			httpcon.getResponseMessage();

		} catch (Throwable e) {
			System.out.println(e.getMessage());
			System.exit(1);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {}
			}

			if (httpcon != null) {
				httpcon.disconnect();
			}
		}
	}

	/* Method call to get value of the Info Item MAC */
	public static String getMacValue(String url) {

		HttpURLConnection httpcon = null;
		BufferedReader br = null;
		StringBuffer response = null;
		try {
			httpcon = (HttpURLConnection) ((new URL(url).openConnection()));
			httpcon.setDoOutput(false);
			httpcon.setRequestMethod("GET");
			br = new BufferedReader(new InputStreamReader(httpcon.getInputStream(), "UTF-8"));
			response = new StringBuffer();

			String inputLine;
			while ((inputLine = br.readLine()) != null) {
				response.append(inputLine);
			}
			br.close();
		} catch (Throwable e) {
			System.out.println(e.getMessage());
			System.exit(1);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {}
			}

			if (httpcon != null) {
				httpcon.disconnect();
			}
		}

		return response.toString();
	}
}
