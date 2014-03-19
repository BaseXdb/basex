package org.basex.query.up.primitives;

import java.util.*;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * Add primitive.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Dimitar Popov
 */
public final class DBAdd extends DBUpdate {
  /** Container for new database documents. */
  private final DBNew add;
  /** Size. */
  private int size;

  /**
   * Constructor.
   * @param data target database
   * @param input document to add (IO or ANode instance)
   * @param qc query context
   * @param info input info
   */
  public DBAdd(final Data data, final NewInput input, final QueryContext qc, final InputInfo info) {
    super(UpdateType.DBADD, data, info);
    final ArrayList<NewInput> docs = new ArrayList<>();
    docs.add(input);
    add = new DBNew(qc, docs, info);
  }

  @Override
  public void merge(final Update up) {
    final DBAdd a = (DBAdd) up;
    for(final NewInput input : a.add.inputs) add.inputs.add(input);
  }

  @Override
  public void apply() {
    data.insert(data.meta.size, -1, new DataClip(add.md));
  }

  @Override
  public void prepare(final MemData tmp) throws QueryException {
    size = add.inputs.size();
    add.addDocs(new MemData(tmp), data.meta.name);
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
