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
 * @author BaseX Team 2005-21, BSD License
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

    final IntList pres = new IntList();
    int p = pre, k = data.kind(p);
    while(k != Data.DOC) {
      pres.add(p);
      p = data.parent(p, k);
      k = data.kind(p);
    }

    final TokenBuilder tb = new TokenBuilder();
    tb.add(Function._DB_OPEN.args(data.meta.name, Token.string(data.text(p, true))).trim());
    for(int i = pres.size() - 1; i >= 0; i--) {
      p = pres.get(i);
      k = data.kind(p);
      final byte[] txt;
      switch(k) {
        case Data.TEXT: txt = TEXT; break;
        case Data.COMM: txt = COMMENT; break;
        case Data.PI:   txt = PI; break;
        case Data.ATTR: txt = Token.concat(ATT, data.name(p, k)); break;
        // element node
        default: txt = data.name(p, k); break;
      }
      tb.add('/').add(txt);
    }
    return tb.finish();
  }

  /**
   * Returns textual contents for the specified node.
   * @param data data reference
   * @param pre pre value
   * @return text
   */
  public static byte[] text(final Data data, final int pre) {
    final int kind = data.kind(pre);
    switch(kind) {
      case Data.ELEM:
        return data.name(pre, kind);
      case Data.ATTR:
        return Token.concat(ATT, data.name(pre, kind), ATT1, data.text(pre, false), ATT2);
      default:
        return data.text(pre, true);
    }
  }

  /**
   * Returns the name of the specified node or a standard text representation.
   * @param opts gui options
   * @param data data reference
   * @param pre pre value
   * @return name
   */
  public static byte[] namedText(final GUIOptions opts, final Data data, final int pre) {
    if(opts.get(GUIOptions.SHOWNAME) && data.kind(pre) == Data.ELEM) {
      final int id = nameID(data);
      if(id != 0) {
        final byte[] attr = data.attValue(id, pre);
        if(attr != null) return attr;
      }
    }
    return Token.chop(text(data, pre), 32);
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
