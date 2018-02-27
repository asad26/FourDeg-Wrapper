/*
 * Created by Asad Javed on 28/08/2017
 * Aalto University project
 * 
 * Last modified 04/10/2017
 */

package fi.aalto.fourdeg.Wrapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FourDegData {

	private static ApiForOmi omiObject;
	private static String omiURL;
	public int count;
	private static OkHttpClient client;

	/* Constructor for this class */
	public FourDegData(Properties prop) {
		omiObject = new ApiForOmi();
		omiURL = prop.getProperty("omi_node");
		count = 0;
		client = new OkHttpClient();
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
		String newKey = null;

		try {
			String pubDate = jObject.getString("last_communication");
			String objectId = jObject.getString("name").replace(" ", "");

			String URL = "http://localhost:8080/Objects/Otakaari4/Fourdeg/" + objectId + "/MAC/value";
			String macValue = getMacValue(URL);
			String MAC = jObject.getString("mac");

			if (!macValue.equals(MAC)) {
				newKey = "MAC";
				infoItems = infoItems + omiObject.createInfoItem(newKey, jObject.getString("mac"), pubDate);
			}

			newKey = "Set-Point";
			infoItems = infoItems + omiObject.createInfoItem(newKey, jObject.getString("current_set_point"), pubDate);

			newKey = "Temperature";
			infoItems = infoItems + omiObject.createInfoItem(newKey, jObject.getString("current_temperature"), pubDate);

			newKey = "Valve-Position";
			infoItems = infoItems + omiObject.createInfoItem(newKey, jObject.getString("current_valve_position"), pubDate);

			newKey = "Battery-Remaining";
			infoItems = infoItems + omiObject.createInfoItem(newKey, jObject.getString("current_battery_remaining"), pubDate);
			
			String roomObject = omiObject.createOdfObject(objectId, infoItems);
			String thermoObject = omiObject.createOdfObject("Fourdeg", roomObject);
			String topObject = omiObject.createOdfObject("Otakaari4", thermoObject);
			String finalMessage = omiObject.createWriteMessage(omiObject.createOdfObjects(topObject));
			sendData(omiURL, finalMessage);
			
		} catch (JSONException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}

	/* Method call to get data in JSON string format */
	public String getJsonData(String url, String token) {
		
		Response response = null;
		try {
			String authToken = "Token " + token;
			Request request = new Request.Builder().url(url).header("Authorization", authToken).build();
			response = client.newCall(request).execute();
			return response.body().string();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
		return null;
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

	/* Method call to get value of the InfoItem MAC */
	public static String getMacValue(String url) {
		//System.out.println(url);
		Response response = null;
		try {
			Request request = new Request.Builder().url(url).build();
			response = client.newCall(request).execute();
			return response.body().string();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
		return null;
	}
}
