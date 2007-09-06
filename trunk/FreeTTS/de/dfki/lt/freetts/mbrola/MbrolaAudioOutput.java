/**
 * Copyright 2002 DFKI GmbH.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */
package de.dfki.lt.freetts.mbrola;

import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.List;

import javax.sound.sampled.AudioFormat;

import com.sun.speech.freetts.ProcessException;
import com.sun.speech.freetts.Utterance;
import com.sun.speech.freetts.UtteranceProcessor;
import com.sun.speech.freetts.audio.AudioPlayer;

/**
 * Supports generating audio output from an MBROLA-synthesized utterance. This
 * is an utterance processor. The primary method, <code> processUtterance
 * </code> takes an utterance containing an open BufferedInputStream, from
 * which to read raw audio data provided by the external MBROLA binary. The
 * audio data is read and sent to the proper audio player.
 *
 */
public class MbrolaAudioOutput implements UtteranceProcessor {
    /**
     * The raw audio data coming out of MBROLA is in native byte order,
     * 16 kHz, 16 bit, mono
     */
    private final static AudioFormat MBROLA_AUDIO =
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                            16000, // samples per second
                            16, // bits per sample
                            1, // mono
                            2, // nr. of bytes per frame
                            16000, // nr. of frames per second
                            ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN);
    
    /**
     * Reads audio data generated by the external MBROLA binary for the given
     * Utterance. The data is read from the open BufferedInputStream associated
     * with the Utterance, and written into the AudioPlayer.
     *
     * @param  utterance  the utterance to generate waves
     *
     * @throws ProcessException if an IOException is thrown during the
     *         processing of the utterance
     */
    public void processUtterance(Utterance utterance) throws ProcessException {

	utterance.getVoice().log("=== " +
                                 utterance.getString("input_text"));

        AudioPlayer audioPlayer = utterance.getVoice().getAudioPlayer();

	audioPlayer.setAudioFormat(MBROLA_AUDIO);
	audioPlayer.setVolume(utterance.getVoice().getVolume());

        // The AudioPlayer interface currently does not allow streaming audio.
        // We need to know the total number of samples that will be written
        // before we can start writing them. Therefore, we need to load all
        // audio data for this utterance into RAM.
        
        List audioData = (List) utterance.getObject("mbrolaAudio");
        if (audioData == null) {
            throw new ProcessException
                ("No \"mbrolaAudio\" object is associated with utterance");
        }

        // total number of audio bytes

        int totalSize;
        try {
            totalSize = utterance.getInt("mbrolaAudioLength");
        } catch (NullPointerException npe) {
            totalSize = 0;
        }

        audioPlayer.begin(totalSize);

        for (Iterator it = audioData.iterator(); it.hasNext();) {
            byte[] bytes = (byte[]) it.next();
            if (!audioPlayer.write(bytes)) {
                throw new ProcessException
                    ("Cannot write audio data to audio player");
            }
        }

        if (!audioPlayer.end()) {
            throw new ProcessException("audio player reports problem");
        }
    }

    
    /**
     * 
     * Returns the string form of this object
     * 
     * @return the string form of this object
     */
    public String toString() {
	return "MbrolaAudioOutput";
    }
}




