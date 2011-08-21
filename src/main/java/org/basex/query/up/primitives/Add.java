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
public final class Add extends InsertBase {
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
   * @param trg target database
   * @param i input info
   * @param d document nodes to add
   * @param n document name
   * @param p document(s) path
   * @param c database context
   */
  public Add(final Data trg, final InputInfo i, final ObjList<Item> d,
      final byte[] n, final byte[] p, final Context c) {

    super(PrimitiveType.INSERTAFTER, lastDoc(trg), trg, i, null);
    docs = d;
    final int ndocs = docs.size();
    final byte[] name = ndocs > 1 || n == null || n.length == 0 ? null : n;
    final byte[] path = p == null || p.length == 0 ? null : p;
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
    final Add a = (Add) u;
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

    while(d.hasNext())
      md.insert(md.meta.size, -1, docData(d.next(),  n.next(), p.next()));
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
    final MemData docData;

    if(doc.node()) {
      // adding a document node
      final ANode nd = (ANode) doc;
      if(nd.ndType() != NodeType.DOC) UPDOCTYPE.thrw(input, nd);
      docData = new MemData(data);
      new DataBuilder(docData).build(nd);
    } else if(doc.str()) {
      // adding file(s) from a path
      final String docpath = string(doc.atom(input));
      final String nm = ctx.mprop.random(data.meta.name);
      final DirParser p = new DirParser(IO.get(docpath), ctx.prop);
      final MemBuilder b = new MemBuilder(nm, p, ctx.prop);
      try {
        docData = b.build();
      } catch(final IOException e) {
        throw DOCERR.thrw(input, docpath);
      }
    } else {
      STRNODTYPE.thrw(input, this, doc.type);
      return null;
    }

    // modify name and path, if needed
    final IntList pres = docData.doc();
    if(pres.size() == 1 && name != null) {
      // name is specified and a single document is added: set the name
      final byte[] nm = path == null ? name : concat(path, SLASH, name);
      docData.update(pres.get(0), Data.DOC, nm);
    } else if(path != null) {
      // path is specified: replace the path of each new document
      for(int i = 0, is = pres.size(); i < is; i++) {
        final int d = pres.get(i);
        final byte[] old = docData.text(d, true);
        final int p = lastIndexOf(old, '/');
        final byte[] nm = p < 0 ? old : subtoken(old, p + 1);
        docData.update(d, Data.DOC, concat(path, SLASH, nm));
      }
    }

    return docData;
  }

  /**
   * Returns the last document in the database.
   * @param data database
   * @return pre value of the last document or {@code 0} if database is empty
   */
  private static int lastDoc(final Data data) {
    final IntList docs = data.doc();
    return docs.size() == 0 ? 0 : docs.get(docs.size() - 1);
  }

  @Override
  public String toString() {
    return Util.name(this) + "[" + targetNode() + "]";
  }
}
