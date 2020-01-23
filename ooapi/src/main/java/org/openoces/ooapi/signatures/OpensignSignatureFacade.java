package org.openoces.ooapi.signatures;

import org.openoces.ooapi.exceptions.InternalException;

public interface OpensignSignatureFacade {

	String getSigntext() throws InternalException;
	
	String getStylesheetDigest() throws InternalException;

	String getSignatureAlgorithm() throws InternalException;

	String getSignatureValue() throws InternalException;

	String getDigestAlgoritm() throws InternalException;

	String getDigestValue() throws InternalException;

}