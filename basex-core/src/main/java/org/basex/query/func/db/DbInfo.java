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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbInfo extends DbAccessFn {
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

      if(!Strings.startsWith(cols[0], ' ')) {
        // new section header
        if(header != null) root.node(header);
        header = element(cols[0], qc);
      } else if(header != null) {
        // indented line: key/value pair, or a keyless status message
        if(cols.length > 1) header.node(element(cols[0], qc).text(cols[1]));
        else header.text(cols[0].trim());
      }
    }
    return root.node(header).finish();
  }

  /**
   * Creates an element whose name is derived from the specified label.
   * @param label label
   * @param qc query context
   * @return element builder
   */
  private static FBuilder element(final String label, final QueryContext qc) {
    final String col = label.replaceAll("[ -:]", "").toLowerCase(Locale.ENGLISH);
    return FElem.build(qc.shared.qName(Token.token(col)));
  }
}
