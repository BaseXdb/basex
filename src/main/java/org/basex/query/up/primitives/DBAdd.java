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
import org.basex.io.IOContent;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.Item;
import org.basex.query.item.NodeType;
import org.basex.query.util.DataBuilder;
import org.basex.util.InputInfo;
import org.basex.util.Util;
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
  private final ObjList<Item> docs = new ObjList<Item>();
  /** Paths to which the new document(s) will be added. */
  private final TokenList paths = new TokenList();
  /** Database context. */
  private final Context ctx;

  /**
   * Constructor.
   * @param d target database
   * @param i input info
   * @param it document to add
   * @param p document(s) path
   * @param c database context
   */
  public DBAdd(final Data d, final InputInfo i, final Item it,
      final String p, final Context c) {

    super(PrimitiveType.INSERTAFTER, -1, d, i, null);
    docs.add(it);
    paths.add(token(p));
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
    final Iterator<byte[]> p = a.paths.iterator();
    while(d.hasNext()) {
      docs.add(d.next());
      paths.add(p.next());
    }
  }

  @Override
  public void apply() {
    super.apply();
    data.insert(data.meta.size, -1, md);
  }

  @Override
  public void prepare() throws QueryException {
    // build data with all documents, to prevent dirty reads
    md = new MemData(data);
    final Iterator<Item> d = docs.iterator();
    final Iterator<byte[]> p = paths.iterator();
    while(d.hasNext()) {
      md.insert(md.meta.size, -1, docData(d.next(),  p.next()));
    }
  }

  /**
   * Creates a {@link Data} instance for the specified document.
   * @param doc item representing document(s)
   * @param pth target path
   * @return {@link Data} instance from the parsed document(s)
   * @throws QueryException if {@code doc} does not represent valid document(s)
   */
  private Data docData(final Item doc, final byte[] pth)
      throws QueryException {

    final MemData mdata;
    String name = string(pth);
    if(name.endsWith(".")) RESINV.thrw(input, pth);

    // add slash to the target if the addressed file is an archive or directory
    IO io = null;
    if(doc.str()) {
      io = IO.get(string(doc.atom(input)));
      if(!io.exists()) RESFNF.thrw(input, pth);
      if(!name.endsWith("/") && (io.isDir() || io.isArchive())) name += "/";
    }

    String target = "";
    final int s = name.lastIndexOf('/');
    if(s != -1) {
      target = name.substring(0, s);
      name = name.substring(s + 1);
    }

    if(doc.str()) {
      // set name of document
      if(!name.isEmpty()) io.name(name);
      // get name from io reference
      else if(!(io instanceof IOContent)) name = io.name();
    }

    // ensure that the final name is not empty
    if(name.isEmpty()) RESINV.thrw(input, pth);

    if(doc.node()) {
      // adding a document node
      final ANode nd = (ANode) doc;
      if(nd.ndType() != NodeType.DOC) UPDOCTYPE.thrw(input, nd);
      mdata = new MemData(data);
      new DataBuilder(mdata).build(nd);
      mdata.update(0, Data.DOC, pth);
    } else if(doc.str()) {
      final DirParser p = new DirParser(io, target, ctx.prop);
      final MemBuilder b = new MemBuilder(data.meta.name, p, ctx.prop);
      try {
        mdata = b.build();
      } catch(final IOException ex) {
        throw IOERR.thrw(input, ex);
      }
    } else {
      throw STRNODTYPE.thrw(input, this, doc.type);
    }
    return mdata;
  }

  @Override
  public int size() {
    return docs.size();
  }

  @Override
  public String toString() {
    return Util.name(this) + "[" + docs.get(0) + "]";
  }
}
