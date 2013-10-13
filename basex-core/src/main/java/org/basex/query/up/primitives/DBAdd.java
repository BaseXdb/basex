package org.basex.query.up.primitives;

import java.util.*;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * Add primitive.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Dimitar Popov
 */
public final class DBAdd extends DBNew {
  /** Size. */
  private int size;

  /**
   * Constructor.
   * @param d target database
   * @param it document to add (IO or ANode instance)
   * @param c database context
   * @param ii input info
   */
  public DBAdd(final Data d, final NewInput it, final QueryContext c, final InputInfo ii) {
    super(TYPE.DBADD, d, c, ii);
    inputs = new ArrayList<NewInput>();
    inputs.add(it);
  }

  @Override
  public void merge(final BasicOperation o) {
    final DBAdd a = (DBAdd) o;
    final Iterator<NewInput> d = a.inputs.iterator();
    while(d.hasNext()) inputs.add(d.next());
  }

  @Override
  public void apply() {
    data.insert(data.meta.size, -1, new DataClip(md));
  }

  @Override
  public void prepare(final MemData tmp) throws QueryException {
    size = inputs.size();
    addDocs(new MemData(tmp), data.meta.name);
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + inputs + ']';
  }
}
