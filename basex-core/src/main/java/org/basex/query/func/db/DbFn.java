package org.basex.query.func.db;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.func.*;
import org.basex.query.value.node.*;

/**
 * Database functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 * @author Dimitar Popov
 */
public abstract class DbFn extends StandardFunc {
  /**
   * Converts the specified info key to an element name.
   * @param str string to be converted
   * @return resulting name
   */
  public static String toName(final String str) {
    return str.replaceAll("[ -:]", "").toLowerCase(Locale.ENGLISH);
  }

  /**
   * Converts the specified info string to a node fragment.
   * @param root name of the root node
   * @param str string to be converted
   * @return node
   */
  static ANode toNode(final String str, final String root) {
    final FElem top = new FElem(root);
    FElem node = null;
    for(final String l : str.split("\r\n?|\n")) {
      final String[] cols = l.split(": ", 2);
      if(cols[0].isEmpty()) continue;

      final FElem n = new FElem(token(toName(cols[0])));
      if(cols[0].startsWith(" ")) {
        if(node != null) node.add(n);
        if(!cols[1].isEmpty()) n.add(cols[1]);
      } else {
        node = n;
        top.add(n);
      }
    }
    return top;
  }
}
