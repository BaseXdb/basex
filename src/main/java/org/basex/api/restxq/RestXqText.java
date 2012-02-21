package org.basex.api.restxq;

import static org.basex.util.Token.*;

import org.basex.query.item.*;
import org.basex.util.*;

/**
 * This class assembles texts which are used in the HTTP classes.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public interface RestXqText {
  /** RESTful annotation URI. */
  byte[] RESTXQURI = token("http://exquery.org/ns/rest/annotation/");

  /** Path annotation. */
  QNm PATH = new QNm(Token.token("path"), RESTXQURI);
  /** GET annotation. */
  QNm GET = new QNm(Token.token("GET"), RESTXQURI);
  /** POST annotation. */
  QNm POST = new QNm(Token.token("POST"), RESTXQURI);
  /** PUT annotation. */
  QNm PUT = new QNm(Token.token("PUT"), RESTXQURI);
  /** DELETE annotation. */
  QNm DELETE = new QNm(Token.token("DELETE"), RESTXQURI);

  /** Error message. */
  String ERR_UNEXPECTED = "Unexpected error: ";
  /** Error message. */
  String ERR_NOT_FOUND = "Path not found.";
}
