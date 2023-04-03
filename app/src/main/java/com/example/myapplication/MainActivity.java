package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.kunal52.AndroidRemoteTv;
import com.example.myapplication.kunal52.AndroidTvListener;
import com.example.myapplication.kunal52.exception.PairingException;
import com.example.myapplication.kunal52.remote.Remotemessage;
import com.faendir.rhino_android.RhinoAndroidHelper;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private Scriptable scope;
    private RhinoAndroidHelper rhinoAndroidHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        rhinoAndroidHelper = new RhinoAndroidHelper(this);
//        context = rhinoAndroidHelper.enterContext();
//        context.setOptimizationLevel(1);
//        scope = new ImporterTopLevel(context);
    }

    AndroidRemoteTv androidRemoteTv;
    public void connect_tv(View view) {

        androidRemoteTv = new AndroidRemoteTv();

        RunUtil.runInBackground(() -> {

            try {
                androidRemoteTv.connect("192.168.1.25", new AndroidTvListener() {
                    @Override
                    public void onSessionCreated() {
                        System.out.println("thanhlv androidRemoteTv.connect() onSessionCreated" );

                    }

                    @Override
                    public void onSecretRequested() {
                        System.out.println("thanhlv androidRemoteTv.connect() onSecretRequested" );
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(System.in));

                        try {
                            String name = reader.readLine();
                            androidRemoteTv.sendSecret(name);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onPaired() {
                        System.out.println("thanhlv androidRemoteTv.connect() onPaired" );
                    }

                    @Override
                    public void onConnectingToRemote() {
                        System.out.println("thanhlv androidRemoteTv.connect() onConnectingToRemote" );
                    }

                    @Override
                    public void onConnected() {
                        System.out.println("thanhlv androidRemoteTv.connect() onConnected" );

                        androidRemoteTv.sendCommand(Remotemessage.RemoteKeyCode.KEYCODE_POWER, Remotemessage.RemoteDirection.SHORT);

                    }

                    @Override
                    public void onDisconnect() {
                        System.out.println("thanhlv androidRemoteTv.connect() onDisconnect" );

                    }

                    @Override
                    public void onError(String error) {
                        System.out.println("thanhlv androidRemoteTv.connect() onError" );


                    }
                });
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (PairingException e) {
                throw new RuntimeException(e);
            }
        });
    }


//    private void toastScript(String script) {
//        try {
//            Object result = context.evaluateString(scope, script, "<hello_world>", 1, null);
//            Toast.makeText(this, Context.toString(result), Toast.LENGTH_LONG).show();
//        } catch (Throwable t) {
//            t.printStackTrace();
//        }
//    }
//
//    public void button(View v) {
//        switch (v.getId()) {
//            case R.id.button:
//                toastScript(((EditText) findViewById(R.id.editText)).getText().toString());
//                break;
//            case R.id.button3:
//                new TaskJS(this, rhinoAndroidHelper).execute(-1, 0, 1);
//                break;
//        }
//    }
}