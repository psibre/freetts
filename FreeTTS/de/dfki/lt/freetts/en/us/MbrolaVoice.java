/**
 * Copyright 2002 DFKI GmbH.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package de.dfki.lt.freetts.en.us;

import java.io.File;
import java.net.URL;

import com.sun.speech.freetts.Item;
import com.sun.speech.freetts.ProcessException;
import com.sun.speech.freetts.Relation;
import com.sun.speech.freetts.Utterance;
import com.sun.speech.freetts.UtteranceProcessor;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.en.us.CMUVoice;

import de.dfki.lt.freetts.mbrola.ParametersToMbrolaConverter;
import de.dfki.lt.freetts.mbrola.MbrolaCaller;
import de.dfki.lt.freetts.mbrola.MbrolaAudioOutput;

import java.io.IOException;

/**
 * Defines an unlimited-domain diphone synthesis based voice using
 * the MBROLA synthesis.
 */
public class MbrolaVoice extends CMUVoice {
        
    /**
     * Creates a simple voice
     */
    public MbrolaVoice() {
	this(false);
    }

    /**
     * Creates a simple voice
     *
     * @param createLexicon if <code>true</code> automatically load up
     * the default CMU lexicon; otherwise, don't load it.
     */
    public MbrolaVoice(boolean createLexicon) {
	super(createLexicon);
    }

    //[[Providing the Mbrola classes via getUnitSelector() and
    // getUnitConcatenator() is just a hack allowing us to use
    // the current CMUVoice.java framework. It only means that
    // after the Durator and the ContourGenerator, the classes
    // process the utterance (Selector before Concatenator).]]
    /**
     * Returns the unit selector to be used by this voice.
     * Derived voices typically override this to customize behaviors.
     * 
     * @return the unit selector
     * 
     * @throws IOException if an IO error occurs while getting
     *     processor
     */
    protected UtteranceProcessor getUnitSelector() throws IOException {
        return new ParametersToMbrolaConverter();
    }


    /**
     * Returns the unit concatenator to be used by this voice.
     * Derived voices typically override this to customize behaviors.
     * 
     * @return the unit conatenator
     * 
     * @throws IOException if an IO error occurs while getting
     *     processor
     */
    protected UtteranceProcessor getUnitConcatenator() throws IOException {
        return null;
    }

    protected UtteranceProcessor getAudioOutput() throws IOException {
        return new MbrolaAudioOutput();
    }

    /**
     * Get a resource for this voice.  Resources for this voice are located in
     * the package <code>com.sun.speech.freetts.en.us</code>.
     */
    protected URL getResource(String resource) {
        return com.sun.speech.freetts.en.us.CMUVoice.class.
            getResource(resource);
    }

    /**
     * Converts this object to a string
     * 
     * @return a string representation of this object
     */
    public String toString() {
	return "MbrolaVoice";
    }
}

