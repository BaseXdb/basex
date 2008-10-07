package org.basex.query.xpath.func;

import org.basex.data.Data;
import org.basex.index.IndexIterator;
import org.basex.index.ValuesToken;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.values.NodeBuilder;
import org.basex.query.xpath.values.NodeSet;
import org.basex.query.xpath.values.Item;
import org.basex.util.Array;
import org.basex.util.Token;

/**
 * Constructor for id() function.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Id extends Func {
  /**
   * Function Constructor.
   * @param arg expression array
   */
  public Id(final Expr[] arg) {
    super(arg, "id(item)");
  }

  @Override
  public NodeSet eval(final XPContext ctx) throws QueryException {
    final NodeSet local = ctx.item;
    final Data data = local.data;
    // should actually depend on DTD
    final int id = data.attNameID(Token.token("id"));
    if(id == 0) return new NodeSet(ctx);

    final Item arg = evalArgs(ctx)[0];
    byte[][] values = null;

    // create atomized texts
    if(arg instanceof NodeSet) {
      final int[] nodes = ((NodeSet) arg).nodes;
      values = new byte[nodes.length][];
      int c = 0;
      for(final int node : nodes) {
        final byte[][] val = Token.split(data.atom(node), ' ');
        while(c + val.length > values.length) values = Array.extend(values);
        Array.copy(val, values, c);
        c += val.length;
      }
      values = Array.finish(values, c);
    } else {
      values = Token.split(arg.str(), ' ');
    }
    
    // find id references
    final NodeBuilder tmp = new NodeBuilder();
    for(final byte[] v : values) {
      if(data.meta.atvindex) {
        final ValuesToken tok = new ValuesToken(false, v);
        final IndexIterator it = data.ids(tok);
        while(it.more()) {
          final int i = it.next();
          if(data.attNameID(i) == id) tmp.add(data.parent(i, data.kind(i)));
        }
      } else {
        final int size = data.size;
        int pre = 0;
        while(pre != size) {
          final byte[] att = data.attValue(id, pre);
          if(att != null && Token.eq(att, v)) tmp.add(pre);
          pre += data.attSize(pre, data.kind(pre));
        }
      }
    }
    ctx.item = new NodeSet(tmp.finish(), ctx);
    return ctx.item;
  }

  @Override
  public boolean checkArguments() {
    return args.length == 1;
  }
}
