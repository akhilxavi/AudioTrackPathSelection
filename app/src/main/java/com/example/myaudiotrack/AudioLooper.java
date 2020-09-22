package com.example.myaudiotrack;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.UserHandle;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioLooper {
    private static final String TAG = "AudioLooper";

    Context context;
    private AudioManager audioManager;
    int preferredDeviceIndexOut, preferredDeviceIndexIn;
    int outPropValue;

    private AudioRecord      recorder              = null;
    private AudioTrack       audioTrack            = null;
    private Thread           recordingThread       = null;
    private static final int SAMPLING_RATE_IN_HZ   = 44100;
    private static final int TRACK_STREAM_TYPE     = AudioManager.STREAM_MUSIC;
    private static final int TRACK_SAMPLE_RATE_HZ  = 48000;
    private static final int AUDIO_FORMAT          = AudioFormat.ENCODING_PCM_16BIT;
    private static final int RECORD_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
    private static final int TRACK_CHANNEL_CONFIG  = AudioFormat.CHANNEL_OUT_STEREO;
    AudioDeviceInfo[] deviceListOut, deviceListIn;

    private boolean mIsMuted = false;
    private float mVolume = 1.0f; // Default AudioTrack value unit gain.

    // Minimum buffer size to construct the AudioRecord object.
    private static int RECORD_BUFFER_SIZE =
            AudioRecord.getMinBufferSize( SAMPLING_RATE_IN_HZ, RECORD_CHANNEL_CONFIG, AUDIO_FORMAT );

    // Track whether or not we're recording.
    private AtomicBoolean mIsRecording = new AtomicBoolean( false );

    // The size of each AudioBuffer. Gets updated when we start audio;
    // We get 2-channel 16-bit audio, so the number of bytes should be a multiple of 4.
    private int mBytesPerAudioBuffer = 512; // Somewhat arbitrarily.

    // Minimum buffer size to construct the AudioTrack object.
    private int TRACK_BUFFER_SIZE = AudioTrack.getMinBufferSize( TRACK_SAMPLE_RATE_HZ, TRACK_CHANNEL_CONFIG, AUDIO_FORMAT );

    private AudioBuffer mBuffer = null;

    public static String[] permissionsRequired()
    {
        return new String[] { Manifest.permission.RECORD_AUDIO };
    }

    public AudioLooper(Context context){
        this.context = context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void setupPermissions()
    {
        // Check permissions
        grantPermission( context, Manifest.permission.CAMERA );

        for ( String permission : permissionsRequired() )
        {
            grantPermission( context, permission );
        }
    }

    private static void grantPermission( Context context, String permissionName )
    {
        if ( ContextCompat.checkSelfPermission( context, permissionName ) != PackageManager.PERMISSION_GRANTED )
        {
            Log.d( TAG, "Granting missing permission: " + permissionName );
            PackageManager packageManager = context.getPackageManager();
            try
            {
                ReflectionHelper.callMethod(
                        packageManager,
                        "grantRuntimePermission",
                        new String[] { String.class.getName(), String.class.getName(), UserHandle.class.getName() },
                        new Object[] {
                                context.getPackageName(), permissionName, android.os.Process.myUserHandle() } );
            }
            catch ( Exception e )
            {
                e.printStackTrace();
                return;
            }
        }
    }

    private class AudioBuffer
    {
        public ByteBuffer buffer    = null;
        public int        bytesUsed = 0;

        public AudioBuffer( int size )
        {
            buffer = ByteBuffer.allocateDirect( size );
        }

        public void clear()
        {
            bytesUsed = 0;
            if ( null != buffer )
            {
                buffer.clear();
            }
        }
    }

    private class RecordingRunnable implements Runnable
    {
        @Override
        public void run()
        {
            Log.d( TAG, "RecordingRunnable start." );

            recorder.startRecording();
            audioTrack.play();

            while ( mIsRecording.get() )
            {
                // Read incoming audio stream from HDMI input.
                read();
            }

            Log.d( TAG, "RecordingRunnable end." );
        }
    }

    private void read()
    {
        mBuffer.bytesUsed = recorder.read( mBuffer.buffer, mBytesPerAudioBuffer, AudioRecord.READ_BLOCKING );

        if ( mBuffer.bytesUsed > 0 )
        {
            int result = audioTrack.write( mBuffer.buffer, mBuffer.bytesUsed, AudioTrack.WRITE_BLOCKING );

            if ( result < 0 )
            {
                Log.e( TAG, "Error writing buffer: " + result + " " + getBufferWriteFailureReason( result ) );
            }

            mBuffer.clear();
        }
        else if ( mBuffer.bytesUsed < 0 )
        {
            // This also happens at startup with nothing plugged in.
            String message = "Reading of audio buffer failed: " + getBufferReadFailureReason( mBuffer.bytesUsed );
            Log.e( TAG, message );
        }
        else
        {
            // Read 0 bytes
        }
    }

    public synchronized void setVolume( float volume )
    {
        if ( null != audioTrack && mVolume != volume )
        {
            mVolume = Math.max( volume, audioTrack.getMinVolume() );
            mVolume = Math.min( mVolume, audioTrack.getMaxVolume() );

            Log.d( TAG, "Setting volume to: " + mVolume );

            mIsMuted = audioTrack.getMinVolume() == mVolume;
            audioTrack.setVolume( mVolume );
        }
    }

    public synchronized void Mute( boolean doMute )
    {
        if ( null != audioTrack )
        {
            mIsMuted     = doMute;
            float volume = mIsMuted ? audioTrack.getMinVolume() : mVolume;
            Log.d( TAG, "Mute( " + doMute + " ). Setting volume to: " + volume );
            audioTrack.setVolume( volume );
        }
    }

    public synchronized boolean IsMuted()
    {
        return mIsMuted;
    }

    public void setPreferredDeviceIndex(){
        deviceListOut = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
        deviceListIn = audioManager.getDevices( AudioManager.GET_DEVICES_INPUTS );

        for (int index = 0; index < deviceListOut.length; index++) {
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
            {
                if (deviceListOut[index].getType() == outPropValue) {
                    preferredDeviceIndexOut = index;
                    //Log.e(TAG,"AKHIL:usb available for out");
                }
            }
        }
        for (int index = 0; index < deviceListIn.length; index++) {
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
            {
                if (deviceListIn[index].getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                    preferredDeviceIndexIn = index;
                    Log.e(TAG,"AKHIL:wired headset available for in");
                }
            }
        }
    }

    public void startAudio( int sampleRate )
    {
        if ( null != recorder && null != audioTrack )
        {
            Log.d( TAG, "startAudio: recorder already initialized." );
            return;
        }

        mBuffer = new AudioBuffer( mBytesPerAudioBuffer );

        try
        {
            Log.d( TAG, "Initializing audio." );
            Log.d( TAG, "Sample rate (Hz): " + sampleRate );
            Log.d( TAG, "Record buffer size (bytes): " + RECORD_BUFFER_SIZE );
            Log.d( TAG, "Playback buffer size (bytes): " + TRACK_BUFFER_SIZE );
            Log.d( TAG, "Bytes per chunk of audio: " + mBytesPerAudioBuffer );

            //setPreferredDeviceIndex();

            // The sample rate used for recording must be fixed because the function (in android)
            // AudioSystem::getSamplingRate always returns the same value. If we don't exactly match the value
            // returned by that function, android will try to resample the audio, however since it is doing so from
            // the wrong source sample rate so it sounds terrible. The value passed to the AudioTrack is the true HDMI
            // in sample rate (from a uevent).
            recorder = new AudioRecord(
                    MediaRecorder.AudioSource.CAMCORDER,
                    SAMPLING_RATE_IN_HZ,
                    RECORD_CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    RECORD_BUFFER_SIZE );

            audioTrack = new AudioTrack(
                    TRACK_STREAM_TYPE,
                    sampleRate,
                    TRACK_CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    TRACK_BUFFER_SIZE,
                    AudioTrack.MODE_STREAM );

            audioTrack.setPreferredDevice(deviceListOut[preferredDeviceIndexOut]);
            //recorder.setPreferredDevice( deviceListIn[preferredDeviceIndexIn] );
        }
        catch ( Throwable x )
        {
            Log.w( TAG, "Error reading HDMI input audio", x );
        }

        if ( recorder.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED )
        {
            Log.d( TAG, "Starting recording." );
            mIsRecording.set( true );
            recordingThread = new Thread( new RecordingRunnable(), "Hdmi Input Audio Recording Thread" );
            recordingThread.start();
        }
        else
        {
            Log.d( TAG, "Not starting recording. Current state: " + recorder.getRecordingState() );
        }
    }

    public void stopAudio()
    {
        Log.d( TAG, "StopRecording" );

        mIsRecording.set( false );

        if ( null == recorder && audioTrack == null && recordingThread == null )
        {
            return;
        }
        try
        {
            if ( recordingThread != null )
            {
                recordingThread = null;
            }

            if ( audioTrack != null || recorder != null )
            {
                if ( recorder != null )
                {
                    if ( recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING )
                    {
                        recorder.stop();
                    }
                    recorder.release();
                    recorder = null;
                }
                if ( audioTrack != null )
                {
                    audioTrack.stop();
                    audioTrack.pause();
                    audioTrack.flush();
                    audioTrack.release();
                    audioTrack = null;
                }
            }
        }
        catch ( Exception e )
        {
            Log.e( TAG, "release() ERROR:", e );
            e.printStackTrace();
        }
    }

    private String getBufferReadFailureReason( int errorCode )
    {
        switch ( errorCode )
        {
            case AudioRecord.ERROR_INVALID_OPERATION:
                return "ERROR_INVALID_OPERATION";
            case AudioRecord.ERROR_BAD_VALUE:
                return "ERROR_BAD_VALUE";
            case AudioRecord.ERROR_DEAD_OBJECT:
                return "ERROR_DEAD_OBJECT";
            case AudioRecord.ERROR:
                return "ERROR";
            default:
                return "Unknown (" + errorCode + ")";
        }
    }

    private String getBufferWriteFailureReason( int errorCode )
    {
        switch ( errorCode )
        {
            case AudioTrack.ERROR_INVALID_OPERATION:
                return "ERROR_INVALID_OPERATION";
            case AudioTrack.ERROR_BAD_VALUE:
                return "ERROR_BAD_VALUE";
            case AudioRecord.ERROR_DEAD_OBJECT:
                return "ERROR_DEAD_OBJECT";
            case AudioRecord.ERROR:
                return "ERROR";
            default:
                return "Unknown (" + errorCode + ")";
        }
    }
}
