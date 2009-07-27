package org.basex.api.xmldb;

import static org.basex.Text.*;
import org.basex.core.Prop;

/**
 * This class organizes textual information for the XMLDB API.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 * @author Christian Gruen
 */
interface BXXMLDBText {
  /** DB URI. */
  String DBURI = NAMESPACE + "://";
  /** XMLDB URI. */
  String XMLDB = "xmldb:";
  /** XMLDB URI. */
  String XMLDBURI = XMLDB + DBURI;
  /** Localhost Name. */
  String LOCALHOST = "localhost:" + Integer.parseInt(Prop.PORT[1].toString()) +
  "/";
  /** Conformance Level of the implementation. */
  String CONFORMANCE_LEVEL = "0";

  /** Error Message. */
  String ERR_URI = "Invalid URI: ";
  /** Error Message. */
  String ERR_PROP = "Property could not be set: ";
  /** Error Message. */
  String ERR_BINARY = "Binary resources not supported.";
  /** Error Message. */
  String ERR_TYPE = "Resource type is unknown: ";
  /** Error Message. */
  String ERR_EMPTY = "Resource has no contents.";
  /** Error Message. */
  String ERR_ID = "Resource has no ID.";
  /** Error Message. */
  String ERR_UNKNOWN = "Unknown Resource: ";
  /** Error Message. */
  String ERR_CONT = "Invalid content; string expected.";
  /** Error Message. */
  String ERR_NSURI = "Namespace URI is empty: ";
  /** Error Message. */
  String ERR_RES = "Resource not found: ";
  /** Error Message. */
  String ERR_ITER = "Resource pointer out of range.";
  /** Error Message. */
  String ERR_DOC = "Document ID cannot be retrieved from query result.";
}

