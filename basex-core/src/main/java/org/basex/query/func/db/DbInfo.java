package org.basex.query.func.db;

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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class DbInfo extends DbAccess {
  @Override
  public FNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = toData(qc);
    return toNode(Q_DATABASE, InfoDB.db(data.meta, false, true), qc);
  }

  /**
   * Converts the specified info string to a node fragment.
   * @param name name of the root node
   * @param string string to be converted
   * @param qc query context
   * @return node
   */
  static FNode toNode(final QNm name, final String string, final QueryContext qc) {
    final FBuilder root = FElem.build(name);

    FBuilder header = null;
    for(final String line : string.split(Prop.NL)) {
      final String[] cols = line.split(": ", 2);
      if(cols[0].isEmpty()) continue;

      final String col = cols[0].replaceAll("[ -:]", "").toLowerCase(Locale.ENGLISH);
      final FBuilder node = FElem.build(qc.shared.qname(Token.token(col)));
      if(Strings.startsWith(cols[0], ' ')) {
        header.add(node.add(cols[1]));
      } else {
        if(header != null) root.add(header);
        header = node;
      }
    }
    return root.add(header).finish();
  }
}
