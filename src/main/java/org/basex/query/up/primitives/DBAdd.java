package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import java.util.Iterator;

import org.basex.build.DirParser;
import org.basex.build.MemBuilder;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.io.IO;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.Item;
import org.basex.query.item.NodeType;
import org.basex.query.util.DataBuilder;
import org.basex.util.InputInfo;
import org.basex.util.Util;
import org.basex.util.list.IntList;
import org.basex.util.list.ObjList;
import org.basex.util.list.TokenList;

/**
 * Add primitive.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Dimitar Popov
 */
public final class DBAdd extends InsertBase {
  /** Documents to add. */
  private final ObjList<Item> docs;
  /** New document names. */
  private final TokenList names;
  /** Paths to which the new document(s) will be added. */
  private final TokenList paths;
  /** Database context. */
  private final Context ctx;

  /**
   * Constructor.
   * @param d target database
   * @param i input info
   * @param doc document nodes to add
   * @param n document name
   * @param p document(s) path
   * @param c database context
   */
  public DBAdd(final Data d, final InputInfo i, final ObjList<Item> doc,
      final String n, final String p, final Context c) {

    super(PrimitiveType.INSERTAFTER, lastDoc(d), d, i, null);
    docs = doc;
    final int ndocs = docs.size();
    final byte[] name = ndocs > 1 || n == null || n.isEmpty() ? null : token(n);
    final byte[] path = p == null || p.isEmpty() ? null : token(p);
    names = new TokenList(ndocs);
    paths = new TokenList(ndocs);
    for(int j = 0; j < ndocs; ++j) {
      names.add(name);
      paths.add(path);
    }
    ctx = c;
  }

  @Override
  public boolean adjacentTexts(final int c) {
    return false;
  }

  @Override
  public void merge(final UpdatePrimitive u) {
    final DBAdd a = (DBAdd) u;
    final Iterator<Item> d = a.docs.iterator();
    final Iterator<byte[]> n = a.names.iterator();
    final Iterator<byte[]> p = a.paths.iterator();
    while(d.hasNext()) {
      docs.add(d.next());
      names.add(n.next());
      paths.add(p.next());
    }
  }

  @Override
  public void apply() {
    super.apply();
    data.insert(pre + data.size(pre, data.kind(pre)), -1, md);
  }

  @Override
  public void prepare() throws QueryException {
    // build data with all documents, to prevent dirty reads
    md = new MemData(data);

    final Iterator<Item> d = docs.iterator();
    final Iterator<byte[]> n = names.iterator();
    final Iterator<byte[]> p = paths.iterator();

    while(d.hasNext()) {
      md.insert(md.meta.size, -1, docData(d.next(),  n.next(), p.next()));
    }
  }

  /**
   * Create a {@link Data} instance for the specified document.
   * @param doc item representing document(s)
   * @param name document name or {@code null} (ignored, if {@code doc}
   * represents multiple documents)
   * @param path document path or {@code null}
   * @return {@link Data} instance from the parsed document(s)
   * @throws QueryException if {@code doc} does not represent valid document(s)
   */
  private Data docData(final Item doc, final byte[] name, final byte[] path)
      throws QueryException {

    final MemData mdata;
    if(doc.node()) {
      // adding a document node
      final ANode nd = (ANode) doc;
      if(nd.ndType() != NodeType.DOC) UPDOCTYPE.thrw(input, nd);
      mdata = new MemData(data);
      new DataBuilder(mdata).build(nd);
    } else if(doc.str()) {
      // adding file(s) from a path
      final String docpath = string(doc.atom(input));
      final DirParser p = new DirParser(IO.get(docpath), ctx.prop);
      final MemBuilder b = new MemBuilder(data.meta.name, p, ctx.prop);
      try {
        mdata = b.build();
      } catch(final IOException ex) {
        throw IOERR.thrw(input, ex);
      }
    } else {
      throw STRNODTYPE.thrw(input, this, doc.type);
    }

    // modify name and path, if needed
    final IntList pres = mdata.docs();
    if(pres.size() == 1 && name != null) {
      // name is specified and a single document is added: set the name
      final byte[] dbpath = path == null ? name : concat(path, SLASH, name);
      mdata.update(pres.get(0), Data.DOC, dbpath);
    } else if(path != null) {
      // path is specified: replace the path of each new document
      for(int i = 0, is = pres.size(); i < is; i++) {
        final int d = pres.get(i);
        final byte[] old = mdata.text(d, true);
        final int p = lastIndexOf(old, '/');
        final byte[] nm = p < 0 ? old : subtoken(old, p + 1);
        mdata.update(d, Data.DOC, concat(path, SLASH, nm));
      }
    }
    return mdata;
  }

  /**
   * Returns the last document in the database.
   * @param data database
   * @return pre value of the last document or {@code 0} if database is empty
   */
  private static int lastDoc(final Data data) {
    final IntList docs = data.docs();
    return docs.size() == 0 ? 0 : docs.get(docs.size() - 1);
  }

  @Override
  public String toString() {
    return Util.name(this) + "[" + targetNode() + "]";
  }
}
