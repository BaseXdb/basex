package org.basex.gui.view;

import org.basex.data.Data;
import org.basex.data.PrintSerializer;
import org.basex.gui.GUIProp;
import org.basex.query.fs.FSUtils;
import org.basex.util.IntList;
import org.basex.util.TokenBuilder;

/**
 * This class assembles some database access methods which are used
 * in the same way by different visualizations. If more specific database
 * access is needed, it is advisable to directly work on the {@link Data}
 * class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
    if(data.deepfs && FSUtils.isFile(data, pre)) return true;

    final int last = pre + (GUIProp.mapatts ? 1 : data.attSize(pre, kind));
    return last == data.size || data.parent(pre, kind) >=
      data.parent(last, data.kind(last));
  }

  /**
   * Returns path for the specified pre value.
   * @param data data reference
   * @param pre pre value
   * @return current path
   */
  public static byte[] path(final Data data, final int pre) {
    if(data.deepfs) return FSUtils.getPath(data, pre);

    int p = pre;
    final IntList il = new IntList();
    while(p != 0) {
      il.add(p);
      final int kind = data.kind(p);
      p = data.parent(p, kind);
    }

    final TokenBuilder sb = new TokenBuilder();
    for(int i = il.size - 1; i >= 0; i--) {
      sb.add('/');
      sb.add(content(data, il.get(i), true));
    }
    return sb.finish();
  }

  /**
   * Returns the contents of the specified node.
   * @param p pre value
   * @param s if specified, a short representation is returned
   * @param data data reference
   * (no full text nodes, only attribute names)
   * @return name
   */
  public static byte[] content(final Data data, final int p, final boolean s) {
    final int kind = data.kind(p);

    return kind == Data.ELEM || kind == Data.DOC ? tag(data, p) :
      PrintSerializer.content(data, p, s);
  }

  /**
   * Returns the tag name of the specified node.
   * Note that the pre value must reference an element node.
   * @param data data reference
   * @param pre pre value
   * @return name
   */
  public static byte[] tag(final Data data, final int pre) {
    if(data.deepfs) {
      final byte[] name = FSUtils.getName(data, pre);
      if(name.length != 0) return name;
    }

    if(GUIProp.shownames && data.nameID != 0) {
      final byte[] att = data.attValue(data.nameID, pre);
      if(att != null) return att;
    }
    return data.tag(pre);
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
}
