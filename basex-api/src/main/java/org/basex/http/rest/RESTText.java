package org.basex.http.rest;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This class assembles texts which are used in the HTTP classes.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
interface RESTText {
  /** REST URI. */
  byte[] REST_URI = Token.concat(QueryText.BASEX_URI, "/", QueryText.REST_PREFIX);

  /** Name. */
  QNm Q_DATABASES = new QNm(QueryText.REST_PREFIX, "databases", REST_URI);
  /** Name. */
  QNm Q_DATABASE = new QNm(QueryText.REST_PREFIX, "database", REST_URI);
  /** Name. */
  QNm Q_RESOURCE = new QNm(QueryText.REST_PREFIX, "resource", REST_URI);

  /** REST. */
  String REST = "REST";
  /** Attribute. */
  String RESOURCES = "resources";
  /** Attribute. */
  String NAME = "name";

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
