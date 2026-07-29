package org.basex.query.func.xslt;

import javax.xml.transform.stream.*;

import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XsltTransformReport extends XsltTransform {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ArrayOutput result = new ArrayOutput();
    final XsltReport report = new XsltReport(qc);
    transform(new StreamResult(result), report, qc);
    report.addMessage();
    report.addResult(result.finish());
    return report.finish();
  }
}
