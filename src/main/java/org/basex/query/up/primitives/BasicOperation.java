package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Basic task that operates on the database but is not an update primitive. This task
 * is carried out after all updates on the database have been made effective in the order
 * of the {@link TYPE}. Hence changes made during a snapshot will be reflected by this
 * task.
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public abstract class BasicOperation implements Comparable<BasicOperation>, Operation {
  /** Basic Operation types. Carried out in the given order. */
  public static enum TYPE {
    /** DBAdd.      */ DBADD,
    /** DBStore.    */ DBSTORE,
    /** DBRename.   */ DBRENAME,
    /** DBDelete.   */ DBDELETE,
    /** DBOptimize. */ DBOPTIMIZE,
    /** DBFlush.    */ DBFLUSH,
    /** FnPut.      */ FNPUT,
    /** DBDrop.     */ DBDROP,
    /** DBCreate.   */ DBCREATE,
  };
  /** Target data reference. */
  Data data;
  /** Input info. */
  public final InputInfo info;
  /** Type of this operation. */
  public final TYPE type;

  /**
   * Constructor.
   * @param t type of this operation
   * @param d target data reference
   * @param ii input info
   */
  public BasicOperation(final TYPE t, final Data d, final InputInfo ii) {
    type = t;
    info = ii;
    data = d;
  }

  @Override
  public final int compareTo(final BasicOperation o) {
    return this.type.ordinal() - o.type.ordinal();
  }

  @Override
  public DBNode getTargetNode() {
    return new DBNode(data, -1);
  }

  @Override
  public final InputInfo getInfo() {
    return info;
  }

  @Override
  public final Data getData() {
    return data;
  }

  /**
   * Merges this operation with the given one.
   * @param o operation to merge into this one
   * @throws QueryException exception
   */
  public abstract void merge(final BasicOperation o) throws QueryException;

  /**
   * Applies this operation.
   * @throws QueryException exception
   */
  public abstract void apply() throws QueryException;

  /**
   * Prepares this operation.
   * @throws QueryException exception
   */
  public abstract void prepare() throws QueryException;

  /**
   * Creates a {@link Data} instance for the specified document.
   * @param doc item representing document(s)
   * @param pth target path
   * @param ctx database context
   * @param dbname name of database
   * @return database instance
   * @throws QueryException query exception
   */
  final Data docData(final Item doc, final byte[] pth, final Context ctx,
      final String dbname) throws QueryException {

    if(doc instanceof AStr) return docData((AStr) doc, pth, ctx, dbname);
    if(doc instanceof ANode) return docData((ANode) doc, pth, ctx.prop);
    throw STRNODTYPE.thrw(info, this, doc.type);
  }

  /**
   * Creates a {@link Data} instance from the specified string.
   * @param doc item representing document(s)
   * @param docPath target path
   * @param ctx database context
   * @param dbname name of database
   * @return database instance
   * @throws QueryException query exception
   */
  private Data docData(final AStr doc, final byte[] docPath, final Context ctx,
      final String dbname) throws QueryException {

    final QueryInput qi = new QueryInput(string(doc.string(info)));
    if(!qi.io.exists()) WHICHRES.thrw(info, qi.original);

    // add slash to the target if the addressed file is an archive or directory
    String name = string(docPath);
    if(name.endsWith(".")) RESINV.thrw(info, docPath);
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
    if(name.isEmpty()) RESINV.thrw(info, docPath);

    final IOFile dbpath = ctx.mprop.dbpath(name);
    final Parser p = new DirParser(qi.io, ctx.prop, dbpath).target(target);
    final MemBuilder b = new MemBuilder(dbname, p);
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
   * @param prop database properties
   * @return database instance
   * @throws QueryException query exception
   */
  private Data docData(final ANode node, final byte[] pth, final Prop prop)
      throws QueryException {

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
    final MemData mdata = (MemData) nd.dbCopy(prop).data;
    mdata.update(0, Data.DOC, name);
    return mdata;
  }
}
