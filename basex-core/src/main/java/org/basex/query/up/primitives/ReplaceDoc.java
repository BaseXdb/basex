package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import java.util.*;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Replace document primitive.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public class ReplaceDoc extends NodeUpdate {
  /** Container for new database documents. */
  private final DBNew replace;
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
    replace = new DBNew(qc, Arrays.asList(input), info);
  }

  @Override
  public void prepare(final MemData tmp) throws QueryException {
    replace.addDocs(tmp, data.meta.name, options);
  }

  @Override
  public void merge(final Update update) throws QueryException {
    throw UPMULTDOC_X.get(info, replace.inputs.get(0).path);
  }

  @Override
  public void update(final NamePool pool) {
    throw Util.notExpected();
  }

  @Override
  public void addAtomics(final AtomicUpdateCache auc) {
    auc.addReplace(pre, new DataClip(replace.data));
  }

  @Override
  public int size() {
    return 1;
  }
}
