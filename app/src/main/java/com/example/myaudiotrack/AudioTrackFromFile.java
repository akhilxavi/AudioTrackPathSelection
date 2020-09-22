package com.example.myaudiotrack;

import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.io.IOException;
import java.io.InputStream;

public class AudioTrackFromFile {
    Context mContext;
    private static int minBufferSize;
    AudioTrack audioTrack;
    InputStream inputStream;
    private AudioManager audioManager;
    AudioDeviceInfo[] deviceList;
    int preferredDeviceIndex;
    private Thread playBackThread = null;

    public AudioTrackFromFile(Context context){
        mContext = context;
        minBufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        inputStream = mContext.getResources().openRawResource(R.raw.news);
    }

    public void initializeAudioTrack(){
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize, AudioTrack.MODE_STREAM);

        audioTrack.setPreferredDevice(deviceList[preferredDeviceIndex]);

        playBackThread = new Thread(new Play(), "AudioTrack Thread");
    }

    public void setPreferredDeviceIndex(){
        deviceList = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);

        for (int index = 0; index < deviceList.length; index++) {
            if (deviceList[index].getType() == AudioDeviceInfo.TYPE_USB_DEVICE) {
                preferredDeviceIndex = index;
            }
        }
    }

    public void startAudioTrack(){
        playBackThread.start();
    }

    private class Play implements Runnable{

        public void run() {
            int len = 0;
            byte[] data = null;

            try {
                data = new byte[512];
                audioTrack.play();

                while ((len = inputStream.read(data)) != -1)
                    audioTrack.write(data, 0, len);

            } catch (IOException e) {
                e.printStackTrace();
            }

            audioTrack.stop();
            audioTrack.release();
        }
    }
}
