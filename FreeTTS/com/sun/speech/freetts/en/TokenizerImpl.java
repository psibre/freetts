/**
 * Portions Copyright 2001 Sun Microsystems, Inc.
 * Portions Copyright 1999-2001 Language Technologies Institute, 
 * Carnegie Mellon University.
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 */
package com.sun.speech.freetts.en;

import com.sun.speech.freetts.Token;
import com.sun.speech.freetts.Tokenizer;
import java.io.Reader;
import java.io.IOException;


/**
 * Implements the tokenizer interface. Breaks an input sequence of
 * characters into a set of tokens.
 */
public class TokenizerImpl implements Tokenizer {
        
    /** A constant indicating that the end of the stream has been read. */
    public static final int EOF = -1;
    
    /** A string containing the default whitespace characters. */
    public static final String DEFAULT_WHITESPACE_SYMBOLS = " \t\n\r";
    
    /** A string containing the default single characters. */
    public static final String DEFAULT_SINGLE_CHAR_SYMBOLS = "(){}[]";
    
    /** A string containing the default pre-punctuation characters. */
    public static final String DEFAULT_PREPUNCTUATION_SYMBOLS = "\"'`({[";
    
    /** A string containing the default post-punctuation characters. */
    public static final String DEFAULT_POSTPUNCTUATION_SYMBOLS 
	= "\"'`.,:;!?(){}[]";


    // the line number
    private int lineNumber = 0;
    
    // the input text (from the Utterance) to tokenize
    private String inputText = null;

    // the file to read input text from, if using file mode
    private Reader reader = null;

    // the token position - doesn't seem really necessary at this point
    // private int tokenPosition = 0;

    // the current character, whether its from the file or the input text
    private int currentChar = 0;
    
    // the current char position for the input text (not the file)
    // this is called "file_pos" in flite
    private int currentPosition = 0;
    
    
    // the delimiting symbols of this tokenizer
    private String whitespaceSymbols = DEFAULT_WHITESPACE_SYMBOLS;
    private String singleCharSymbols = DEFAULT_SINGLE_CHAR_SYMBOLS;
    private String prepunctuationSymbols = DEFAULT_PREPUNCTUATION_SYMBOLS;
    private String postpunctuationSymbols = DEFAULT_POSTPUNCTUATION_SYMBOLS;

    // The error description
    private String errorDescription = null;
    
    // a place to store the current token
    private Token token;
    private Token lastToken = null;

    // for timing
    private long duration = 0;
        

    /**
     * Constructs a Tokenizer.
     */
    public TokenizerImpl() {
    }


    /**
     * Creates a tokenizer that will return tokens from
     * the given string.
     *
     * @param string the string to tokenize
     */
    public TokenizerImpl(String string) {
	setInputText(string);
    }

    /**
     * Creates a tokenizer that will return tokens from
     * the given file.
     *
     * @param file where to read the input from
     */
    public TokenizerImpl(Reader file) {
	setInputReader(file);
    }


    /**
     * Sets the whitespace symbols of this Tokenizer to the given symbols.
     *
     * @param symbols the whitespace symbols
     */
    public void setWhitespaceSymbols(String symbols) {
	whitespaceSymbols = symbols;
    }
        

    /**
     * Sets the single character symbols of this Tokenizer to the given
     * symbols.
     *
     * @param symbols the single character symbols
     */
    public void setSingleCharSymbols(String symbols) {
	singleCharSymbols = symbols;
    }
        

    /**
     * Sets the prepunctuation symbols of this Tokenizer to the given
     * symbols.
     *
     * @param symbols the prepunctuation symbols
     */
    public void setPrepunctuationSymbols(String symbols) {
	prepunctuationSymbols = symbols;
    }
        

    /**
     * Sets the postpunctuation symbols of this Tokenizer to the given
     * symbols.
     *
     * @param symbols the postpunctuation symbols
     */
    public void setPostpunctuationSymbols(String symbols) {
	postpunctuationSymbols = symbols;
    }
    

    /**
     * Sets the text to tokenize. 
     *
     * @param  inputString  the string to tokenize
     */
    public void setInputText(String inputString) {
	inputText = inputString;
	currentPosition = 0;
	
	if (inputText != null) {
	    getNextChar();
	}
    }

    /**
     * Sets the input reader
     *
     * @param  reader the input source
     */
    public void setInputReader(Reader reader) {
	this.reader = reader;
	getNextChar();
    }
    

    /**
     * Returns the next token.
     *
     * @return  the next token if it exists,
     *          <code>null</code> if no more tokens
     */
    public Token getNextToken() {
	lastToken = token;
	token = new Token();
	
	token.setLineNumber(lineNumber);
	
	// Skip whitespace
	token.setWhitespace(getTokenSubpart(whitespaceSymbols));
	
	// quoted strings currently ignored
	
	// get prepunctuation
	token.setPrepunctuation(getTokenSubpart(prepunctuationSymbols));
	
	// get the symbol itself
	if (singleCharSymbols.indexOf(currentChar) != -1) {
	    token.setWord(String.valueOf((char) currentChar));
	    getNextChar();
	} else {
	    token.setWord(getTokenSubpart2(whitespaceSymbols));
	}

	token.setPosition(currentPosition);
	
	// This'll have token *plus* postpunctuation
	// Get postpunctuation
	removeTokenPostpunctuation();
	
	return token;
    }
    

    /**
     * Returns <code>true</code> if there are more tokens,
     * 		<code>false</code> otherwise.
     *
     * @return <code>true</code> if there are more tokens
     *         <code>false</code> otherwise
     */
    public boolean hasMoreTokens() {
	int nextChar = currentChar;
	return (nextChar != EOF);
    }
    

