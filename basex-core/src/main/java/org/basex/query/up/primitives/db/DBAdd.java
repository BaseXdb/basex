package org.basex.query.up.primitives.db;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Add primitive.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Dimitar Popov
 */
public final class DBAdd extends DBUpdate {
  /** Database update options. */
  private final DBOptions options;
  /** Container for new database documents. */
  private final DBNew newDocs;
  /** Size. */
  private int size;

  /**
   * Constructor.
   * @param data target database
   * @param input document to add (IO or ANode instance)
   * @param opts database options
   * @param qc query context
   * @param info input info
   * @throws QueryException query exception
   */
  public DBAdd(final Data data, final NewInput input, final Options opts, final QueryContext qc,
      final InputInfo info) throws QueryException {

    super(UpdateType.DBADD, data, info);
    options = new DBOptions(opts, DBOptions.PARSING, info);

    final ArrayList<NewInput> docs = new ArrayList<>();
    docs.add(input);
    newDocs = new DBNew(qc, docs, options, info);
  }

  @Override
  public void merge(final Update update) {
    newDocs.merge(((DBAdd) update).newDocs);
  }

  @Override
  public void prepare() throws QueryException {
    size = newDocs.inputs.size();
    newDocs.prepare(data.meta.name);
  }

  @Override
  public void apply() {
    try {
      data.insert(data.meta.size, -1, new DataClip(newDocs.data));
    } finally {
      newDocs.finish();
    }
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + newDocs.inputs + ']';
  }
}
