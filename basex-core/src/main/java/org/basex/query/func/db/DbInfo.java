package org.basex.query.func.db;

import java.util.*;

import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class DbInfo extends DbAccess {
  /** Resource element name. */
  private static final String DATABASE = "database";

  @Override
  public FElem item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = toData(qc);
    return toNode(DATABASE, InfoDB.db(data.meta, false, true));
  }

  /**
   * Converts the specified info string to a node fragment.
   * @param name name of the root node
   * @param string string to be converted
   * @return node
   */
  static FElem toNode(final String name, final String string) {
    final FElem root = new FElem(name);
    FElem node = null;
    for(final String line : string.split(Prop.NL)) {
      final String[] cols = line.split(": ", 2);
      if(cols[0].isEmpty()) continue;

      final FElem child = new FElem(cols[0].replaceAll("[ -:]", "").toLowerCase(Locale.ENGLISH));
      if(Strings.startsWith(cols[0], ' ')) {
        if(node != null) node.add(child);
        if(!cols[1].isEmpty()) child.add(cols[1]);
      } else {
        node = child;
        root.add(child);
      }
    }
    return root;
  }
}
