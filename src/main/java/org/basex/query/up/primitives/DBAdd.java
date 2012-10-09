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
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
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
  private ArrayList<Item> docs = new ArrayList<Item>();
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
    paths.add(token(p));
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
    if(doc instanceof AStr) {
      final byte[] cont = doc.string(info);
      io = IO.get(string(cont));
      if(!io.exists()) WHICHRES.thrw(info, cont);
      if(!name.endsWith("/") && (io.isDir() || io.isArchive())) name += "/";
    }

    String target = "";
    final int s = name.lastIndexOf('/');
    if(s != -1) {
      target = name.substring(0, s);
      name = name.substring(s + 1);
    }

    if(doc instanceof AStr) {
      // set name of document
      if(!name.isEmpty()) io.name(name);
      // get name from io reference
      else if(!(io instanceof IOContent)) name = io.name();
    }

    // ensure that the final name is not empty
    if(name.isEmpty()) RESINV.thrw(info, pth);

    if(doc instanceof AStr) {
      final Parser p = new DirParser(io, ctx.prop, data.meta.path).target(target);
      final MemBuilder b = new MemBuilder(data.meta.name, p);
      try {
        mdata = b.build();
      } catch(final IOException ex) {
        throw IOERR.thrw(info, ex);
      }
    } else if(doc instanceof ANode) {
      // adding a document node
      ANode nd = (ANode) doc;
      if(nd.type != NodeType.DOC) {
        if(nd.type == NodeType.ATT) UPDOCTYPE.thrw(info, nd);
        nd = new FDoc().add(nd);
      }
      mdata = (MemData) nd.dbCopy(data.meta.prop).data;
      mdata.update(0, Data.DOC, pth);
    } else {
      throw STRNODTYPE.thrw(info, this, doc.type);
    }
    return mdata;
  }

  @Override
  public String toString() {
    return Util.name(this) + '[' + docs.get(0) + ']';
  }

  @Override
  public int size() {
    return size;
  }
}
