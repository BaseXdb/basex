package org.basex.query.func.db;

import java.util.*;

import org.basex.data.*;
import org.basex.data.Inspection.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbInspect extends DbAccessFn {
  @Override
  public XQMap value(final QueryContext qc) throws QueryException {
    final Inspection inspection = new Inspection(toData(qc), qc);
    final ArrayList<Issue> issues = inspection.issues();

    final RecordType type = Records.INSPECTION_ISSUE.get();
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Issue issue : issues) {
      vb.add(XQMap.get(type, Str.get(issue.check().toString()), Itr.get(issue.count()),
          issue.first() == -1 ? Empty.VALUE : Itr.get(issue.first())));
    }
    return XQMap.get(Records.INSPECTION_RESULT.get(), Itr.get(inspection.nodes),
        Bln.get(issues.isEmpty()), vb.value(this));
  }
}
