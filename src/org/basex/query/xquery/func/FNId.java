package org.basex.query.xquery.func;

import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.XQTokens.*;
import static org.basex.util.Token.*;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Node;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.NodeIter;
import org.basex.query.xquery.util.Err;
import org.basex.query.xquery.util.NodeBuilder;
import org.basex.util.TokenList;

/**
 * ID functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class FNId extends Fun {
  @Override
  public Iter iter(final XQContext ctx, final Iter[] arg) throws XQException {
    final Iter iter = arg.length == 1 ? check(ctx) : arg[1];
    final Item it = iter.atomic(this, true);
    if(it == null) Err.or(XPEMPTYPE, info(), Type.NOD);

    final Node node = checkNode(it);

    switch(func) {
      case ID:    return id(arg[0], node);
      case IDREF: return idref(arg[0], node);
      case LANG:  return lang(arg[0], node);
      default:    throw new RuntimeException("Not defined: " + func);
    }
  }

  /**
   * Returns the result of the ID function.
   * @param it item ids to be found
   * @param node attribute
   * @return resulting node list
   * @throws XQException xquery exception
   */
  private Iter id(final Iter it, final Node node) throws XQException {
    final NodeBuilder nb = new NodeBuilder(false);
    add(ids(it), nb, node);
    return nb.iter();
  }

  /**
   * Returns the result of the IDREF function.
   * @param it item ids to be found
   * @param node attribute
   * @return resulting node list
   * @throws XQException xquery exception
   */
  private Iter idref(final Iter it, final Node node) throws XQException {
    final NodeBuilder nb = new NodeBuilder(false);
    addRef(ids(it), nb, node);
    return nb.iter();
  }

  /**
   * Returns the result of the language function.
   * @param it item ids to be found
   * @param node attribute
   * @return resulting node list
   * @throws XQException xquery exception
   */
  private Iter lang(final Iter it, final Node node) throws XQException {
    final byte[] lang = lc(checkStr(it));
    Node n = node;
    while(n != null) {
      final NodeIter atts = n.attr();
      Node at;
      while((at = atts.next()) != null) {
        if(!eq(at.qname().str(), LANG)) continue;
        final byte[] ln = lc(norm(checkStr(at)));
        if(startsWith(ln, lang) && (lang.length == ln.length ||
            !letter(ln[lang.length]))) return Bln.TRUE.iter();
      }
      n = n.parent();
    }
    return Bln.FALSE.iter();
  }

  /**
   * Extracts the ids from the specified item.
   * @param iter iterator
   * @return ids
   * @throws XQException evaluation exception
   */
  private byte[][] ids(final Iter iter) throws XQException {
    final TokenList tl = new TokenList();
    Item id;
    while((id = iter.next()) != null) tl.add(split(norm(checkStr(id)), ' '));
    return tl.finish();
  }

  /**
   * Adds nodes with the specified id.
   * @param ids ids to be found
   * @param nb node builder
   * @param nod node
   * @throws XQException evaluation exception
   */
  private void add(final byte[][] ids, final NodeBuilder nb,
      final Node nod) throws XQException {

    final NodeIter ni = nod.attr();
    Node att;
    while((att = ni.next()) != null) {
      // <CG> XQuery/DTD Parsing
      for(final byte[] id : ids) {
        if(!eq(checkStr(att), id)) continue;
        final byte[] nm = lc(att.qname().str());
        if(contains(nm, ID) && !contains(nm, IDREF)) nb.add(nod);
      }
    }
    final NodeIter ch = nod.child();
    while((att = ch.next()) != null) add(ids, nb, att.finish());
  }

  /**
   * Adds nodes with the specified id.
   * @param ids ids to be found
   * @param nb node builder
   * @param nod node
   * @throws XQException evaluation exception
   */
  private void addRef(final byte[][] ids, final NodeBuilder nb,
      final Node nod) throws XQException {

    final NodeIter ni = nod.attr();
    Node att;
    while((att = ni.next()) != null) {
      // <CG> XQuery/DTD Parsing
      for(final byte[] id : ids) {
        if(!eq(checkStr(att), id)) continue;
        final byte[] nm = lc(att.qname().str());
        if(contains(nm, IDREF)) nb.add(att.finish());
      }
    }
    final NodeIter ch = nod.child();
    while((att = ch.next()) != null) addRef(ids, nb, att.finish());
  }
}
