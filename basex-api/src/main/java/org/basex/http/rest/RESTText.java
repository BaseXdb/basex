package org.basex.http.rest;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This class assembles texts which are used in the HTTP classes.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
interface RESTText {
  /** REST URI. */
  byte[] REST_URI = Token.concat(QueryText.BASEX_URI, "/", QueryText.REST_PREFIX);

  /** REST. */
  String REST = "REST";
  /** Attribute. */
  String NAME = "name";
  /** Directory token. */
  byte[] DIR = Token.token("dir");

  /** Name. */
  QNm Q_DATABASES = new QNm("databases", REST_URI);
  /** Name. */
  QNm Q_DATABASE = new QNm("database", REST_URI);
  /** Name. */
  QNm Q_RESOURCE = new QNm("resource", REST_URI);
  /** Dir. */
  QNm Q_DIR = new QNm(DIR, REST_URI);

  /** Commands operation. */
  String COMMANDS = "commands";
  /** Command operation. */
  String COMMAND = "command";
  /** Run operation. */
  String RUN = "run";
  /** Query operation. */
  String QUERY = "query";

  /** Initial context. */
  String CONTEXT = "context";
}
