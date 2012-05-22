package org.basex.tests.w3c;

import static org.basex.tests.w3c.QT3Constants.*;

import java.util.*;

import org.basex.core.*;
import org.basex.tests.bxapi.*;
import org.basex.tests.bxapi.xdm.*;
import org.basex.util.list.*;

/**
 * Driver environment for the {@link QT3TS} test suite driver.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class QT3Env {
  /** Namespaces: prefix, uri. */
  final ArrayList<HashMap<String, String>> namespaces;
  /** Sources: role, file, validation, uri, xml:id. */
  final ArrayList<HashMap<String, String>> sources;
  /** Resources. */
  final ArrayList<HashMap<String, String>> resources;
  /** Parameters: name, as, select, declared. */
  final ArrayList<HashMap<String, String>> params;
  /** Decimal Formats: decimal-separator, grouping-separator,
      digit, pattern-separator, infinity, NaN, per-mille,
      minus-sign, name, percent, zero-digit. */
  final ArrayList<HashMap<String, String>> formats;
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
  QT3Env(final Context ctx, final XdmValue env) {
    name = XQuery.string('@' + NNAME, env, ctx);
    sources = list(ctx, env, SOURCE);
    resources = list(ctx, env, RESOURCE);
    params = list(ctx, env, PARAM);
    namespaces = list(ctx, env, NAMESPACE);
    formats = list(ctx, env, DECIMAL_FORMAT);
    ArrayList<HashMap<String, String>> al = list(ctx, env, SCHEMA);
    schemas = al.isEmpty() ? null : al.get(0);
    al = list(ctx, env, COLLATION);
    collations = al.isEmpty() ? null : al.get(0);
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
    for(final XdmItem iatt : qsrc) collSources.add(iatt.getString());
    qsrc.close();
  }

  /**
   * Returns a list of all attributes of the specified element in a map.
   * @param ctx database context
   * @param env root element
   * @param elem element to be parsed
   * @return map list
   */
  static ArrayList<HashMap<String, String>> list(final Context ctx,
      final XdmValue env, final String elem) {

    final ArrayList<HashMap<String, String>> list =
        new ArrayList<HashMap<String, String>>();
    final XQuery query = new XQuery("*:" + elem, ctx).context(env);
    for(final XdmItem it : query) list.add(map(ctx, it));
    query.close();
    return list;
  }

  /**
   * Returns all attributes of the specified element in a map.
   * @param ctx database context
   * @param env root element
   * @return map
   */
  static HashMap<String, String> map(final Context ctx, final XdmValue env) {
    final HashMap<String, String> map = new HashMap<String, String>();
    final XQuery query = new XQuery("@*", ctx).context(env);
    for(final XdmItem it : query) map.put(it.getName(), it.getString());
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
  static String string(final String elm, final Context ctx, final XdmValue env) {
    String value = null;
    final XQuery query = new XQuery("*:" + elm, ctx).context(env);
    final XdmItem it = query.next();
    if(it != null) {
      final XQuery qattr = new XQuery("string(@*)", ctx).context(it);
      value = qattr.next().getString();
      qattr.close();
    }
    query.close();
    return value;
  }
}
