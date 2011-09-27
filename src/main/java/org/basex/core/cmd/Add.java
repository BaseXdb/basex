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
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.util.Performance;
import org.basex.util.Util;

/**
 * Evaluates the 'add' command and adds a document to a collection.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Add extends ACreate {
  /** Builder. */
  private Builder build;

  /**
   * Default constructor.
   * @param input input file or XML string
   */
  public Add(final String input) {
    this(input, null);
  }

  /**
   * Constructor, specifying a document name.
   * @param input input file or XML string
   * @param name name of document
   */
  public Add(final String input, final String name) {
    this(input, name, (String) null);
  }

  /**
   * Constructor, specifying a document name and a target.
   * @param input input XML file or XML string
   * @param name name of document. If {@code null}, the name of the input
   *   will be used
   * @param target target. If {@code null}, target will be set to root
   */
  public Add(final String input, final String name, final String target) {
    super(DATAREF | User.WRITE, name, target == null ? "" : target, input);
  }

  @Override
  protected boolean run() {
    String name = args[0];
    String target = MetaData.normPath(args[1]);
    if(target == null) return error(NAMEINVALID, args[1]);
    if(!target.isEmpty()) target += '/';

    Parser parser;
    if(in == null) {
      final String input = args[2];
      final IO io = IO.get(input);
      if(!io.exists()) return error(FILEWHICH, io);

      if(name != null && !name.isEmpty()) {
        // set specified document name
        io.name(name);
      } else if(io instanceof IOContent) {
        // if no name exists, set database name as document name
        name = context.data().meta.name + IO.XMLSUFFIX;
        io.name(name);
      }
      parser = new DirParser(io, target, prop);
    } else {
      final SAXSource sax = new SAXSource(in);
      parser = new SAXWrapper(sax, name, target, context.prop);
    }

    final String input = name == null ? parser.src.path() : name;
    final String nm = name == null ? parser.src.name() : name;
    // ensure that the name contains no slashes and trailing dots
    if(nm.isEmpty() || nm.endsWith(".") || nm.indexOf('/') != -1)
      return error(NAMEINVALID, nm);

    final String path = target + (target.isEmpty() ? "/" : "") + nm;

    // create disk instances for large documents
    // test does not work for input streams and directories
    final long fl = parser.src.length();
    boolean large = false;
    final Runtime rt = Runtime.getRuntime();
    if(fl > rt.freeMemory() / 3) {
      Performance.gc(2);
      large = fl > rt.freeMemory() / 3;
    }

    // create random database name
    final Data data = context.data();
    final String dbname = large ? context.mprop.random(data.meta.name) : path;
    build = large ? new DiskBuilder(dbname, parser, context) :
      new MemBuilder(dbname, parser, context.prop);

    Data tmp = null;
    try {
      tmp = build.build();
      // ignore empty fragments
      if(tmp.meta.size > 1) {
        data.insert(data.meta.size, -1, tmp);
        context.update();
        data.flush();
      }
      return info(parser.info() + PATHADDED, input, perf);
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

  @Override
  public void build(final CommandBuilder cb) {
    cb.init().arg(AS, 0).arg(TO, 1).arg(2);
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
