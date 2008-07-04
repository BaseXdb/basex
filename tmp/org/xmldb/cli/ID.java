package org.xmldb.cli;

/*
 * Copyright (c) 2000 The XML:DB Initiative. All Rights Reserved.
 *
 * This XML:DB work (including software, documents, or other related 
 * items) is being provided by the copyright holders under the following
 * license. By obtaining, using and/or copying this work, you (the 
 * licensee) agree that you have read, understood, and will comply with 
 * the following terms and conditions: 
 *
 * Permission to use, copy, and modify this software and its 
 * documentation, with or without modification, for any purpose and 
 * without fee or royalty is hereby granted, provided that you include 
 * the following on ALL copies of the software and documentation or 
 * portions thereof, including modifications, that you make: 
 *
 *    1. The full text of this NOTICE in a location viewable to users 
 *       of the redistributed or derivative work. 
 *    2. Any pre-existing intellectual property disclaimers, notices, 
 *       or terms and conditions. 
 *    3. Notice of any changes or modifications to the XML:DB files, 
 *       including the date changes were made. (We recommend you provide 
 *       URIs to the location from which the code is derived.) 
 *
 * THIS SOFTWARE AND DOCUMENTATION IS PROVIDED "AS IS," AND COPYRIGHT 
 * HOLDERS MAKE NO REPRESENTATIONS OR WARRANTIES, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO, WARRANTIES OF MERCHANTABILITY OR FITNESS
 * FOR ANY PARTICULAR PURPOSE OR THAT THE USE OF THE SOFTWARE OR 
 * DOCUMENTATION WILL NOT INFRINGE ANY THIRD PARTY PATENTS, COPYRIGHTS, 
 * TRADEMARKS OR OTHER RIGHTS. 
 *
 * COPYRIGHT HOLDERS WILL NOT BE LIABLE FOR ANY DIRECT, INDIRECT, SPECIAL 
 * OR CONSEQUENTIAL DAMAGES ARISING OUT OF ANY USE OF THE SOFTWARE OR 
 * DOCUMENTATION. 
 *
 * The name and trademarks of copyright holders may NOT be used in 
 * advertising or publicity pertaining to the software without specific,
 * written prior permission. Title to copyright in this software and any 
 * associated documentation will at all times remain with copyright 
 * holders. 
 *
 * $Id: ID.java,v 1.3 2000/11/16 20:46:15 kstaken Exp $
 */

/**
 * ID is the generic representation of a Unique Identifier.  An ID's
 * underlying implementation is vendor specific, and an ID should not
 * necessarily be considered globally unique.
 */

public interface ID {
   /**
    * equalTo compares this ID to another ID and returns whether
    * or not the two are identical.
    *
    * @param id The compared ID
    * @return If they are identical
    */
   boolean equalTo(ID id);
}
