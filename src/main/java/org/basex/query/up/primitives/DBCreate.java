package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import java.io.*;

import org.basex.build.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/*
 * [LK]
 * CREATE permissions (checked in DatabaseModifier)
 * pinning in ContextModifier.apply
 * multiple DBCreate on same target allowed? NO
 * overwrites existing DBs YES
 * initial document path? YES
 * multiple initial documents YES
 *
 * CreateDB.run()
 * Strings -> IO.get()
 * Nodes -> IO.get(node.serialize())
 * empty -> Parser.emptyParser(context.prop)
 * document path -> ?
 */

/**
 * Update primitive for the {@link Function#_DB_CREATE} function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class DBCreate extends BasicOperation {
  /** Name for created database. */
  public final String name;
  /** Documents to add. */
  private Item docs;
  /** Path to which initial document(s) will be added. */
  private final byte[] path;
  /** Insertion sequence. */
  private Data md;
  /** Query context. */
  private final QueryContext ctx;

  /**
   * Constructor.
   * @param ii input info
   * @param nm name for created database
   * @param it initial content item
   * @param p path
   * @param c query context
   */
  public DBCreate(final InputInfo ii, final String nm, final Item it, final String p,
      final QueryContext c) {

    super(TYPE.DBCREATE, null, ii);
    ctx = c;
    name = nm;
    docs = it;
    path = Token.token(p);
  }

  @Override
  public DBNode getTargetNode() {
    return null;
  }

  @Override
  public void merge(final BasicOperation o) throws QueryException {
    BXDB_CREATE.thrw(info, ((DBCreate) o).name);
  }

  @Override
  public void prepare(final MemData tmp) throws QueryException {
    if(docs == null) return;
    // build data with all documents, to prevent dirty reads
    md = new MemData(ctx.context.prop);
    md.insert(md.meta.size, -1, new DataClip(docData(docs, path, ctx.context, name)));
    // clear entries to recover memory
    docs = null;
  }

  @Override
  public void apply() throws QueryException {
    // close data instance in query processor
    ctx.resource.removeData(name);
    // check if addressed databases are still pinned
    if(ctx.context.pinned(name)) BXDB_OPENED.thrw(info, name);

    try {
      data = CreateDB.create(name, Parser.emptyParser(ctx.context.prop), ctx.context);
    } catch(final IOException ex) {
      UPDBOPTERR.thrw(info, ex);
    }
    ctx.resource.addData(data);

    // add initial documents
    if(md != null) {
      if(!data.startUpdate()) BXDB_OPENED.thrw(null, data.meta.name);
      data.insert(data.meta.size, -1, new DataClip(md));
      try {
        Optimize.optimize(data, null);
      } catch(final IOException ex) {
        data.finishUpdate();
        UPDBOPTERR.thrw(info, ex);
      }
    }
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public String toString() {
    return Util.name(this) + '[' + docs + ']';
  }
}
