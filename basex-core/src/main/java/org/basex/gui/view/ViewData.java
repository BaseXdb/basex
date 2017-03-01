package org.basex.gui.view;

import static org.basex.data.DataText.*;

import org.basex.data.*;
import org.basex.gui.*;
import org.basex.query.func.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class assembles some database access methods which are used
 * in the same way by different visualizations. If more specific database
 * access is needed, it is advisable to directly work on the {@link Data}
 * class.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class ViewData {
  /** Preventing class instantiation. */
  private ViewData() { }

  /**
   * Checks if the specified node is a text node.
   * @param opts gui options
   * @param data data reference
   * @param pre pre value
   * @return result of check
   */
  public static boolean leaf(final GUIOptions opts, final Data data, final int pre) {
    final int kind = data.kind(pre);
    if(kind == Data.ATTR) return true;

    final boolean atts = opts.get(GUIOptions.MAPATTS);
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
    if(data == null || pre >= data.meta.size) return Token.EMPTY;

    int p = pre;
    int k = data.kind(p);
    final IntList il = new IntList();
    while(k != Data.DOC) {
      il.add(p);
      p = data.parent(p, k);
      k = data.kind(p);
    }

    final byte[] doc = text(data, p, true);
    final TokenBuilder tb = new TokenBuilder();
    tb.add(Function._DB_OPEN.args(data.meta.name, Token.string(doc)));
    for(int i = il.size() - 1; i >= 0; i--) {
      tb.add('/').add(text(data, il.get(i), true));
    }
    return tb.finish();
  }

  /**
   * Returns textual contents for the specified node.
   * @param data data reference
   * @param pre pre value
   * @param compact if specified, a compact representation is returned
   * @return text
   */
  public static byte[] text(final Data data, final int pre, final boolean compact) {
    final int kind = data.kind(pre);
    switch(kind) {
      case Data.ELEM: return data.name(pre, kind);
      case Data.DOC:  return data.text(pre, true);
      case Data.TEXT: return compact ? TEXT : data.text(pre, true);
      case Data.COMM: return compact ? COMMENT : data.text(pre, true);
      case Data.PI:   return compact ? PI : data.text(pre, true);
    }

    final TokenBuilder tb = new TokenBuilder();
    tb.add(ATT);
    tb.add(data.name(pre, kind));
    if(!compact) {
      tb.add(ATT1);
      tb.add(data.text(pre, false));
      tb.add(ATT2);
    }
    return tb.finish();
  }

  /**
   * Returns the name of the specified element.
   * Note that the pre value must reference an element node.
   * @param opts gui options
   * @param data data reference
   * @param pre pre value
   * @return name
   */
  public static byte[] name(final GUIOptions opts, final Data data, final int pre) {
    if(data.kind(pre) == Data.ELEM) {
      final int id = nameID(data);
      if(id != 0 && opts.get(GUIOptions.SHOWNAME)) {
        final byte[] att = data.attValue(id, pre);
        if(att != null) return att;
      }
    }
    return text(data, pre, true);
  }

  /**
   * Returns the name id of the specified node.
   * @param data data reference
   * @return name id
   */
  public static int nameID(final Data data) {
    return data.attrNames.id(T_NAME);
  }

  /**
   * Returns the size id of the specified node.
   * @param data data reference
   * @return name id
   */
  public static int sizeID(final Data data) {
    return data.attrNames.id(T_SIZE);
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
