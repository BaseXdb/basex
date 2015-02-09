package org.basex.tests.w3c;

import static org.basex.tests.w3c.QT3Constants.*;

import java.util.*;

import javax.xml.namespace.*;

import org.basex.core.*;
import org.basex.query.value.item.*;
import org.basex.tests.bxapi.*;
import org.basex.tests.bxapi.xdm.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Driver environment for the {@link QT3TS} test suite driver.
 *
 * @author BaseX Team 2005-15, BSD License
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
  /** Schemas: uri, file, xml:id. */
  final HashMap<String, String> schemas;
  /** Collations: uri, default. */
  final HashMap<String, String> collations;
  /** Decimal Formats: decimal-separator, grouping-separator,
      digit, pattern-separator, infinity, NaN, per-mille,
      minus-sign, name, percent, zero-digit. */
  final HashMap<QName, HashMap<String, String>> decFormats;
  /** Static Base URI: uri. */
  final String baseURI;
  /** Name. */
  final String name;

  /** Collection uri. */
  final String collURI;
  /** Initial context item. */
  final XdmValue context;
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
    ArrayList<HashMap<String, String>> al = list(ctx, env, SCHEMA);
    schemas = al.isEmpty() ? null : al.get(0);
    al = list(ctx, env, COLLATION);
    collations = al.isEmpty() ? null : al.get(0);
    final String uri = string(STATIC_BASE_URI, ctx, env);
    baseURI = "#UNDEFINED".equals(uri) ? "" : uri;

    // collections
    collURI = XQuery.string("*:collection/@uri", env, ctx);

    collContext = new XQuery("*:collection/*:source/@role = '.'", ctx).
        context(env).value().getBoolean();

    collSources = new StringList();
    for(final XdmItem iatt : new XQuery("*:collection/*:source/@file", ctx).context(env))
      collSources.add(iatt.getString());

    decFormats = new HashMap<>();
    for(final XdmItem it : new XQuery("*:decimal-format", ctx).context(env)) {
      final XdmValue it1 = new XQuery(
        "for $n in @name " +
        "let $b := substring-before($n, ':') " +
        "return QName(if($b) then namespace-uri-for-prefix($b, .) else '', $n)",
        ctx).context(it).value();
      final HashMap<String, String> hm = new HashMap<>();
      final QNm qnm = it1.size() != 0 ? (QNm) it1.internal() : new QNm(Token.EMPTY);
      decFormats.put(qnm.toJava(), hm);
      for(final XdmItem it2 : new XQuery("@*[name() != 'name']", ctx).context(it)) {
        hm.put(it2.getName().getLocalPart(), it2.getString());
      }
    }

    final String c = XQuery.string("*:context-item/@select", env, ctx);
    context = c.isEmpty() ? null : new XQuery(c, ctx).value();
  }

  /**
   * Returns a list of all attributes of the specified element in a map.
   * @param ctx database context
   * @param env root element
   * @param elem element to be parsed
   * @return map list
   */
  static ArrayList<HashMap<String, String>> list(final Context ctx, final XdmValue env,
      final String elem) {

    final ArrayList<HashMap<String, String>> list = new ArrayList<>();
    for(final XdmItem it : new XQuery("*:" + elem, ctx).context(env)) {
      list.add(map(ctx, it));
    }
    return list;
  }

  /**
   * Returns all attributes of the specified element in a map.
   * @param ctx database context
   * @param env root element
   * @return map
   */
  static HashMap<String, String> map(final Context ctx, final XdmValue env) {
    final HashMap<String, String> map = new HashMap<>();
    for(final XdmItem it : new XQuery("@*", ctx).context(env))
      map.put(it.getName().getLocalPart(), it.getString());
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
    final XdmItem it = new XQuery("*:" + elm, ctx).context(env).next();
    return it == null ? null :
      new XQuery("string(@*)", ctx).context(it).next().getString();
  }
}
