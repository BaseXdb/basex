package org.basex.query.func.proc;

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
public final class ProcExecute extends ProcFn {
  /** QName: result. */
  private static final QNm Q_RESULT = new QNm("result");
  /** QName: output. */
  private static final QNm Q_OUTPUT = new QNm("output");
  /** QName: error. */
  private static final QNm Q_ERROR = new QNm("error");
  /** QName: code. */
  private static final QNm Q_CODE = new QNm("code");

  @Override
  public FNode value(final QueryContext qc) throws QueryException {
    final ProcResult result = exec(qc, false);
    final TokenBuilder error = new TokenBuilder(error(result));
    final boolean ex = result.exception != null;
    if(ex) error.add(Util.message(result.exception));
    final byte[] output = output(result).string(info);

    final FBuilder root = FElem.build(Q_RESULT);
    if(output.length != 0) root.node(FElem.build(Q_OUTPUT).text(output));
    if(!error.isEmpty()) root.node(FElem.build(Q_ERROR).text(error.finish()));
    if(!ex) root.node(FElem.build(Q_CODE).text(result.code));
    return root.finish();
  }
}
