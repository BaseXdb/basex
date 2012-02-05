package org.basex.tests.w3c;

import static org.basex.tests.w3c.QT3Constants.*;
import java.util.HashMap;

import org.basex.core.Context;
import org.basex.tests.w3c.qt3api.XQItem;
import org.basex.tests.w3c.qt3api.XQValue;
import org.basex.tests.w3c.qt3api.XQuery;
import org.basex.util.list.ObjList;
import org.basex.util.list.StringList;

/**
 * Driver environment for the {@link QT3TS} test suite driver.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
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
  /** Static Base URI: uri. */
  final String baseURI;
  /** Name. */
  final String name;

  /** Collection uri. */
  final String collURI;
  /** Collection context flag. */
  final boolean collContext;
  /** Collection sources. */
  final StringList collSources;

  /**
   * Constructor.
   * @param ctx database context
   * @param env environment item
   */
  QT3Env(final Context ctx, final XQValue env) {
    name = XQuery.string('@' + NNAME, env, ctx);
    sources = list(ctx, env, SOURCE);
    params = list(ctx, env, PARAM);
    namespaces = list(ctx, env, NAMESPACE);
    formats = list(ctx, env, DECIMAL_FORMAT);
    schemas = list(ctx, env, SCHEMA).get(0);
    collations = list(ctx, env, COLLATION).get(0);
    baseURI = string(STATIC_BASE_URI, ctx, env);

    // collections
    collURI = XQuery.string("*:collection/@uri", env, ctx);

    final XQuery cc = new XQuery("*:collection/*:source/@role = '.'", ctx).
        context(env);
    collContext = cc.next().getBoolean();
    cc.close();

    collSources = new StringList();
    final XQuery qsrc = new XQuery("*:collection/*:source/@file", ctx).
        context(env);
    for(final XQItem iatt : qsrc) collSources.add(iatt.getString());
    qsrc.close();
  }

  /**
   * Returns a list of all attributes of the specified element in a map.
   * @param ctx database context
   * @param env root element
   * @param elem element to be parsed
   * @return map list
   */
  ObjList<HashMap<String, String>> list(final Context ctx, final XQValue env,
      final String elem) {

    final ObjList<HashMap<String, String>> list =
        new ObjList<HashMap<String, String>>();
    final XQuery query = new XQuery("*:" + elem, ctx).context(env);
    for(final XQItem it : query) list.add(map(ctx, it));
    query.close();
    return list;
  }

  /**
   * Returns all attributes of the specified element in a map.
   * @param ctx database context
   * @param env root element
   * @return map
   */
  static HashMap<String, String> map(final Context ctx, final XQValue env) {
    final HashMap<String, String> map = new HashMap<String, String>();
    final XQuery query = new XQuery("@*", ctx).context(env);
    for(final XQItem it : query) map.put(it.getName(), it.getString());
    query.close();
    return map;
  }

  /**
   * Returns a single attribute string.
   * @param elm name of element
   * @param ctx database context
   * @param env root element
   * @return map
   */
  static String string(final String elm, final Context ctx, final XQValue env) {
    String value = null;
    final XQuery query = new XQuery("*:" + elm, ctx).context(env);
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
