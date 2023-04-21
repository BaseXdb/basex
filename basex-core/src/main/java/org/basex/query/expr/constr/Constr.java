package org.basex.query.expr.constr;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Node constructor.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class Constr {
  /** Error: attribute position. */
  public QNm errAtt;
  /** Error: duplicate attribute. */
  public QNm duplAtt;
  /** Error: namespace position. */
  QNm errNS;
  /** Error: duplicate namespace. */
  byte[] duplNS;

  /** Node builder. */
  private final FBuilder builder;
  /** Query context. */
  private final QueryContext qc;
  /** Static context. */
  private final StaticContext sc;
  /** Input information. */
  private final InputInfo info;
  /** Text cache. */
  private final TokenBuilder text = new TokenBuilder();
  /** Space separator flag. */
  private boolean more;

  /**
   * Creates the children of the constructor.
   * @param builder node builder
   * @param info input info
   * @param sc static context
   * @param qc query context
   */
  public Constr(final FBuilder builder, final InputInfo info, final StaticContext sc,
      final QueryContext qc) {
    this.builder = builder;
    this.info = info;
    this.sc = sc;
    this.qc = qc;
  }

  /**
   * Constructs child and attribute nodes.
   * @param exprs input expressions
   * @return self reference
   * @throws QueryException query exception
   */
  public Constr add(final Expr... exprs) throws QueryException {
    final int size = sc.ns.size();
    try {
      final QNmSet qnames = new QNmSet();
      for(final Expr expr : exprs) {
        more = false;
        final Iter iter = expr.iter(qc);
        for(Item item; (item = qc.next(iter)) != null && add(item, qnames););
      }
      builder.add(qc.pool.token(text.toArray()));
      return this;
    } finally {
      sc.ns.size(size);
    }
  }

  /**
   * Recursively adds nodes to the element arrays.
   * @param item current item
   * @param qnames assigned attributes (required for duplicate check)
   * @return true if item was added
   * @throws QueryException query exception
   */
  private boolean add(final Item item, final QNmSet qnames) throws QueryException {
    if(item instanceof XQArray) {
      for(final Value value : ((XQArray) item).members()) {
        for(final Item it : value) {
          if(!add(it, qnames)) return false;
        }
      }
      return true;
    }

    if(item instanceof FItem) throw CONSFUNC_X.get(info, item);

    if(item instanceof ANode) {
      // type: nodes
      final ANode node = (ANode) item;

      final Type type = item.type;
      if(type == NodeType.TEXT) {
        // type: text node
        text.add(node.string());

      } else if(type == NodeType.ATTRIBUTE) {
        // type: attribute node

        // check if attribute is specified after texts or child nodes
        final QNm name = node.qname();
        if(!text.isEmpty() || builder.children != null) {
          errAtt = name;
          return false;
        }
        // check for duplicate attribute names
        if(!qnames.add(name)) {
          duplAtt = name;
          return false;
        }
        // add attribute
        builder.add(name, qc.pool.token(node.string()));
        // add new namespace
        if(name.hasURI()) sc.ns.add(name.prefix(), name.uri());

      } else if(type == NodeType.NAMESPACE_NODE) {
        // type: namespace node

        // no attribute allowed after texts or child nodes
        if(!text.isEmpty() || builder.children != null) {
          errNS = node.qname();
          return false;
        }

        // add namespace
        final byte[] name = node.name(), uri = node.string();
        final byte[] knownUri = builder.namespaces == null ? null : builder.namespaces.value(name);
        if(knownUri == null) {
          builder.addNS(name, uri);
        } else if(!Token.eq(uri, knownUri)) {
          // duplicate namespace (ignore duplicates with same uri)
          duplNS = name;
          return false;
        }

      } else if(type == NodeType.DOCUMENT_NODE) {
        // type: document node

        final BasicNodeIter iter = node.childIter();
        for(Item it; (it = qc.next(iter)) != null && add(it, qnames););

      } else {
        // type: element/comment/processing instruction node

        // add text node
        builder.add(qc.pool.token(text.next()));
        final boolean keep = !qc.context.options.get(MainOptions.COPYNODE);
        builder.add(node.materialize(n -> keep, info, qc));
      }
      more = false;
    } else {
      // type: atomic value
      if(more) text.add(' ');
      text.add(item.string(info));
      more = true;

    }
    return true;
  }

  /**
   * Enrich and assign in-scope namespaces.
   * @param inscopeNS in-scope namespaces
   * @param nm element name
   * @throws QueryException query exception
   */
  void namespaces(final Atts inscopeNS, final QNm nm) throws QueryException {
    final Atts namespaces = builder.namespaces;
    final int ns = namespaces == null ? 0 : namespaces.size();

    final byte[] nmPrefix = nm.prefix(), nmUri = nm.uri();
    if(!eq(nmPrefix, XML)) {
      // check declaration of default namespace
      final int defaultNS = ns != 0 ? namespaces.get(EMPTY) : -1;
      if(defaultNS != -1) {
        if(!nm.hasURI()) throw EMPTYNSCONS.get(info);
        final int scope = inscopeNS.get(EMPTY);
        final byte[] scopeUri = scope != -1 ? inscopeNS.value(scope) : sc.ns.uri(EMPTY);
        final byte[] uri = namespaces.value(defaultNS);
        if(scopeUri != null && !eq(scopeUri, uri)) throw DUPLNSCONS_X.get(info, uri);
      }
      // add new namespace to in-scope namespaces
      if(nm.hasURI() && !inscopeNS.contains(nmPrefix)) inscopeNS.add(nmPrefix, nmUri);
    }
    for(int n = 0; n < ns; n++) {
      addNS(namespaces.name(n), namespaces.value(n), inscopeNS);
    }

    final ANodeList attributes = builder.attributes;
    if(attributes != null) {
      final int as = attributes.size();
      for(int a = 0; a < as; a++) {
        final ANode attr = attributes.get(a);
        final QNm qnm = attr.qname();
        // skip attributes without prefixes or URIs
        if(!qnm.hasPrefix() || !qnm.hasURI()) continue;

        // skip XML namespace
        final byte[] prefix = qnm.prefix();
        if(eq(prefix, XML)) continue;

        final byte[] auri = qnm.uri(), npref = addNS(prefix, auri, inscopeNS);
        if(npref != null) {
          final QNm aname = qc.pool.qnm(concat(npref, COLON, qnm.local()), auri);
          attributes.set(a, new FAttr(aname, qc.pool.token(attr.string())));
        }
      }
    }
    builder.namespaces = inscopeNS.isEmpty() ? null : inscopeNS;
  }

  /**
   * Adds the specified namespace to the namespace array.
   * If the prefix is already used for another URI, a new name is generated.
   * @param prefix prefix
   * @param uri uri
   * @param inscopeNS in-scope namespaces
   * @return resulting prefix or {@code null}
   */
  private static byte[] addNS(final byte[] prefix, final byte[] uri, final Atts inscopeNS) {
    final byte[] u = inscopeNS.value(prefix);
    if(u == null) {
      // add undeclared namespace
      inscopeNS.add(prefix, uri);
    } else if(!eq(u, uri)) {
      // prefixes with different URIs exist; new one must be replaced
      byte[] pref = null;
      // check if one of the existing prefixes can be adopted
      final int ns = inscopeNS.size();
      for(int n = 0; n < ns; n++) {
        if(eq(inscopeNS.value(n), uri)) pref = inscopeNS.name(n);
      }
      // if negative, generate a new one that is not used yet
      if(pref == null) {
        int i = 1;
        do {
          pref = concat(prefix, "_", i++);
        } while(inscopeNS.contains(pref));
        inscopeNS.add(pref, uri);
      }
      return pref;
    }
    return null;
  }
}
