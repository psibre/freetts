/**
 * Copyright 2001 Sun Microsystems, Inc.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package com.sun.speech.freetts.audio;

import com.sun.speech.freetts.util.Utilities;

import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import javax.sound.sampled.AudioFormat;


/**
 * Provides an implementation of <code>AudioPlayer</code> that sends
 * all audio data to the given file. 
 */
public class RawFileAudioPlayer implements AudioPlayer {

    private AudioFormat audioFormat;
    private float volume;
    private BufferedOutputStream os;

    
    /**
     * Constructs a RawFileAudioPlayer.  Reads the "rawPath" property
     * to define where to send the raw audio.
     */
    public RawFileAudioPlayer() throws IOException {
        this(Utilities.getProperty("path","freetts.raw"));
    }
    
    /**
     * Constructs a NullAudioPlayer
     */
    public RawFileAudioPlayer(String path) throws IOException {
	os = new BufferedOutputStream(new FileOutputStream(path));
    }
    

    /**
     * Sets the audio format for this player
     *
     * @param format the audio format
     */
    public void setAudioFormat(AudioFormat format) {
	this.audioFormat = format;
    }

    /**
     * Retrieves the audio format for this player
     *
     * @return the current audio format.
     */
    public AudioFormat getAudioFormat() {
	return audioFormat;
    }

    /**
     * Cancels all queued output. Current 'write' call will return
     * false
     *
     */
    public void cancel() {
    }


    /**
     * Pauses the audio output
     */
    public void pause() {
    }


    /**
     * Prepares for another batch of output. Larger groups of output
     * (such as all output associated with a single FreeTTSSpeakable)
     * should be grouped between a reset/drain pair.
     */
    public void reset() {
    }


    /**
     * Resumes audio output
     */
    public void resume() {
    }
	



    /**
     * Waits for all audio playback to stop, and closes this AudioPlayer.
     */
    public void close() {
        try {
            os.flush();
            os.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
        

    /**
     * Returns the current volume.
     *
     * @return the current volume (between 0 and 1)
     */
    public float getVolume() {
	return volume;
    }	      

    /**
     * Sets the current volume.
     *
     * @param volume  the current volume (between 0 and 1)
     */
    public void setVolume(float volume) {
	this.volume = volume;
    }	      


    /**
     * Writes the given bytes to the audio stream
     *
     * @param audioData array of audio data
     *
     * @return <code>true</code> of the write completed successfully, 
     *       	<code> false </code>if the write was cancelled.
     */
    public boolean write(byte[] audioData) {
	return write(audioData, 0, audioData.length);
    }


    /**
     *  Starts the output of a set of data
     *
     * @param size the size of data between now and the end
     *
     */
    public void begin(int size) {
    }

    /**
     *  Marks the end of a set of data
     *
     */
    public boolean  end()  {
	return true;
    }

    /**
     * Writes the given bytes to the audio stream
     *
     * @param bytes audio data to write to the device
     * @param offset the offset into the buffer
     * @param size the size into the buffer
     *
     * @return <code>true</code> of the write completed successfully, 
     *       	<code> false </code>if the write was cancelled.
     */
    public boolean write(byte[] bytes, int offset, int size) {
	try {
	    os.write(bytes, offset, size);
	} catch (IOException ioe) {
	    return false;
	}
	return true;
    }

    /**
     * Starts the first sample timer
     */
    public void startFirstSampleTimer() {
    }

    /**
     * Waits for all queued audio to be played
     *
     * @return <code>true</code> if the audio played to completion,
     *     	<code> false </code>if the audio was stopped
     */
    public boolean drain()  {
	return true;
    }

    /**
     * Gets the amount of played since the last resetTime
     * Currently not supported.
     *
     * @return the amount of audio in milliseconds
     */
    public long getTime()  {
	return -1L;
    }


    /**
     * Resets the audio clock
     */
    public void resetTime() {
    }

    /**
     * Shows metrics for this audio player
     */
    public void showMetrics() {
    }
}
