package tlab.hue;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;


public class MainActivity extends ActionBarActivity {

    private final String USERNAME_FILENAME = "HueBridgeUserNames";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Find available Bridges
        try {
            String bridgeIPAddress = new findAvailableBridges().execute().get();
        }catch(Exception e){

        }

        //TODO: Handle Errors if no bridges are found.
        //TODO: Allow user to select bridge if multiple are found.

        //Connect to Found Bridge

    }

    /**
     * Search the current netowrk for Hue Bridges.
     *
     * @return the IP address of the first bridge found
     *
     * TODO: Return multiple bridges
     *
     */
    private class findAvailableBridges extends AsyncTask<Boolean, Void, String> {
        protected String doInBackground(Boolean... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();

            HttpGet httpGet = new HttpGet("https://www.meethue.com/api/nupnp");

            HttpResponse response;
            String xml = null;

            try {
                response = httpClient.execute(httpGet, localContext);
                System.out.print(response.toString());

                HttpEntity httpEntity = response.getEntity();
                xml = EntityUtils.toString(httpEntity);
            } catch (Exception e) {
                System.out.print(e.toString());
            }

            String id = null;
            String ipAddress = null;

            try {
                JSONArray jsonArray = new JSONArray(xml);
                JSONObject jsonObject = jsonArray.getJSONObject(0);


                id = jsonObject.getString("id");
                ipAddress = jsonObject.getString("internalipaddress");
            } catch (JSONException e){
                e.printStackTrace();
            }


            StringBuffer fileContent = new StringBuffer("");

            //Try to load a username from the internal storage
            /*try {
                FileInputStream fIn = openFileInput ( USERNAME_FILENAME ) ;
                InputStreamReader isr = new InputStreamReader( fIn ) ;
                BufferedReader buffreader = new BufferedReader( isr ) ;

                String readString = buffreader.readLine ( ) ;
                while ( readString != null ) {
                    fileContent.append(readString);
                    readString = buffreader.readLine ( ) ;
                }

                isr.close ( ) ;
            } catch ( IOException ioe ) {
                ioe.printStackTrace ( ) ;
            }*/


            //Get a username from the Bridge
            httpClient = new DefaultHttpClient();
            localContext = new BasicHttpContext();
            StringEntity input = null;

            HttpPost httpPost = new HttpPost("http://" + ipAddress + "/api");
            JSONObject keyArg = new JSONObject();
            input = null;
            try{
                keyArg.put("devicetype", "tlab#testPhone");
                input = new StringEntity(keyArg.toString());
            }catch(JSONException e){

            } catch (UnsupportedEncodingException e){

            }

            httpPost.setEntity(input);
            try {
                HttpResponse postResponse = httpClient.execute(httpPost, localContext);
                HttpEntity httpEntity = postResponse.getEntity();
                xml = EntityUtils.toString(httpEntity);
            } catch (Exception e) {
                //TODO: Log Exception
                System.out.print(e.toString());
            }





            String username = "";
            try {
                JSONArray jsonArray = new JSONArray(xml);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                JSONObject jsonObject2 = jsonObject.getJSONObject("success");

                username = jsonObject2.getString("username");
            } catch (JSONException e){
                e.printStackTrace();
            }

            //TODO: Store the username into a file

            return ipAddress;
        }
    }

}
