package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.FDoc;
import org.basex.query.xquery.item.FTxt;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.NodIter;
import org.basex.query.xquery.iter.NodeIter;
import org.basex.query.xquery.util.Err;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Document fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class CDoc extends Single {
  /**
   * Constructor.
   * @param e expression
   */
  public CDoc(final Expr e) {
    super(e);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final NodIter nodes = new NodIter();
    final Iter iter = ctx.iter(expr);
    final byte[] base = Token.EMPTY;

    final TokenBuilder text = new TokenBuilder();
    boolean more = false;
    Item it;
    while((it = iter.next()) != null) {
      if(it.node() && it.type != Type.TXT) {
        if(it.type == Type.ATT) Err.or(XPATT);

        if(text.size != 0) {
          nodes.add(new FTxt(text.finish(), null));
          text.reset();
        }
        if(it.type == Type.DOC) {
          final NodeIter ni = ((Nod) it).child();
          if(ni.next() != null) nodes.add(((Nod) it).copy());
        } else {
          nodes.add(((Nod) it).copy());
        }
        more = false;
      } else {
        if(more && text.size != 0 && it.type != Type.TXT) text.add(' ');
        text.add(it.str());
        more = it.type != Type.TXT;
      }
    }
    if(text.size != 0) {
      nodes.add(new FTxt(text.finish(), null));
      text.reset();
    }

    final FDoc doc = new FDoc(nodes, base);
    for(int n = 0; n < nodes.size; n++) nodes.list[n].parent(doc);
    return doc.iter();
  }

  @Override
  public String info() {
    return "document constructor";
  }

  @Override
  public String toString() {
    return Token.string(name()) + "(" + expr + ")";
  }
}
