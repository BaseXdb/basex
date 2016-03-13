package org.basex.query.up.primitives.node;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.up.atomic.*;
import org.basex.query.up.primitives.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Replace document primitive.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class ReplaceDoc extends NodeUpdate {
  /** Container for new database documents. */
  private final DBNew newDocs;
  /** Database update options. */
  private final DBOptions options;

  /**
   * Constructor.
   * @param pre target node pre value
   * @param data target data instance
   * @param input new document
   * @param opts options
   * @param qc query context
   * @param info input info
   * @throws QueryException query exception
   */
  public ReplaceDoc(final int pre, final Data data, final NewInput input, final Options opts,
      final QueryContext qc, final InputInfo info) throws QueryException {

    super(UpdateType.REPLACENODE, pre, data, info);
    options = new DBOptions(opts, DBOptions.PARSING, info);
    newDocs = new DBNew(qc, Arrays.asList(input), options, info);
  }

  @Override
  public void prepare(final MemData tmp) throws QueryException {
    newDocs.prepare(data.meta.name);
  }

  @Override
  public void merge(final Update update) throws QueryException {
    throw UPMULTDOC_X_X.get(info, data.meta.name, newDocs.inputs.get(0).path);
  }

  @Override
  public void update(final NamePool pool) {
    throw Util.notExpected();
  }

  @Override
  public void addAtomics(final AtomicUpdateCache auc) {
    auc.addReplace(pre, new DataClip(newDocs.data));
  }

  @Override
  public int size() {
    return 1;
  }
}
