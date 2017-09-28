package org.basex.query.up.primitives.db;

import static org.basex.query.QueryError.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Add primitive.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Dimitar Popov
 */
public final class DBAdd extends DBUpdate {
  /** Container for new database documents. */
  private final DBNew newDocs;
  /** Replace flag. */
  private final boolean replace;
  /** Size. */
  private int size;

  /**
   * Constructor.
   * @param data target database
   * @param input document to add (IO or ANode instance)
   * @param opts database options
   * @param replace replace flag
   * @param qc query context
   * @param info input info
   * @throws QueryException query exception
   */
  public DBAdd(final Data data, final NewInput input, final Options opts, final boolean replace,
      final QueryContext qc, final InputInfo info) throws QueryException {

    super(UpdateType.DBADD, data, info);
    this.replace = replace;

    final DBOptions options = new DBOptions(opts, DBOptions.PARSING, info);
    newDocs = new DBNew(qc, options, info, input);
  }

  @Override
  public void merge(final Update update) throws QueryException {
    final DBAdd add = (DBAdd) update;
    if(replace || add.replace) {
      final NewInput input = newDocs.inputs.get(0), addInput = add.newDocs.inputs.get(0);
      String path = input.path, addPath = addInput.path;
      if(input.io != null) path += '/' + input.io.name();
      if(addInput.io != null) addPath += '/' + addInput.io.name();
      if(path.equals(addPath)) throw UPMULTDOC_X_X.get(info, data.meta.name, addPath);
    }
    newDocs.merge(add.newDocs);
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
