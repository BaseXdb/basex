package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.*;

import javax.xml.transform.sax.*;

import org.basex.build.*;
import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * Evaluates the 'add' command and adds a document to a collection.<br/>
 * Note that the constructors of this class have changed with Version 7.0:
 * the target path and file name have been merged and are now specified
 * as first argument.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Add extends ACreate {
  /** Builder. */
  private Builder build;

  /**
   * Constructor, specifying a target path.
   * Note that the constructors of this class have changed with Version 7.0:
   * the target path and file name have been merged and are now specified
   * as first argument.
   * @param path target path, optionally terminated by a new file name
   */
  public Add(final String path) {
    this(path, null);
  }

  /**
   * Constructor, specifying a target path and an input.
   * Note that the constructors of this class have changed with Version 7.0:
   * the target path and file name have been merged and are now specified
   * as first argument.
   * @param path target path, optionally terminated by a new file name.
   * If {@code null}, the name of the input will be set as path.
   * @param input input file or XML string
   */
  public Add(final String path, final String input) {
    super(DATAREF | User.WRITE, path == null ? "" : path, input);
  }

  @Override
  protected boolean run() {
    final boolean create = context.user.perm(User.CREATE);
    String name = MetaData.normPath(args[0]);
    if(name == null || name.endsWith("."))
      return error(NAME_INVALID_X, args[0]);

    // add slash to the target if the addressed file is an archive or directory
    IO io = null;
    if(in == null) {
      io = IO.get(args[1]);
    } else if(in.getSystemId() != null) {
      io = IO.get(in.getSystemId());
    } else if(in.getByteStream() != null) {
      try {
        io = cache();
      } catch(final IOException ex) {
        return error(Util.message(ex));
      }
    }

    if(io != null) {
      if(!io.exists()) return error(FILE_NOT_FOUND_X, create ? io : args[1]);
      if(!name.endsWith("/") && (io.isDir() || io.isArchive())) name += '/';
    }

    String target = "";
    final int s = name.lastIndexOf('/');
    if(s != -1) {
      target = name.substring(0, s);
      name = name.substring(s + 1);
    }

    final Data data = context.data();
    final Parser parser;

    if(io != null) {
      // set name of document
      if(!name.isEmpty()) io.name(name);
      // get name from io reference
      else if(!(io instanceof IOContent)) name = io.name();
      parser = new DirParser(io, target, prop, data.meta.path);
    } else {
      parser = new SAXWrapper(new SAXSource(in), name, target, context.prop);
    }

    // ensure that the final name is not empty
    if(name.isEmpty()) return error(NAME_INVALID_X, name);

    // create disk instances for large documents
    // (does not work for input streams and directories)
    final long fl = parser.src.length();
    boolean large = false;
    final Runtime rt = Runtime.getRuntime();
    if(fl > rt.freeMemory() / 3) {
      Performance.gc(2);
      large = fl > rt.freeMemory() / 3;
    }
    // in main memory mode, never write to disk
    if(prop.is(Prop.MAINMEM)) large = false;

    // create random database name for disk-based creation
    final String db = large ? context.mprop.random(data.meta.name) : name;
    build = large ? new DiskBuilder(db, parser, context) :
      new MemBuilder(db, parser, context.prop);

    Data tmp = null;
    try {
      tmp = build.build();
      // ignore empty fragments
      if(tmp.meta.size > 1) {
        data.insert(data.meta.size, -1, tmp);
        context.update();
        data.flush();
      }
      return info(parser.info() + PATH_ADDED_X_X, name, perf);

    } catch(final IOException ex) {
      Util.debug(ex);
      return error(Util.message(ex));

    } finally {
      // close and drop intermediary database instance
      try { build.close(); } catch(final IOException e) { }
      if(tmp != null) try { tmp.close(); } catch(final IOException e) { }
      // drop temporary database instance
      if(large) DropDB.drop(db, context);
    }
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init().arg(TO, 0).arg(1);
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
