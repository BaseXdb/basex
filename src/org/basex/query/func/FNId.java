package org.basex.query.func;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import org.basex.BaseX;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.NodeIter;
import org.basex.query.util.Err;
import org.basex.util.TokenList;

/**
 * ID functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class FNId extends Fun {
  @Override
  public Iter iter(final QueryContext ctx, final Iter[] arg)
      throws QueryException {
    final Iter iter = arg.length == 1 ? checkCtx(ctx) : arg[1];
    final Item it = iter.atomic(this, true);
    if(it == null) Err.or(XPEMPTYPE, info(), Type.NOD);

    final Nod node = checkNode(it);
    switch(func) {
      case ID:    return id(arg[0], node);
      case IDREF: return idref(arg[0], node);
      case LANG:  return lang(arg[0], node);
      default:    BaseX.notexpected(func); return null;
    }
  }

  /**
   * Returns the result of the ID function.
   * @param it item ids to be found
   * @param node attribute
   * @return resulting node list
   * @throws QueryException xquery exception
   */
  private Iter id(final Iter it, final Nod node) throws QueryException {
    final NodIter nb = new NodIter(false);
    add(ids(it), nb, node);
    return nb;
  }

  /**
   * Returns the result of the IDREF function.
   * @param it item ids to be found
   * @param node attribute
   * @return resulting node list
   * @throws QueryException xquery exception
   */
  private Iter idref(final Iter it, final Nod node) throws QueryException {
    final NodIter nb = new NodIter(false);
    addRef(ids(it), nb, node);
    return nb;
  }

  /**
   * Returns the result of the language function.
   * @param it item ids to be found
   * @param node attribute
   * @return resulting node list
   * @throws QueryException xquery exception
   */
  private Iter lang(final Iter it, final Nod node) throws QueryException {
    final byte[] lang = lc(checkStr(it));
    Nod n = node;
    while(n != null) {
      final NodeIter atts = n.attr();
      Nod at;
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
   * @throws QueryException evaluation exception
   */
  private byte[][] ids(final Iter iter) throws QueryException {
    final TokenList tl = new TokenList();
    Item id;
    while((id = iter.next()) != null) {
      for(final byte[] i : split(norm(checkStr(id)), ' ')) tl.add(i);
    }
    return tl.finish();
  }

  /**
   * Adds nodes with the specified id.
   * @param ids ids to be found
   * @param nb node builder
   * @param nod node
   * @throws QueryException evaluation exception
   */
  private void add(final byte[][] ids, final NodIter nb,
      final Nod nod) throws QueryException {

    final NodeIter ni = nod.attr();
    Nod att;
    while((att = ni.next()) != null) {
      // [CG] XQuery/ID-IDREF Parsing
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
   * @throws QueryException evaluation exception
   */
  private void addRef(final byte[][] ids, final NodIter nb,
      final Nod nod) throws QueryException {

    final NodeIter ni = nod.attr();
    Nod att;
    while((att = ni.next()) != null) {
      // [CG] XQuery/ID-IDREF Parsing
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
