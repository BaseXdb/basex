package org.basex.tests.w3c;

import static org.basex.tests.w3c.QT3Constants.*;
import java.util.HashMap;

import org.basex.core.Context;
import org.basex.tests.w3c.qt3api.XQItem;
import org.basex.tests.w3c.qt3api.XQuery;
import org.basex.util.list.ObjList;

/**
 * Structure for handling test environments.
 */
class QT3Env {
  /** Namespaces: prefix, uri. */
  final ObjList<HashMap<String, String>> namespaces;
  /** Sources: role, file, validation, uri, xml:id. */
  final ObjList<HashMap<String, String>> sources;
  /** Parameters: name, as, select, declared. */
  final ObjList<HashMap<String, String>> params;
  /** Decimal Formats: decimal-separator, grouping-separator,
      digit, pattern-separator, infinity, NaN, per-mille,
      minus-sign, name, percent, zero-digit. */
  final ObjList<HashMap<String, String>> formats;
  /** Schemas: uri, file, xml:id. */
  final HashMap<String, String> schemas;
  /** Collations: uri, default. */
  final HashMap<String, String> collations;
  /** Collection: uri. */
  final String collection;
  /** Static Base URI: uri. */
  final String baseURI;
  /** Name. */
  final String name;

  /**
   * Constructor.
   * @param ctx database context
   * @param itenv environment item
   * @param base base uri
   */
  QT3Env(final Context ctx, final XQItem itenv, final String base) {
    name = XQuery.string("@" + NNAME, itenv, ctx);
    sources = list(ctx, itenv, SOURCE);
    params = list(ctx, itenv, PARAM);
    namespaces = list(ctx, itenv, NAMESPACE);
    formats = list(ctx, itenv, DECIMAL_FORMAT);
    schemas = list(ctx, itenv, SCHEMA).get(0);
    collations = list(ctx, itenv, COLLATION).get(0);
    collection = string(COLLECTION, ctx, itenv);
    baseURI = string(STATIC_BASE_URI, ctx, itenv);

    if(base.isEmpty()) return;
    for(final HashMap<String, String> src : sources) {
     src.put(FILE, base + src.get(FILE));
    }
  }

  /**
   * Returns a list of all attributes of the specified element in a map.
   * @param ctx database context
   * @param itenv root element
   * @param elem element to be parsed
   * @return map list
   */
  ObjList<HashMap<String, String>> list(final Context ctx, final XQItem itenv,
      final String elem) {

    final ObjList<HashMap<String, String>> list =
        new ObjList<HashMap<String, String>>();
    final XQuery query = new XQuery("*:" + elem, ctx).context(itenv);
    for(final XQItem it : query) list.add(map(ctx, it));
    query.close();
    return list;
  }

  /**
   * Returns all attributes of the specified element in a map.
   * @param ctx database context
   * @param itenv root element
   * @return map
   */
  HashMap<String, String> map(final Context ctx, final XQItem itenv) {
    final HashMap<String, String> map = new HashMap<String, String>();
    final XQuery query = new XQuery("@*", ctx).context(itenv);
    for(final XQItem it : query) map.put(it.getName(), it.getString());
    query.close();
    return map;
  }

  /**
   * Returns a single attribute string.
   * @param elem name of element
   * @param ctx database context
   * @param itenv root element
   * @return map
   */
  String string(final String elem, final Context ctx, final XQItem itenv) {
    String value = null;
    final XQuery query = new XQuery("*:" + elem, ctx).context(itenv);
    final XQItem it = query.next();
    if(it != null) {
      final XQuery qattr = new XQuery("string(@*)", ctx).context(it);
      value = qattr.next().getString();
      qattr.close();
    }
    query.close();
    return value;
  }
}
