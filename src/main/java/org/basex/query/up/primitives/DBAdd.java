package org.basex.query.up.primitives;

import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Add primitive.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Dimitar Popov
 */
public final class DBAdd extends BasicOperation {
  /** Documents to add. */
  private List<Item> docs = new ArrayList<Item>();
  /** Paths to which the new document(s) will be added. */
  private TokenList paths = new TokenList();
  /** Database context. */
  private final Context ctx;
  /** Insertion sequence. */
  private Data md;
  /** Size. */
  private int size;

  /**
   * Constructor.
   * @param d target database
   * @param it document to add
   * @param p document(s) path
   * @param c database context
   * @param ii input info
   */
  public DBAdd(final Data d, final Item it, final String p, final Context c,
      final InputInfo ii) {

    super(TYPE.DBADD, d, ii);
    docs.add(it);
    paths.add(p);
    ctx = c;
  }

  @Override
  public void merge(final BasicOperation o) {
    final DBAdd a = (DBAdd) o;
    final Iterator<Item> d = a.docs.iterator();
    final Iterator<byte[]> p = a.paths.iterator();
    while(d.hasNext()) {
      docs.add(d.next());
      paths.add(p.next());
    }
  }

  @Override
  public void apply() {
    data.insert(data.meta.size, -1, new DataClip(md));
  }

  @Override
  public void prepare(final MemData tmp) throws QueryException {
    // build data with all documents, to prevent dirty reads
    md = new MemData(tmp);
    for(int i = 0; i < docs.size(); i++) {
      md.insert(md.meta.size, -1, new DataClip(docData(
          docs.get(i), paths.get(i), ctx, data.meta.name)));
      // clear entries to recover memory
      docs.set(i, null);
      paths.set(i, null);
      size++;
    }
    docs = null;
    paths = null;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public String toString() {
    return Util.name(this) + '[' + docs.get(0) + ']';
  }
}
