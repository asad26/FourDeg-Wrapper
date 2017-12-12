/*
 * Created by Asad Javed on 28/08/2017
 * Aalto University project
 * 
 * Last modified 04/10/2017
 */

package fi.aalto.fourdeg.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.json.JSONException;

import fi.aalto.fourdeg.Wrapper.FourDegData;

public class Main {

	public static void main(String[] args) {

		System.out.println("Process has been started...");
		
		// Load and get properties from the file
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream("resources/config.properties"));
		} catch (IOException e) {
			System.out.print(e.getMessage());
			System.exit(1);
		}
		String token = prop.getProperty("fourdeg_token");
		String fdUrl = prop.getProperty("fourdeg_url");

		FourDegData fdObject = new FourDegData(prop);
		
		//System.out.println(jsonData);
		try {
			String jsonData = fdObject.getJsonData(fdUrl, token);
			fdObject.parseArray(jsonData);
		} catch (JSONException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
		
		System.out.println("Total objects processed: " + fdObject.count);
		System.out.println("***Data has been successfully exposed to the OMI sand box***");
		System.exit(0);
	}
}
