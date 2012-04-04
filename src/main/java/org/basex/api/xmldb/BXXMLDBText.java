package org.basex.api.xmldb;

import static org.basex.core.Text.*;

/**
 * This class organizes textual information for the XMLDB API.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
interface BXXMLDBText {
  /** DB URI. */
  String DBURI = NAMELC + "://";
  /** XMLDB URI. */
  String XMLDB = "xmldb:";
  /** XMLDB URI. */
  String XMLDBURI = XMLDB + DBURI;
  /** Conformance level of the implementation. */
  String CONFORMANCE_LEVEL = "0";

  /** Error message. */
  String ERR_URI = "Invalid URI: ";
  /** Error message. */
  String ERR_PROP = "Property could not be set: ";
  /** Error message. */
  String ERR_BINARY = "Binary resources not supported.";
  /** Error message. */
  String ERR_TYPE = "Resource type is unknown: ";
  /** Error message. */
  String ERR_EMPTY = "Resource has no contents.";
  /** Error message. */
  String ERR_ID = "Resource has no ID.";
  /** Error message. */
  String ERR_UNKNOWN = "Unknown Resource: ";
  /** Error message. */
  String ERR_CONT = "Content cannot be set.";
  /** Error message. */
  String ERR_NSURI = "Namespace URI is empty: ";
  /** Error message. */
  String ERR_RES = "Resource not found: ";
  /** Error message. */
  String ERR_ITER = "Resource pointer out of range.";
  /** Error message. */
  String ERR_DOC = "Document ID cannot be retrieved from query result.";
  /** Error message. */
  String ERR_LOCK = "Database cannot be marked as 'updating'.";
}
