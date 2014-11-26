package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * Evaluates the 'add' command and adds a document to a collection.<br/>
 * Note that the constructors of this class have changed with Version 7.0:
 * the target path and file name have been merged and are now specified
 * as first argument.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Add extends ACreate {
  /** Builder. */
  private Builder build;

  /** Data clip to insert. */
  DataClip clip;
  /** Name of temporary database instance. */
  private String clipDB;

  /**
   * Constructor, specifying a target path.
   * The input needs to be set via {@link #setInput(InputStream)}.
   * @param path target path, optionally terminated by a new file name
   */
  public Add(final String path) {
    this(path, null);
  }

  /**
   * Constructor, specifying a target path and an input.
   * @param path target path, optionally terminated by a new file name.
   * If {@code null}, the name of the input will be set as path.
   * @param input input file or XML string
   */
  public Add(final String path, final String input) {
    super(Perm.WRITE, true, path == null ? "" : path, input);
  }

  @Override
  protected boolean run() {
    try {
      if(!build()) return false;

      // skip update if fragment is empty
      if(clip.data.meta.size > 1) {
        if(!startUpdate()) return false;

        context.invalidate();
        final Data data = context.data();
        final AtomicUpdateCache auc = new AtomicUpdateCache(data);
        auc.addInsert(data.meta.size, -1, clip);
        auc.execute(false);

        finishUpdate();
      }
      return info(RES_ADDED_X, perf);
    } finally {
      close();
    }
  }

  /**
   * Builds a data clip for the document(s) to be added.
   * @return success flag
   */
  boolean build() {
    String name = MetaData.normPath(args[0]);
    if(name == null) return error(PATH_INVALID_X, args[0]);

    // retrieve input
    final IO io;
    try {
      io = sourceToIO(name);
    } catch(final IOException ex) {
      return error(Util.message(ex));
    }

    // check if resource exists
    if(io == null) return error(RES_NOT_FOUND);
    if(!io.exists()) return in != null ? error(RES_NOT_FOUND) :
        error(RES_NOT_FOUND_X, context.user.has(Perm.CREATE) ? io : args[1]);

    if(!name.endsWith("/") && (io.isDir() || io.isArchive())) name += '/';

    String target = "";
    final int s = name.lastIndexOf('/');
    if(s != -1) {
      target = name.substring(0, s);
      name = name.substring(s + 1);
    }

    // get name from io reference
    if(name.isEmpty()) name = io.name();
    else io.name(name);

    // ensure that the final name is not empty
    if(name.isEmpty()) return error(NAME_INVALID_X, name);

    try {
      final Data data = context.data();
      final Parser parser = new DirParser(io, context, data.meta.path);
      parser.target(target);

      // create random database name for disk-based creation
      if(cache(parser)) {
        clipDB = context.globalopts.random(data.meta.name);
        build = new DiskBuilder(clipDB, parser, context);
      } else {
        build = new MemBuilder(name, parser);
      }
      clip = build.dataClip();
      return true;
    } catch(final IOException ex) {
      return error(Util.message(ex));
    }
  }

  /**
   * Finalizes an add operation.
   */
  void close() {
    // close and drop intermediary database instance
    if(clip != null) clip.data.close();
    if(clipDB != null) DropDB.drop(clipDB, context);
  }

  /**
   * Decides if the input should be cached before being written to the final database.
   * @param parser parser reference
   * @return result of check
   */
  private boolean cache(final Parser parser) {
    // main memory mode: never write to disk
    if(options.get(MainOptions.MAINMEM)) return false;
    // explicit caching
    if(options.get(MainOptions.ADDCACHE)) return true;

    // create disk instances for large documents
    // (does not work for input streams and directories)
    long fl = parser.source.length();
    if(parser.source instanceof IOFile) {
      final IOFile f = (IOFile) parser.source;
      if(f.isDir()) {
        for(final String d : f.descendants()) fl += new IOFile(f, d).length();
      }
    }

    // check free memory
    final Runtime rt = Runtime.getRuntime();
    final long max = rt.maxMemory();
    if(fl < (max - rt.freeMemory()) / 2) return false;
    // if caching may be necessary, run garbage collection and try again
    Performance.gc(2);
    return fl > (max - rt.freeMemory()) / 2;
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init().arg(S_TO, 0).arg(1);
  }

  @Override
  protected String tit() {
    return ADD;
  }

  @Override
  protected double prog() {
    return build != null ? build.prog() : 0;
  }
}
