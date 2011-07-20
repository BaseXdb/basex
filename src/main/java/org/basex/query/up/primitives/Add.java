package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.util.List;
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

/**
 * Add primitive.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Dimitar Popov
 */
public final class Add extends InsertBase {
  /** Documents to add. */
  private final List<Item> docs;
  /** New document name (used only for adding a single document). */
  private final byte[] name;
  /** Path to which new document(s) will be added. */
  private final byte[] path;
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
  public Add(final Data trg, final InputInfo i, final List<Item> d,
      final byte[] n, final byte[] p, final Context c) {

    super(PrimitiveType.INSERTAFTER, lastDoc(trg), trg, i, null);
    docs = d;
    name = n == null || n.length == 0 ? null : n;
    path = p == null || p.length == 0 ? null : p;
    ctx = c;
  }

  @Override
  public boolean checkTextAdjacency(final int c) {
    return false;
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    if(p instanceof Add) {
      docs.addAll(((Add) p).docs);
    } else {
      Util.notexpected(p);
    }
  }

  @Override
  public void apply() {
    super.apply();
    data.insert(pre + data.size(pre, data.kind(pre)), -1, md);
    if(ctx.data != null) ctx.update();
  }

  @Override
  public void prepare() throws QueryException {
    // build data with all documents, to prevent dirty reads
    md = new MemData(data);
    for(final Item d : docs) {
      final MemData docData;
      if(d.node()) {
        // adding a document node
        final ANode doc = (ANode) d;
        if(doc.ndType() != NodeType.DOC) UPDOCTYPE.thrw(input, doc);
        docData = new MemData(data);
        new DataBuilder(docData).build(doc);
      } else if(d.str()) {
        // adding file(s) from a path
        final String docpath = string(d.atom(input));
        final IO io = IO.get(docpath);
        final DirParser p = new DirParser(io, ctx.prop);
        final MemBuilder b = new MemBuilder(p, ctx.prop);
        try {
          docData = b.build(ctx.mprop.random(data.meta.name));
        } catch(final IOException e) {
          throw DOCERR.thrw(input, docpath);
        }
      } else {
        throw STRNODTYPE.thrw(input, this, d.type);
      }
      md.insert(md.meta.size, -1, docData);
    }

    // set new names, if needed
    final IntList pres = md.doc();
    if(pres.size() == 1 && name != null) {
      // name is specified and a single document is added: set the name
      final byte[] nm = path == null ? name : concat(path, SLASH, name);
      md.update(pres.get(0), Data.DOC, nm);
    } else if(path != null) {
      // path is specified: replace the path of each new document
      for(int i = 0, is = pres.size(); i < is; i++) {
        final int d = pres.get(i);
        final byte[] old = md.text(d, true);
        final int p = lastIndexOf(old, '/');
        final byte[] nm = p < 0 ? old : subtoken(old, p + 1);
        md.update(d, Data.DOC, concat(path, SLASH, nm));
      }
    }
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
