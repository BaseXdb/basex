package org.basex.query.func.db;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DbInfo extends DbAccess {
  /** Resource element name. */
  private static final String DATABASE = "database";

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = checkData(qc);
    return toNode(InfoDB.db(data.meta, false, true), DATABASE);
  }

  /**
   * Converts the specified info string to a node fragment.
   * @param root name of the root node
   * @param string string to be converted
   * @return node
   */
  static ANode toNode(final String string, final String root) {
    final FElem top = new FElem(root);
    FElem node = null;
    for(final String l : string.split("\r\n?|\n")) {
      final String[] cols = l.split(": ", 2);
      if(cols[0].isEmpty()) continue;

      final FElem n = new FElem(token(toName(cols[0])));
      if(Strings.startsWith(cols[0], ' ')) {
        if(node != null) node.add(n);
        if(!cols[1].isEmpty()) n.add(cols[1]);
      } else {
        node = n;
        top.add(n);
      }
    }
    return top;
  }

  /**
   * Converts the specified info key to an element name.
   * @param string string to be converted
   * @return resulting name
   */
  public static String toName(final String string) {
    return string.replaceAll("[ -:]", "").toLowerCase(Locale.ENGLISH);
  }
}
