package com.example.myaudiotrack;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity{

    Spinner mySpinner1, mySpinner2;
    ArrayList<String> audioPaths;
    String outSelection = "default";
    String inSelection = "default";

//    String outCmd = "persist.audio.output.sel ";
    String inCmd = "persist.audio.input.sel ";

    String inPropValue;
    AudioManager audioManager;
    public AudioTrackFromFile audioTrackFromFile = null;
    public AudioRecordToFile audioRecordToFile = null;
    AudioLooper audioLooper = null;
    private final int PERMISSIONS_RECORD_AUDIO = 1;

    public void updateAudioPath(ArrayList<String> strings){
        switch (strings.get(0)){
            case "3.5 MM":
                audioLooper.outPropValue = AudioDeviceInfo.TYPE_WIRED_HEADSET;
                break;
            case "USB":
                audioLooper.outPropValue = AudioDeviceInfo.TYPE_USB_DEVICE;
                break;
            default:
                audioLooper.outPropValue = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER;
                break;
        }

        switch (strings.get(1)){
            case "Hdmi In":
                inPropValue = "hdmi-in";
                break;
            case "3.5 MM":
                inPropValue = "3.5mm";
                break;
            case "USB":
                inPropValue = "usb";
                break;
            default:
                inPropValue = "default";
                break;
        }
    }

    public void playTrack(View view){
      //  audioTrackFromFile.startAudioTrack();
       // audioRecordToFile.startRecording();
        audioLooper.startAudio(48000);
    }

    public void stopAudio(View view){
        audioLooper.stopAudio();
    }

    public void applyConfig(View view){
        outSelection = mySpinner1.getSelectedItem().toString();
        inSelection = mySpinner2.getSelectedItem().toString();
        audioPaths = new ArrayList<String>();

        audioPaths.add(outSelection);
        audioPaths.add(inSelection);

        updateAudioPath(audioPaths);

        audioLooper.setPreferredDeviceIndex();
        //Log.i("output path = ",outPropValue);
        Log.i(" input path = ",inPropValue);
    }

    private void requestAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG).show();

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        PERMISSIONS_RECORD_AUDIO);

            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        PERMISSIONS_RECORD_AUDIO);
            }
        }
        //If permission is granted, then go ahead recording audio
        else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {

            //Go ahead with recording audio now
        }
    }

    //Handling callback
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permissions Denied to record audio", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mySpinner1 = (Spinner) findViewById(R.id.spinner1);
        mySpinner2 = (Spinner) findViewById(R.id.spinner2);

        ArrayAdapter<String> myArrayAdaptor1 = new ArrayAdapter<String>(MainActivity.this,
                R.layout.my_spinner, getResources().getStringArray(R.array.outAudioPaths));

        ArrayAdapter<String> myArrayAdaptor2 = new ArrayAdapter<String>(MainActivity.this,
                R.layout.my_spinner, getResources().getStringArray(R.array.inAudioPaths));

        mySpinner1.setAdapter(myArrayAdaptor1);
        mySpinner2.setAdapter(myArrayAdaptor2);

//        audioTrackFromFile = new AudioTrackFromFile(this);
//        audioTrackFromFile.setPreferredDeviceIndex();
//        audioTrackFromFile.initializeAudioTrack();
        requestAudioPermissions();
        audioLooper = new AudioLooper(this);
//        audioRecordToFile = new AudioRecordToFile(this);
//        audioRecordToFile.initializeAudioRecord();
    }
}