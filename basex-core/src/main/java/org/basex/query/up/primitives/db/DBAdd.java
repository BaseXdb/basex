package org.basex.query.up.primitives.db;

import java.util.*;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Add primitive.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Dimitar Popov
 */
public final class DBAdd extends DBUpdate {
  /** Database update options. */
  private final DBOptions options;
  /** Container for new database documents. */
  private final DBNew add;
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
    add = new DBNew(qc, docs, info);
  }

  @Override
  public void merge(final Update update) {
    final DBAdd a = (DBAdd) update;
    for(final NewInput input : a.add.inputs) add.inputs.add(input);
  }

  @Override
  public void prepare(final MemData tmp) throws QueryException {
    size = add.inputs.size();
    add.addDocs(tmp, data.meta.name, options);
  }

  @Override
  public void apply() {
    data.insert(data.meta.size, -1, new DataClip(add.data));
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + add.inputs + ']';
  }
}
