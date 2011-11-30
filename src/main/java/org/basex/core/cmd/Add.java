package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.IOException;

import javax.xml.transform.sax.SAXSource;

import org.basex.build.Builder;
import org.basex.build.DirParser;
import org.basex.build.DiskBuilder;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.build.xml.SAXWrapper;
import org.basex.core.CommandBuilder;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.data.InsertBuilder;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.util.Performance;
import org.basex.util.Util;

/**
 * Evaluates the 'add' command and adds a document to a collection.<br/>
 * Note that the constructors of this class have changed with Version 7.0:
 * the target path and file name have been merged and are now specified
 * as first argument.
 *
 * @author BaseX Team 2005-11, BSD License
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
    if(name == null || name.endsWith(".")) return error(NAMEINVALID, args[0]);

    // add slash to the target if the addressed file is an archive or directory
    IO io = null;
    if(in == null) {
      io = IO.get(args[1]);
      if(!io.exists()) return error(FILEWHICH, create ? io : args[1]);
      if(!name.endsWith("/") && (io.isDir() || io.isArchive())) name += '/';
    }

    String target = "";
    final int s = name.lastIndexOf('/');
    if(s != -1) {
      target = name.substring(0, s);
      name = name.substring(s + 1);
    }

    Parser parser;
    if(io != null) {
      // set name of document
      if(!name.isEmpty()) io.name(name);
      // get name from io reference
      else if(!(io instanceof IOContent)) name = io.name();
      parser = new DirParser(io, target, prop);
    } else {
      parser = new SAXWrapper(new SAXSource(in), name, target, context.prop);
    }

    // ensure that the final name is not empty
    if(name.isEmpty()) return error(NAMEINVALID, name);

    boolean old = false;
    
    //This is the old Add code that does a Parser -> [Mem/Disk]Builder -> temp Data -> target Data
    if(old)
    {
      // create disk instances for large documents
      // (does not work for input streams and directories)
      final long fl = parser.src.length();
      boolean large = false;
      final Runtime rt = Runtime.getRuntime();
      if(fl > rt.freeMemory() / 3) {
        Performance.gc(2);
        large = fl > rt.freeMemory() / 3;
      }
      
      // create random database name for disk-based creation    
      final Data data = context.data();
      final String dbname = large ? context.mprop.random(data.meta.name) : name;
  
      build = large ? new DiskBuilder(dbname, parser, context) :
        new MemBuilder(dbname, parser, context.prop);
  
      Data tmp = null;
      try {
        tmp = build.build();
        // ignore empty fragments
        // [CG] check if fragments can be empty at all
        if(tmp.meta.size > 1) {
          data.insert(data.meta.size, -1, tmp);
          context.update();
          data.flush();
        }
        return info(parser.info() + PATHADDED, name, perf);
      } catch(final IOException ex) {
        Util.debug(ex);
        return error(Util.message(ex));
      } finally {
        // close and drop intermediary database instance
        try { build.close(); } catch(final IOException e) { }
        if(tmp != null) try { tmp.close(); } catch(final IOException e) { }
        if(large) DropDB.drop(dbname, context.mprop);
      }   
    }
    // This is the new Add code that does a Parser -> InsertBuilder -> target Data
    else
    {    
      // This is the new Add code that does a Parser -> InsertBuilder -> target Data
      // create random database name for disk-based creation
      final Data data = context.data();
      build = new InsertBuilder(data.meta.size, -1, data, parser);
      try {
        build.build();
        return info(parser.info() + PATHADDED, name, perf);
      } catch(final IOException ex) {
        Util.debug(ex);
        return error(Util.message(ex));
      } finally {
        context.update();
        data.flush();
        try { build.close(); } catch(final IOException e) { }
      }
    }
     
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init().arg(TO, 0).arg(1);
  }

  @Override
  protected String tit() {
    return BUTTONADD;
  }

  @Override
  protected double prog() {
    return build != null ? build.prog() : 0;
  }
}
