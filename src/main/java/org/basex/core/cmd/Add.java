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
import org.basex.core.BaseXException;
import org.basex.core.CommandBuilder;
import org.basex.core.Context;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.util.Performance;
import org.basex.util.Util;
import org.xml.sax.InputSource;

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
    this(input, name, null);
  }

  /**
   * Constructor, specifying a document name and a target.
   * @param input input XML file or XML string
   * @param name name of document. If {@code null}, the name of the input
   *   will be used
   * @param target target. If {@code null}, target will be set to root
   */
  public Add(final String input, final String name, final String target) {
    super(DATAREF | User.WRITE, input, name, target == null ? "" : target);
  }

  @Override
  protected boolean run() {
    final String input = args[0];
    final IO io = IO.get(input);
    if(!io.exists()) return error(FILEWHICH, io);

    String name = args[1];
    if(name != null && !name.isEmpty()) {
      // assure that name contains no slashes
      if(name.matches(".*[\\\\/].*")) return error(NAMEINVALID, args[1]);
      // set specified document name
      io.name(name);
    } else if(io instanceof IOContent) {
      // if no name exists, set database name as document name
      name = context.data.meta.name + IO.XMLSUFFIX;
      io.name(name);
    }

    final String trg = path(args[2]);
    final DirParser p = new DirParser(io, trg, prop);
    try {
      return info(add(p, context, trg, name, this));
    } catch(final BaseXException ex) {
      return error(ex.getMessage());
    }
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init().arg(AS, 1).arg(TO, 2).arg(0);
  }

  /**
   * Adds a document to the database.
   * @param name name of document. If {@code null}, the name of the input
   *   will be used
   * @param target target. If {@code null}, target will be set to root
   * @param input XML input source
   * @param ctx database context
   * @param cmd calling command
   * @param lock if {@code true}, register a write lock in context
   * @return info string
   * @throws BaseXException database exception
   */
  public static String add(final String name, final String target,
      final InputSource input, final Context ctx, final Add cmd,
      final boolean lock) throws BaseXException {

    final Data data = ctx.data;
    if(data == null) return PROCNODB;

    String trg = path(target);
    if(!trg.isEmpty()) trg += '/';

    final SAXSource sax = new SAXSource(input);
    final Parser parser = new SAXWrapper(sax, name, trg, ctx.prop);
    try {
      if(lock) ctx.register(true);
      return add(parser, ctx, trg, name, cmd);
    } finally {
      if(lock) ctx.unregister(true);
    }
  }

  /**
   * Adds the specified input to the database.
   * @param parser parser instance
   * @param ctx database context
   * @param target target
   * @param name name
   * @param cmd calling command
   * @return info string
   * @throws BaseXException database exception
   */
  private static String add(final Parser parser, final Context ctx,
      final String target, final String name, final Add cmd)
      throws BaseXException {

    final Performance p = new Performance();
    final String input = name == null ? parser.src.path() : name;
    final String path = target + (target.isEmpty() ? "/" : "") +
        (name == null ? parser.src.name() : name);

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
    final Data data = ctx.data;
    final String dbname = large ? ctx.mprop.random(data.meta.name) : path;
    final Builder build = large ? new DiskBuilder(dbname, parser, ctx) :
      new MemBuilder(dbname, parser, ctx.prop);
    if(cmd != null) cmd.build = build;

    Data tmp = null;
    try {
      tmp = build.build();
      // ignore empty fragments
      if(tmp.meta.size > 1) {
        data.insert(data.meta.size, -1, tmp);
        ctx.update();
        data.flush();
      }
      return Util.info(parser.info() + PATHADDED, input, p);
    } catch(final IOException ex) {
      Util.debug(ex);
      throw new BaseXException(ex);
    } finally {
      // close and drop intermediary database instance
      try { build.close(); } catch(final IOException e) { }
      if(tmp != null) try { tmp.close(); } catch(final IOException e) { }
      if(large) DropDB.drop(dbname, ctx.mprop);
    }
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