    /**
     * Advances the currentPosition pointer by 1 (if not exceeding
     * length of inputText, and returns the character pointed by
     * currentPosition.
     *
     * @return the next character EOF if no more characters exist
     */
    private int getNextChar() {
	if (reader != null) {
	    try {
	        int readVal  = reader.read();
		if (readVal == -1) {
		    currentChar = EOF;
		} else {
		    currentChar = (char) readVal;
		}
	    } catch (IOException ioe) {
		currentChar = EOF;
		errorDescription = ioe.getMessage();
	    }
	} else if (inputText != null) {
	    if (currentPosition < inputText.length()) {
		currentChar = (int) inputText.charAt(currentPosition);
	    } else {
		currentChar = EOF;
	    }
	}
	if (currentChar != EOF) {
	    currentPosition++;
	}
	if (currentChar == '\n') {
	    lineNumber++;
	}
	return currentChar;
    }
    

    /**
     * Starting from the current position of the input text,
     * returns the subsequent characters of type charClass,
     * and not of type singleCharSymbols.
     *
     * @param  charClass  the type of characters to look for
     * @param  buffer  the place to append characters of type charClass
     *
     * @return  a string of characters starting from the current position
     *          of the input text, until it encounters a character not
     *          in the string charClass
     *
     */
    private String getTokenSubpart(String charClass) {
	return getTokenSubpartMeat(charClass, true);
    }

    /**
     * Starting from the current position of the input text/file,
     * returns the subsequent characters, not of type singleCharSymbols,
     * and ended at characters of type endingCharClass.  E.g., if the current
     * string is "xxxxyyy", endingCharClass is "yz", and singleCharClass
     * "abc". Then this method will return to "xxxx".
     *
     * @param  endingCharClass  the type of characters to look for
     *
     * @return  a string of characters from the current position until
     *          it encounters characters in endingCharClass
     *
     */
    private String getTokenSubpart2(String endingCharClass) {
	return getTokenSubpartMeat(endingCharClass, false);
    }
    
    /**
     * Provides a `compressed' method from getTokenSubpart() and 
     * getTokenSubpart2().
     * If parameter containThisCharClass is <code>true</code>, 
     * then a string from the
     * current position to the last character in charClass is returned.
     * If containThisCharClass is <code>false</code>, then a string 
     * before the first
     * occurrence of a character in containThisCharClass is returned.
     *
     * @param  charClass  the string of characters you want included or
     *                    excluded in your return
     * @param  containThisCharClass  determines if you want characters
     *                in charClass in the returned string or not
     *
     * @return  a string of characters from the current position until
     *          it encounters characters in endingCharClass
     */
    private String getTokenSubpartMeat(String charClass, 
	    			boolean containThisCharClass) {	
	StringBuffer buffer = new StringBuffer();
	
	// if we want the returned string to contain chars in charClass, then
	// containThisCharClass is TRUE and
	// (charClass.indexOf(currentChar) != 1) == containThisCharClass)
	// returns true; if we want it to stop at characters of charClass,
	// then containThisCharClass is FALSE, and the condition returns
	// false.
	while ((charClass.indexOf(currentChar) != -1)
	       == containThisCharClass  &&
	       singleCharSymbols.indexOf(currentChar) == -1 &&
	       currentChar != EOF) {
	    buffer.append((char) currentChar);
	    getNextChar();
	}
	return buffer.toString();
    }
    
    /**
     * Removes the postpunctuation characters from the current token.
     * Copies those postpunctuation characters to the class
     * variable 'postpunctuation'.
     */
    private void removeTokenPostpunctuation() {
	if (token != null) {
	    String tokenWord = token.getWord();

	    int tokenLength = tokenWord.length();
	    int position = tokenLength - 1;
	    
	    while (position > 0 &&
		   postpunctuationSymbols.indexOf
		   ((int)tokenWord.charAt(position)) != -1) {
		position--;
	    }
	    
	    if (tokenLength - 1 != position) {
		// Copy postpunctuation from token
		token.setPostpunctuation( tokenWord.substring(position+1));
		
		// truncate token at postpunctuation
		token.setWord(tokenWord.substring(0, position+1));
	    } else {
		token.setPostpunctuation("");
	    }
	}
    }

    /**
     * Returns <code>true</code> if there were errors while reading tokens
     *
     * @return <code>true</code> if there were errors;
     * 		<code>false</code> otherwise
     */
    public boolean hasErrors() {
	return errorDescription != null;
    }

    /**
     * if hasErrors returns <code>true</code>, this will return a 
     * description of the error encountered, otherwise
     * it will return <code>null</code>
     *
     * @return a description of the last error that occurred.
     */
    public String getErrorDescription() {
	return errorDescription;
    }

    /**
     * Determines if the current token should start a new sentence.
     *
     * @return <code>true</code> if a new sentence should be started
     */
    public boolean isBreak() {
	if (lastToken == null || token == null) {
	    return false;
	} else if (token.getWhitespace().indexOf('\n') !=
	    token.getWhitespace().lastIndexOf('\n')) {
	    return true;
	} else  if (lastToken.getPostpunctuation().indexOf(':') != -1 ||
	            lastToken.getPostpunctuation().indexOf('?') != -1 ||
	            lastToken.getPostpunctuation().indexOf('!') != -1) {
	    return true;
    	} else if (lastToken.getPostpunctuation().indexOf('.') != -1 &&
	           token.getWhitespace().length() > 1 &&
		   Character.isUpperCase(token.getWord().charAt(0))) {
	    return true;
    	} else if (lastToken.getPostpunctuation().indexOf('.') != -1 &&
		   Character.isUpperCase(token.getWord().charAt(0)) &&
		       !Character.isUpperCase(
			    lastToken.getWord().charAt(
			    lastToken.getWord().length() - 1))) {
	    return true;
	}
	return false;
    }
}

