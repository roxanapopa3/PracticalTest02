package ro.pub.cs.systems.eim.practical2test;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;
import ro.pub.cs.systems.eim.practical2test.Constants;
import ro.pub.cs.systems.eim.practical2test.Utilities;
import ro.pub.cs.systems.eim.practical2test.EarthquakeInformation;

public class CommunicationThread extends Thread {

    private ServerThread serverThread;
    private Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] BufferedReader/PrintWriter are null!");
                return;
            }

            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (city / information type)!");
            // read from client
            String value = bufferedReader.readLine();
            if (value == null || value.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client!");
                return;
            }
            HashMap<String, EarthquakeInformation> data = serverThread.getData();
            EarthquakeInformation earthquakeInformation = null;
            if (data.containsKey(value)) {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                earthquakeInformation = data.get(value);
            } else {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");
                HttpClient httpClient = new DefaultHttpClient();
                String pageSourceCode = "";

                HttpGet httpGet = new HttpGet(Constants.WEB_SERVICE_ADDRESS + value);
                HttpResponse httpGetResponse = httpClient.execute(httpGet);
                HttpEntity httpGetEntity = httpGetResponse.getEntity();
                if (httpGetEntity != null) {
                    pageSourceCode = EntityUtils.toString(httpGetEntity);
                }

                if (pageSourceCode == null) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                    return;
                }
                Log.i(Constants.TAG, pageSourceCode);

                //get first element of jsonarray

                JSONArray jsonArray = new JSONArray(pageSourceCode);
                JSONObject content = jsonArray.getJSONObject(0);
                JSONArray meaningArray = content.getJSONArray("meanings");
                JSONObject meaning = meaningArray.getJSONObject(0);
                JSONArray definitions = meaning.getJSONArray("definitions");
                JSONObject definition = definitions.getJSONObject(0);

                earthquakeInformation = new EarthquakeInformation(
                        definition.getString(Constants.DEFINITION));

                serverThread.setData(value, earthquakeInformation);
            }

            if (earthquakeInformation == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Weather Forecast Information is null!");
                return;
            }

            String result = earthquakeInformation.getDefinition();
            // write to socket
            printWriter.println(result);
            printWriter.flush();
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } catch (JSONException jsonException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + jsonException.getMessage());
            if (Constants.DEBUG) {
                jsonException.printStackTrace();
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }
}