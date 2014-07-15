package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * ID functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNId extends StandardFunc {
  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNId(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(sc, info, func, args);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    // functions have 1 or 2 arguments...
    final ANode node = checkNode(exprs.length == 2 ? exprs[1] : checkCtx(qc), qc);

    switch(func) {
      case ID:              return id(qc.iter(exprs[0]), node);
      case IDREF:           return idref(qc.iter(exprs[0]), node);
      case ELEMENT_WITH_ID: return elid(qc.iter(exprs[0]), node);
      default:              return super.iter(qc);
    }
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // functions have 1 or 2 arguments...
    final ANode node = checkNode(exprs.length == 2 ? exprs[1] : checkCtx(qc), qc);

    switch(func) {
      case LANG:  return lang(lc(checkEStr(exprs[0], qc)), node);
      default:    return super.item(qc, ii);
    }
  }

  /**
   * Returns the parent result of the ID function.
   * @param it item ids to be found
   * @param node attribute
   * @return resulting node list
   * @throws QueryException query exception
   */
  private Iter elid(final Iter it, final ANode node) throws QueryException {
    return id(it, node);
    /*
    final NodeCache nc = id(it, node);
    final NodeCache res = new NodeCache().random();
    for(ANode n; (n = nc.next()) != null;) res.add(n.parent());
    return res;
    */
  }

  /**
   * Returns the result of the ID function.
   * @param it item ids to be found
   * @param node attribute
   * @return resulting node list
   * @throws QueryException query exception
   */
  private NodeSeqBuilder id(final Iter it, final ANode node) throws QueryException {
    final NodeSeqBuilder nc = new NodeSeqBuilder().check();
    add(ids(it), nc, checkRoot(node), false);
    return nc;
  }

  /**
   * Returns the result of the IDREF function.
   * @param it item ids to be found
   * @param node attribute
   * @return resulting node list
   * @throws QueryException query exception
   */
  private Iter idref(final Iter it, final ANode node) throws QueryException {
    final NodeSeqBuilder nb = new NodeSeqBuilder().check();
    add(ids(it), nb, checkRoot(node), true);
    return nb;
  }

  /**
   * Returns the result of the language function.
   * @param lang language to be found
   * @param node attribute
   * @return resulting node list
   */
  private static Bln lang(final byte[] lang, final ANode node) {
    for(ANode n = node; n != null; n = n.parent()) {
      final AxisIter atts = n.attributes();
      for(ANode at; (at = atts.next()) != null;) {
        if(eq(at.qname().string(), LANG)) {
          final byte[] ln = lc(norm(at.string()));
          return Bln.get(startsWith(ln, lang) &&
              (lang.length == ln.length || ln[lang.length] == '-'));
        }
      }
    }
    return Bln.FALSE;
  }

  /**
   * Extracts the ids from the specified iterator.
   * @param iter iterator
   * @return ids
   * @throws QueryException query exception
   */
  private byte[][] ids(final Iter iter) throws QueryException {
    final TokenList tl = new TokenList();
    for(Item id; (id = iter.next()) != null;) {
      for(final byte[] i : split(norm(checkEStr(id)), ' ')) tl.add(i);
    }
    return tl.toArray();
  }

  /**
   * Adds nodes with the specified id.
   * @param ids ids to be found
   * @param idref idref flag
   * @param nc node cache
   * @param node node
   */
  private static void add(final byte[][] ids, final NodeSeqBuilder nc, final ANode node,
      final boolean idref) {
    AxisIter ai = node.attributes();
    for(ANode at; (at = ai.next()) != null;) {
      final byte[][] val = split(at.string(), ' ');
      // [CG] XQuery: ID-IDREF Parsing
      for(final byte[] id : ids) {
        if(!eq(id, val)) continue;
        final byte[] nm = lc(at.qname().string());
        final boolean ii = contains(nm, ID), ir = contains(nm, IDREF);
        if(idref ? ir : ii && !ir) nc.add(idref ? at.finish() : node);
      }
    }
    ai = node.children();
    for(ANode att; (att = ai.next()) != null;) add(ids, nc, att.finish(), idref);
  }

  /**
   * Checks if the specified node has a document node as root.
   * @param node input node
   * @return specified node
   * @throws QueryException query exception
   */
  private ANode checkRoot(final ANode node) throws QueryException {
    if(node instanceof FNode) {
      ANode n = node;
      while(n.type != NodeType.DOC) {
        n = n.parent();
        if(n == null) throw IDDOC.get(info);
      }
    }
    return node;
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.CTX && exprs.length == 1 || super.has(flag);
  }
}
