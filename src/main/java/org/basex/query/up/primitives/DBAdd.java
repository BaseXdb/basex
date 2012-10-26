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
   * @return database instance
   * @throws QueryException query exception
   */
  private Data docData(final Item doc, final byte[] pth) throws QueryException {
    if(doc instanceof AStr) return docData((AStr) doc, pth);
    if(doc instanceof ANode) return docData((ANode) doc, pth);
    throw STRNODTYPE.thrw(info, this, doc.type);
  }

  /**
   * Creates a {@link Data} instance from the specified string.
   * @param doc item representing document(s)
   * @param pth target path
   * @return database instance
   * @throws QueryException query exception
   */
  private Data docData(final AStr doc, final byte[] pth) throws QueryException {
    final QueryInput qi = new QueryInput(string(doc.string(info)));
    if(!qi.io.exists()) WHICHRES.thrw(info, qi.original);

    // add slash to the target if the addressed file is an archive or directory
    String name = string(pth);
    if(name.endsWith(".")) RESINV.thrw(info, pth);
    if(!name.endsWith("/") && (qi.io.isDir() || qi.io.isArchive())) name += "/";
    String target = "";
    final int s = name.lastIndexOf('/');
    if(s != -1) {
      target = name.substring(0, s);
      name = name.substring(s + 1);
    }

    // set name of document
    if(!name.isEmpty()) qi.io.name(name);
    // get name from io reference
    else if(!(qi.io instanceof IOContent)) name = qi.io.name();

    // ensure that the final name is not empty
    if(name.isEmpty()) RESINV.thrw(info, pth);

    final Parser p = new DirParser(qi.io, ctx.prop, data.meta.path).target(target);
    final MemBuilder b = new MemBuilder(data.meta.name, p);
    try {
      return b.build();
    } catch(final IOException ex) {
      throw IOERR.thrw(info, ex);
    }
  }

  /**
   * Creates a {@link Data} instance from the specified node.
   * @param node node to be added
   * @param pth target path
   * @return database instance
   * @throws QueryException query exception
   */
  private Data docData(final ANode node, final byte[] pth) throws QueryException {
    if(endsWith(pth, '.') || endsWith(pth, '/')) RESINV.thrw(info, pth);

    // adding a document node
    ANode nd = node;
    if(nd.type != NodeType.DOC) {
      if(nd.type == NodeType.ATT) UPDOCTYPE.thrw(info, nd);
      nd = new FDoc().add(nd);
    }

    // ensure that the final name is not empty
    byte[] name = pth;
    if(name.length == 0) {
      // adopt name from document node
      name = nd.baseURI();
      final Data d = node.data();
      // adopt path if node is part of database. otherwise, only adopt file name
      int i = d == null || d.inMemory() ? lastIndexOf(name, '/') : indexOf(name, '/');
      if(i != -1) name = substring(name, i + 1);
      if(name.length == 0) RESINV.thrw(info, name);
    }

    // adding a document node
    final MemData mdata = (MemData) nd.dbCopy(data.meta.prop).data;
    mdata.update(0, Data.DOC, name);
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
