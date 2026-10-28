package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.query.up.atomic.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'put' command and replaces documents in a collection.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Put extends ACreate {
  /** Add command (can be {@code null}). */
  private Add add;

  /**
   * Constructor.
   * The input needs to be set via {@link #setInput(InputStream)}.
   * @param path resource path
   */
  public Put(final String path) {
    this(path, null);
  }

  /**
   * Constructor.
   * @param path resource path
   * @param input input reference (local/remote file path or XML string; can be {@code null})
   */
  public Put(final String path, final String input) {
    super(Perm.WRITE, true, path, input);
  }

  @Override
  protected boolean run() {
    // check if the input source has already been initialized
    final IO io = in == null ? IO.get(args[1]) : null;
    if(io != null) {
      if(denied(io)) return error(PERM_REQUIRED_X, Perm.ADMIN);
      if(!io.exists()) return error(RES_NOT_FOUND_X, io);
      in = io.inputSource();
    }

    final String path = MetaData.normPath(args[0]);
    if(path == null) return error(PATH_INVALID_X, args[0]);

    final Data data = context.data();
    final boolean tree = io != null && (io.isDir() || io.isArchive());
    return update(data, () -> put(data, path, tree));
  }

  /**
   * Puts (adds or replaces) XML documents in the specified database.
   * @param data database
   * @param path target path
   * @param tree replace all documents at and below the path
   * @return success flag
   */
  private boolean put(final Data data, final String path, final boolean tree) {
    final IntList docs = tree ? data.resources.docs(path) : data.resources.docsAt(path);
    final int ds = docs.size(), fs = tree ? 0 : data.resources.files(path);

    if(options.get(MainOptions.REPLACE) || ds + fs == 0) {
      add = new Add(path);
      try {
        add.setInput(in);
        add.init(context, out);
        try {
          if(!pushJob(add).build()) return error(add.info());
        } finally {
          popJob();
        }

        // replace existing document or add new one, delete spare documents
        final AtomicUpdateCache auc = new AtomicUpdateCache(data);
        final DataClip clip = new DataClip(add.tmpData);
        int d = 0;
        if(docs.isEmpty()) {
          auc.addInsert(data.nodes(), -1, clip);
        } else {
          auc.addReplace(docs.get(d++), clip);
        }
        for(; d < ds; d++) auc.addDelete(docs.get(d));
        auc.execute(false);
      } finally {
        add.finish();
      }

      // delete binary and value resources with the same path
      if(!tree) {
        for(final ResourceType type : Resources.BINARIES) data.resources.delete(path, type);
      }
    }
    return info(RES_REPLACED_X_X, ds + fs, jc().performance);
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init().arg(0).add(1);
  }

  @Override
  public String shortInfo() {
    return PUT + DOTS;
  }

  @Override
  public double progressInfo() {
    final Builder builder = add != null ? add.builder : null;
    return builder != null ? builder.progressInfo() : 0;
  }
}
