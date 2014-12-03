package org.basex.http.rest;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This class assembles texts which are used in the HTTP classes.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
interface RESTText {
  /** REST URI. */
  String REST_PREFIX = Token.string(QueryText.REST_PREFIX);
  /** REST URI. */
  String REST_URI = new TokenBuilder(Prop.URL).add('/').add(QueryText.REST_PREFIX).toString();

  /** Name. */
  QNm Q_DATABASES = QNm.get(REST_PREFIX, "databases", REST_URI);
  /** Name. */
  QNm Q_DATABASE = QNm.get(REST_PREFIX, "database", REST_URI);
  /** Name. */
  QNm Q_RESOURCE = QNm.get(REST_PREFIX, "resource", REST_URI);

  /** Attribute. */
  String RESOURCES = "resources";
  /** Attribute. */
  String NAME = "name";

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
