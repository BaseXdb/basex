package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.up.atomic.*;
import org.basex.util.*;

/**
 * Evaluates the 'add' command and adds a document to a collection.
 * Note that the constructors of this class have changed with Version 7.0:
 * the target path and file name have been merged and are now specified
 * as first argument.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Add extends ACreate {
  /** Builder. */
  private Builder builder;

  /** Data to insert. */
  Data tmpData;

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
   * @param path target path, optionally terminated by a new file name
   * If {@code null}, the name of the input will be set as path.
   * @param input input file or XML string
   */
  public Add(final String path, final String input) {
    super(Perm.WRITE, true, path == null ? "" : path, input);
  }

  @Override
  protected boolean run() {
    if(!build()) return false;
    try {
      final Data data = context.data();
      return update(data, new Code() {
        @Override
        boolean run() {
          // skip update if fragment is empty
          if(tmpData.meta.size > 1) {
            context.invalidate();
            final AtomicUpdateCache auc = new AtomicUpdateCache(data);
            auc.addInsert(data.meta.size, -1, new DataClip(tmpData));
            auc.execute(false);
          }
          return info(RES_ADDED_X, jc().performance);
        }
      });
    } finally {
      finish();
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
    final IO source;
    try {
      source = sourceToIO(name);
    } catch(final IOException ex) {
      return error(Util.message(ex));
    }

    // check if resource exists
    if(source == null) return error(RES_NOT_FOUND);
    if(!source.exists()) return in != null ? error(RES_NOT_FOUND) :
        error(RES_NOT_FOUND_X, context.user().has(Perm.CREATE) ? source : args[1]);

    if(!Strings.endsWith(name, '/') && (source.isDir() || source.isArchive())) name += '/';

    String target = "";
    final int s = name.lastIndexOf('/');
    if(s != -1) {
      target = name.substring(0, s);
      name = name.substring(s + 1);
    }

    // get name from io reference
    if(name.isEmpty()) name = source.name();
    else source.name(name);

    // ensure that the final name is not empty
    if(name.isEmpty()) return error(NAME_INVALID_X, name);

    try {
      final Data data = context.data();
      final Parser parser = new DirParser(source, options).target(target);

      // create random database name for disk-based creation
      if(cache(parser)) {
        final String tmpName = soptions.createTempDb(data.meta.name);
        builder = new DiskBuilder(tmpName, parser, soptions, options);
      } else {
        builder = new MemBuilder(name, parser);
      }
      tmpData = builder.binaryDir(data.meta.dir).build();
      return true;
    } catch(final IOException ex) {
      return error(Util.message(ex));
    }
  }

  /**
   * Finalizes an add operation.
   */
  void finish() {
    if(tmpData != null) {
      DropDB.drop(tmpData, soptions);
      tmpData = null;
    }
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
    cb.init().arg(S_TO, 0).add(1);
  }

  @Override
  public String shortInfo() {
    return ADD;
  }

  @Override
  public double progressInfo() {
    return builder != null ? builder.progressInfo() : 0;
  }
}
