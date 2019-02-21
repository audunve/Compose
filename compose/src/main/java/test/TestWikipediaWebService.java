package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


public class TestWikipediaWebService {
	
	public static void main(String[] args) {
		
		String query = "car";
		String text = "";
        String content = null;
       
        
        try
        {
        		URL url = new URL("https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch="+query+"&format=xml&srlimit=10");
        		//URL url = new URL("http://en.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&format=json&titles=Abraham Lincoln&continue=&rvgeneratexml=");
            System.out.println("Test: The URL in invokeWebService() is " + url);
            HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            System.out.println("Response: " + conn.getResponseCode() + " and " + conn.getResponseMessage());
            System.out.println("Test: Length of content is " + conn.getContentLength());
            InputStream in = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String str;
            System.out.println("Reading line " + reader.readLine());
            while((str = reader.readLine()) != null) 
                text = (new StringBuilder(String.valueOf(text))).append(str).toString();
            		System.out.println("Test: text from wikipedia is " + text.length());
            if(text.trim().length() > 0) {
            	System.out.println("Test: There is no text: " + text);
               // setContent(text);
            content = text;
            } else {
            	content = null;
            	System.out.println("We have text yoohoo!");
               // setContent(null);
            }
            conn.disconnect();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }

	}

}
