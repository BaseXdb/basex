package org.basex.query.func.fn;

import java.math.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 */
public final class FnSystemProperties extends StandardFunc {
  /** XPath version. */
  private static final Dec XPATH_VERSION = Dec.get(BigDecimal.valueOf(4));
  /** XSD version. */
  private static final Dec XSD_VERSION = Dec.get(BigDecimal.valueOf(11, 1));

  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return new MapBuilder().
        put(property("xpath-version"), XPATH_VERSION).
        put(property("xsd-version"), XSD_VERSION).
        put(property("product-name"), Str.get(Prop.NAME)).
        put(property("product-version"), Str.get(Prop.VERSION)).
        put(property("schema-aware"), Bln.FALSE).
        put(property("accepts-typed-data"), Bln.FALSE).
        put(property("supports-xinclude"), Bln.TRUE).
                   // supports-dtd: false, because id/idref is not fully supported
        put(property("supports-dtd"), Bln.FALSE).
        put(property("supports-invisible-xml"), Bln.get(FnInvisibleXml.available())).
        put(property("supports-dynamic-xquery"), Bln.TRUE).
                   // supports-dynamic-xslt: false, because fn:transform is not available
        put(property("supports-dynamic-xslt"), Bln.FALSE).map();
  }

  /**
   * Returns a no-namespace QName for a standardized property name.
   * @param local local name
   * @return QName
   */
  private static QNm property(final String local) {
    return new QNm(local, "");
  }
}
