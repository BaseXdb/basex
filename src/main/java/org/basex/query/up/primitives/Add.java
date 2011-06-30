package org.basex.query.up.primitives;

import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.query.item.ANode;
import org.basex.query.iter.NodeCache;
import org.basex.query.util.DataBuilder;
import org.basex.util.InputInfo;
import org.basex.util.TokenList;
import org.basex.util.Util;

/**
 * Add primitive.
 * @author BaseX Team 2005-11, BSD License
 * @author Dimitar Popov
 */
public final class Add extends InsertBase {
  /** Documents to add. */
  private final NodeCache docs;
  /** Document paths. */
  private final TokenList paths;
  /** Database context. */
  private final Context ctx;

  /**
   * Constructor.
   * @param d target database
   * @param i input info
   * @param n document nodes to add
   * @param p document paths
   * @param c database context
   */
  public Add(final Data d, final InputInfo i, final NodeCache n,
      final TokenList p, final Context c) {
    super(PrimitiveType.INSERTAFTER, lastDoc(d), d, i, n);
    docs = n;
    paths = p;
    ctx = c;
  }

  @Override
  public boolean checkTextAdjacency(final int c) {
    return false;
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    if(p instanceof Add) {
      final NodeCache newdocs = ((Add) p).docs;
      for(ANode i; (i = newdocs.next()) != null;) docs.add(i);
    } else {
      Util.notexpected(p);
    }
  }

  @Override
  public void apply() {
    super.apply();

    final int k = data.kind(pre);
    data.insert(pre + data.size(pre, k), -1, md);
    if(ctx.data != null) ctx.update();
  }

  @Override
  public void prepare() {
    // build main memory representation of nodes to be copied
    md = new MemData(data);
    new DataBuilder(md).build(docs);
    // rename the document paths as required
    final int[] pres = md.doc();
    for(int i = 0; i < pres.length; ++i)
      md.replace(pres[i], Data.DOC, paths.get(i));
  }

  /**
   * Last document in the database.
   * @param data database
   * @return pre value of the last document or {@code 0} if database is empty
   */
  private static int lastDoc(final Data data) {
    final int[] docs = data.doc();
    return docs.length == 0 ? 0 : docs[docs.length - 1];
  }
}
