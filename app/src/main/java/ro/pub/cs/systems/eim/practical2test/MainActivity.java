package ro.pub.cs.systems.eim.practical2test;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import ro.pub.cs.systems.eim.practical2test.R;
import ro.pub.cs.systems.eim.practical2test.Constants;
import ro.pub.cs.systems.eim.practical2test.ClientThread;
import ro.pub.cs.systems.eim.practical2test.ServerThread;

public class MainActivity extends AppCompatActivity {

    // Server widgets
    private EditText serverPortEditText = null;
    private Button serverConnectButton = null;

    // Client widgets
    private EditText clientAddressEditText = null;
    private EditText clientPortEditText = null;
    private EditText clientWordEditText = null;

    private TextView earthquakeInformationTextView = null;

    private Button getEarthquakeInformationButton = null;

    // Threads
    private ServerThread serverThread = null;
    private ClientThread clientThread = null;

    private ServerConnectButtonListener serverConnectButtonListener = new ServerConnectButtonListener();
    private class ServerConnectButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            String serverPort = serverPortEditText.getText().toString();
            if (serverPort == null || serverPort.isEmpty()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Server port cannot be null!", Toast.LENGTH_LONG).show();
                return;
            }
            serverThread = new ServerThread(Integer.parseInt(serverPort));
            if (serverThread.getServerSocket() == null) {
                Log.e(Constants.TAG, "[MAIN ACTIVITY] Could not create server thread!");
                return;
            }
            serverThread.start();
        }
    }

    private ClientGetEarthquakeButtonListener clientGetEarthquakeInformationClientButtonListener = new ClientGetEarthquakeButtonListener();
    private class ClientGetEarthquakeButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            String clientAddress = clientAddressEditText.getText().toString();
            String clientPort = clientPortEditText.getText().toString();
            String word = clientWordEditText.getText().toString();

            if (clientAddress == null || clientAddress.isEmpty() || clientPort == null || clientPort.isEmpty()
                    || word == null || word.isEmpty()){
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Client parameters cannot be null!", Toast.LENGTH_LONG).show();
                return;
            }

            // check serverThread
            if (serverThread == null || !serverThread.isAlive()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] There is no server to connect to!", Toast.LENGTH_LONG).show();
                return;
            }

            clientThread = new ClientThread(clientAddress, Integer.parseInt(clientPort),
                    word, earthquakeInformationTextView);
            clientThread.start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Constants.TAG, "[MAIN ACTIVITY] onCreate() callback method has been invoked");
        setContentView(R.layout.activity_main);

        // server widgets
        serverPortEditText = findViewById(R.id.portServerEditText);
        serverConnectButton = findViewById(R.id.connectServerButton);
        serverConnectButton.setOnClickListener(serverConnectButtonListener);

        // client widgets
        clientAddressEditText = findViewById(R.id.addressClientEditText);
        clientPortEditText = findViewById(R.id.portClientEditText);
        clientWordEditText = findViewById(R.id.clientWordEditText);
        earthquakeInformationTextView = findViewById(R.id.earthquake_information_text_view);
        getEarthquakeInformationButton = findViewById(R.id.getEarthquakeInformationClientButton);
        getEarthquakeInformationButton.setOnClickListener(clientGetEarthquakeInformationClientButtonListener);

    }

    @Override
    protected void onDestroy() {
        Log.i(Constants.TAG, "[MAIN ACTIVITY] onDestroy() callback has been invoked");
        if (serverThread != null) {
            serverThread.stopThread();
        }
        super.onDestroy();
    }
}