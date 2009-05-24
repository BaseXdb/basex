package org.basex.gui.view;

import static org.basex.data.DataText.*;
import org.basex.data.Data;
import org.basex.gui.GUIProp;
import org.basex.util.IntList;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This class assembles some database access methods which are used
 * in the same way by different visualizations. If more specific database
 * access is needed, it is advisable to directly work on the {@link Data}
 * class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class ViewData {
  /** Preventing class instantiation. */
  private ViewData() { }

  /**
   * Checks if the specified node is a leaf node
   * (text node or file element or file tag).
   * @param data data reference
   * @param pre pre value
   * @return result of comparison
   */
  public static boolean isLeaf(final Data data, final int pre) {
    final int kind = data.kind(pre);
    if(kind == Data.ATTR) return true;
    if(data.fs != null && data.fs.isFile(pre)) return true;

    final boolean atts = GUIProp.mapatts && data.fs == null;
    final int last = pre + (atts ? 1 : data.attSize(pre, kind));
    return last == data.meta.size || data.parent(pre, kind) >=
      data.parent(last, data.kind(last));
  }

  /**
   * Returns path for the specified pre value.
   * @param data data reference
   * @param pre pre value
   * @return current path
   */
  public static byte[] path(final Data data, final int pre) {
    if(data == null) return Token.EMPTY;
    if(data.fs != null) return data.fs.path(pre, false);

    int p = pre;
    int k = data.kind(p);
    final IntList il = new IntList();
    while(k != Data.DOC) {
      il.add(p);
      p = data.parent(p, k);
      k = data.kind(p);
    }

    final TokenBuilder sb = new TokenBuilder();
    sb.add("doc(\"");
    sb.add(content(data, p, true));
    sb.add("\")");
    for(int i = il.size - 1; i >= 0; i--) {
      sb.add('/');
      sb.add(content(data, il.list[i], true));
    }
    return sb.finish();
  }

  /**
   * Returns the contents of the specified node.
   * @param data data reference
   * @param p pre value
   * @param s if specified, a short representation is returned
   * (no full text nodes, only attribute names)
   * @return name
   */
  public static byte[] content(final Data data, final int p, final boolean s) {
    switch(data.kind(p)) {
      case Data.ELEM: return data.tag(p);
      case Data.DOC:  return data.text(p);
      case Data.TEXT: return s ? TEXT : data.text(p);
      case Data.COMM: return s ? COMM : data.text(p);
      case Data.PI:   return s ? PI : data.text(p);
    }

    final TokenBuilder tb = new TokenBuilder();
    tb.add(ATT);
    tb.add(data.attName(p));
    if(!s) {
      tb.add(ATT1);
      tb.add(data.attValue(p));
      tb.add(ATT2);
    }
    return tb.finish();
  }

  /**
   * Returns the tag name of the specified node.
   * Note that the pre value must reference an element node.
   * @param data data reference
   * @param pre pre value
   * @return name
   */
  public static byte[] tag(final Data data, final int pre) {
    if(data.fs != null) {
      final byte[] name = data.fs.name(pre);
      if(name.length != 0) return name;
    }

    if(GUIProp.shownames && data.nameID != 0) {
      final byte[] att = data.attValue(data.nameID, pre);
      if(att != null) return att;
    }
    return content(data, pre, true);
  }

  /**
   * Returns the parent for the specified node.
   * @param data data reference
   * @param pre child node
   * @return parent node
   */
  public static int parent(final Data data, final int pre) {
    return data.parent(pre, data.kind(pre));
  }

  /**
   * Returns the size for the specified node.
   * @param data data reference
   * @param pre child node
   * @return parent node
   */
  public static int size(final Data data, final int pre) {
    return data.size(pre, data.kind(pre));
  }
}
