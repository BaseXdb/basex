package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.query.util.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Add primitive.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Dimitar Popov
 */
public final class DBAdd extends InsertBase {
  /** Documents to add. */
  private ArrayList<Item> docs = new ArrayList<Item>();
  /** Paths to which the new document(s) will be added. */
  private TokenList paths = new TokenList();
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
  public DBAdd(final Data d, final InputInfo i, final Item it, final String p,
      final Context c) {

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
    for(int i = 0; i < docs.size(); i++) {
      md.insert(md.meta.size, -1, docData(docs.get(i),  paths.get(i)));
      // clear entries to recover memory
      docs.set(i, null);
      paths.set(i, null);
      size++;
    }
    docs = null;
    paths = null;
  }

  /**
   * Creates a {@link Data} instance for the specified document.
   * @param doc item representing document(s)
   * @param pth target path
   * @return {@link Data} instance from the parsed document(s)
   * @throws QueryException if {@code doc} does not represent valid document(s)
   */
  private Data docData(final Item doc, final byte[] pth) throws QueryException {
    final MemData mdata;
    String name = string(pth);
    if(name.endsWith(".")) RESINV.thrw(info, pth);

    // add slash to the target if the addressed file is an archive or directory
    IO io = null;
    final Type dt = doc.type;
    if(dt.isString()) {
      io = IO.get(string(doc.string(info)));
      if(!io.exists()) WHICHRES.thrw(info, pth);
      if(!name.endsWith("/") && (io.isDir() || io.isArchive())) name += "/";
    }

    String target = "";
    final int s = name.lastIndexOf('/');
    if(s != -1) {
      target = name.substring(0, s);
      name = name.substring(s + 1);
    }

    if(dt.isString()) {
      // set name of document
      if(!name.isEmpty()) io.name(name);
      // get name from io reference
      else if(!(io instanceof IOContent)) name = io.name();
    }

    // ensure that the final name is not empty
    if(name.isEmpty()) RESINV.thrw(info, pth);

    if(dt.isNode()) {
      // adding a document node
      final ANode nd = (ANode) doc;
      if(nd.type != NodeType.DOC) UPDOCTYPE.thrw(info, nd);
      mdata = new MemData(data);
      new DataBuilder(mdata).build(nd);
      mdata.update(0, Data.DOC, pth);
    } else if(dt.isString()) {
      final Parser p = new DirParser(io, ctx.prop, data.meta.path).target(target);
      final MemBuilder b = new MemBuilder(data.meta.name, p);
      try {
        mdata = b.build();
      } catch(final IOException ex) {
        throw IOERR.thrw(info, ex);
      }
    } else {
      throw STRNODTYPE.thrw(info, this, doc.type);
    }
    return mdata;
  }

  @Override
  public String toString() {
    return Util.name(this) + '[' + docs.get(0) + ']';
  }
}
