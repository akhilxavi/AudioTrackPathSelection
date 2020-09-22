package com.example.myaudiotrack;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Process;
import android.os.UserHandle;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class AudioRecordToFile {

    private static final String LOGTAG = "AudioRecordFile";
    public static final String AUDIO_RECORDING_FILE_NAME = "recording.raw";
    Context mContext;
    int minBufferSize;
    private AudioManager audioManager;
    AudioRecord audioRecord;
    String filePath;
    BufferedOutputStream os = null;
    boolean mStop = false;
    byte audioData[];
    private Thread recordThread = null;

    public AudioRecordToFile(Context context){
        mContext = context;
        minBufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        filePath = Environment.getExternalStorageDirectory().getPath() + "/" + AUDIO_RECORDING_FILE_NAME;
    }

    public void initializeAudioRecord(){
//        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO,
//                                        AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
//
//        try {
//            os = new BufferedOutputStream(new FileOutputStream(filePath));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        audioData = new byte[minBufferSize];
        recordThread = new Thread(new AudioRecordToFile.Record(), "AudioRecord Thread");
    }

    public void startRecording(){
        recordThread.start();
    }

    private class Record implements Runnable{
        public void run(){
            //android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, minBufferSize);

            try {
                os = new BufferedOutputStream(new FileOutputStream(filePath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            audioData = new byte[minBufferSize];
            audioRecord.startRecording();
            while(!mStop){
                int status = audioRecord.read(audioData, 0, audioData.length);
                if (status == AudioRecord.ERROR_INVALID_OPERATION ||
                        status == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e(LOGTAG, "Error reading audio data!");
                    return;
                }
                try{
                    os.write(audioData, 0, audioData.length);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            try{
                os.close();
                audioRecord.stop();
                audioRecord.release();

                Log.v(LOGTAG, "Recording Done...!");
                mStop = false;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
