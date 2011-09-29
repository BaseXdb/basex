package org.basex.api.rest;

import static org.basex.util.Token.*;
import org.basex.core.Text;

/**
 * This class assembles texts which are used in the HTTP classes.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public interface RESTText {
  /** REST string.  */
  byte[] REST = token("rest");
  /** REST URI. */
  byte[] RESTURI = concat(token(Text.URL), SLASH, REST);

  /** Element. */
  byte[] DATABASES = concat(REST, COLON, token("databases"));
  /** Element. */
  byte[] DATABASE = concat(REST, COLON, token("database"));
  /** Element. */
  byte[] RESOURCE = concat(REST, COLON, token("resource"));
  /** Attribute. */
  byte[] RESOURCES = token("resources");

  /** Error message. */
  String ERR_UNEXPECTED = "Unexpected error: ";
  /** Error message. */
  String ERR_PARAM = "Unknown parameter: ";
  /** Error message. */
  String ERR_NOPATH = "No path specified.";
  /** Error message. */
  String ERR_NORES = "Path does not exist.";
  /** Error message. */
  String ERR_NOPARAM = "No parameters supported here.";
  /** Error message. */
  String ERR_ONLYONE = "Only one operation can be specified.";
  /** Error message. */
  String ERR_CTXITEM = "Multiple context items specified.";

  /** Command operation. */
  String COMMAND = "command";
  /** Run operation. */
  String RUN = "run";
  /** Query operation. */
  String QUERY = "query";

  /** Wrap parameter. */
  String WRAP = "wrap";
  /** Initial context. */
  String CONTEXT = "context";
}
