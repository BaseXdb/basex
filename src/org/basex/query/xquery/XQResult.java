package org.basex.query.xquery;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.data.Serializer;
import org.basex.query.xpath.values.NodeBuilder;
import org.basex.query.xquery.item.DNode;
import org.basex.query.xquery.item.FNode;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.NodeIter;
import org.basex.query.xquery.util.SeqBuilder;
import org.basex.util.Token;

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
      for(int i = 0; i < s; i++) if(seq.item[i].type != sb.item[s].type ||
          !seq.item[i].eq(sb.item[s])) return false;
    } catch(final XQException e) {
      return false;
    }
    return true;
  }

  /** {@inheritDoc} */
  public void serialize(final Serializer ser) throws Exception {
    try {
      ser.open(seq.size);
      for(int i = 0; i < seq.size; i++) {
        if(ser.finished()) break;
        ser.openResult();
        seq.item[i].serialize(ser, ctx, 0);
        ser.closeResult();
      }
      ser.close(seq.size);
    } catch(final XQException ex) {
      BaseX.debug(ex);
      try {
        ser.item(Token.token(" " + ex.getMessage() + "..."));
      } catch(final IOException exx) {
        throw exx;
      }
    }
  }

  /**
   * Converts nodes of a XQuery result into a BaseX nodeset.
   * If that's not possible, <pre>null</pre> is returned.
   * This method is only called by the GUI to allow visualization of
   * XQuery results.
   * @param data data reference
   * @return BaseX node set
   * @throws XQException evaluation exception
   */
  public Nodes nodes(final Data data) throws XQException {
    final NodeBuilder nb = new NodeBuilder();

    for(int i = 0; i < seq.size; i++) {
      final Item it = seq.item[i];
      if(!it.node()) return null;
      if(it instanceof DNode) {
        if(((DNode) it).data != data) return null;
        nb.add(((DNode) it).pre);
      } else {
        final FNode node = (FNode) it;
        final NodeIter ch = node.child();
        Item c;
        while((c = ch.next()) != null) {
          if(c instanceof DNode && ((DNode) c).data == data)
            nb.add(((DNode) c).pre);
        }
      }
    }
    return nb.size != 0 ? new Nodes(nb.finish(), data) : null;
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
    return "XQueryResult[\"" + seq + "\"]";
  }
}
