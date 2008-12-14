package org.basex.query.xquery;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.data.Serializer;
import org.basex.query.xpath.item.Bln;
import org.basex.query.xpath.item.Dbl;
import org.basex.query.xpath.item.NodeBuilder;
import org.basex.query.xpath.item.Str;
import org.basex.query.xquery.item.DBNode;
import org.basex.query.xquery.item.FNode;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.NodeIter;
import org.basex.query.xquery.util.SeqBuilder;

/**
 * This is a container for XQuery results.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class XQResult implements Result {
  /** Query context. */
  private final XQContext ctx;
  /** Result item. */
  private final SeqBuilder seq;

  /**
   * Constructor.
   * @param c query context
   * @param sb result sequence
   */
  public XQResult(final XQContext c, final SeqBuilder sb) {
    seq = sb;
    ctx = c;
  }

  /** {@inheritDoc} */
  public int size() {
    return seq.size;
  }

  /** {@inheritDoc} */
  public boolean same(final Result v) {
    if(!(v instanceof XQResult)) return false;

    final SeqBuilder sb = ((XQResult) v).seq;
    final int s = seq.size;
    if(s != sb.size) return false;
    try {
      for(int i = 0; i < s; i++) {
        if(seq.item[i].type != sb.item[i].type ||
            !seq.item[i].eq(sb.item[i])) return false;
      }
    } catch(final XQException e) {
      return false;
    }
    return true;
  }

  /** {@inheritDoc} */
  public void serialize(final Serializer ser) throws IOException {
    ser.open(seq.size);
    for(int i = 0; i < seq.size; i++) {
      if(ser.finished()) break;
      serialize(ser, i);
    }
    ser.close(seq.size);
    
    /*
    } catch(final IOException ex) {
      BaseX.debug(ex);
      try {
        ser.item(Token.token(" " + ex.getMessage() + "..."));
      } catch(final IOException exx) {
        throw exx;
      }
    }*/
  }

  /** {@inheritDoc} */
  public void serialize(final Serializer ser, final int n) throws IOException {
    ser.openResult();
    ctx.serialize(ser, seq.item[n]);
    ser.closeResult();
  }

  /**
   * Converts nodes to an XPath result.
   * If that's not possible, the XQuery value is returned.
   * @param data data reference
   * @return BaseX node set
   */
  public Result xpResult(final Data data) {
    try {
      if(seq.size == 0) return new Nodes(data);
      if(seq.size == 1) {
        final Item it = seq.item[0];
        if(it.type == Type.BLN) return Bln.get(it.bool());
        if(it.n()) return new Dbl(it.dbl());
        if(it.s()) return new Str(it.str());
      }
    
      final NodeBuilder nb = new NodeBuilder();
      for(int i = 0; i < seq.size; i++) {
        final Item it = seq.item[i];
        if(!it.node()) return this;
        if(it instanceof DBNode) {
          if(((DBNode) it).data != data) return this;
          nb.add(((DBNode) it).pre);
        } else {
          final FNode node = (FNode) it;
          final NodeIter ch = node.child();
          Item c;
          while((c = ch.next()) != null) {
            if(c instanceof DBNode && ((DBNode) c).data == data)
              nb.add(((DBNode) c).pre);
          }
        }
      }
      return nb.size != 0 ? new Nodes(nb.finish(), data) : this;
    } catch(final XQException ex) {
      BaseX.debug(ex);
      return this;
    }
  }

  /**
   * Returns an item representation of the result.
   * @return item
   */
  public Item item() {
    return seq.finish();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + seq + "]";
  }
}
