package ro.pub.cs.systems.eim.practical2test;

import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import ro.pub.cs.systems.eim.practical2test.Constants;
import ro.pub.cs.systems.eim.practical2test.Utilities;

public class ClientThread extends Thread {

    private String address;
    private int port;
    private String word;
    private TextView weatherForecastTextView;

    private Socket socket;

    public ClientThread(String address, int port, String word, TextView weatherForecastTextView) {
        this.address = address;
        this.port = port;
        this.word = word;
        this.weatherForecastTextView = weatherForecastTextView;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(address, port);
            if (socket == null) {
                Log.e(Constants.TAG, "[CLIENT THREAD] Could not create socket!");
                return;
            }

            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[CLIENT THREAD] Buffered Reader / Print Writer are null!");
                return;
            }

            // write to socket
            printWriter.println(word);
            printWriter.flush();

            // read
            String earthquakeInformation;
            while((earthquakeInformation = bufferedReader.readLine()) != null) {
                final String finalizedWeatherInformation = earthquakeInformation;
                weatherForecastTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        weatherForecastTextView.setText(finalizedWeatherInformation);
                    }
                });
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }

    }
}