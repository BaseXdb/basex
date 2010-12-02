package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.transform.sax.SAXSource;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.build.xml.DirParser;
import org.basex.build.xml.SAXWrapper;
import org.basex.core.BaseXException;
import org.basex.core.CommandBuilder;
import org.basex.core.Context;
import org.basex.core.User;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.util.Performance;
import org.basex.util.Util;
import org.xml.sax.InputSource;

/**
 * Evaluates the 'add' command and adds a document to a collection.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Add extends ACreate {
  /** Builder. */
  private MemBuilder build;
  
  /**
   * Default constructor.
   * @param input input XML file or XML string
   */
  public Add(final String input) {
    this(input, null);
  }

  /**
   * Constructor, specifying a document name.
   * @param input input XML file or XML string
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
      // set specified document name
      io.name(name);
      try {
        // try to resolve name (platform dependent)
        new File(name).getCanonicalFile();
      } catch(final IOException ex) {
        name = null;
      }
      if(name == null || name.matches(".*[\\\\/].*"))
        return error(NAMEINVALID, args[1]);
    } else if(io instanceof IOContent) {
      // if no name exists, set database name as document name
      name = context.data.meta.name + IO.XMLSUFFIX;
      io.name(name);
    }

    final String trg = path(args[2]);
    final DirParser p = new DirParser(io, context.prop, trg);
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
   * @param input XML input stream
   * @param ctx database context
   * @param cmd calling command
   * @return info string
   * @throws BaseXException database exception
   */
  public static String add(final String name, final String target,
      final InputStream input, final Context ctx, final Add cmd)
      throws BaseXException {

    final Data data = ctx.data;
    if(data == null) return PROCNODB;

    final String trg = path(target);
    final BufferedInputStream is = new BufferedInputStream(input);
    final SAXSource sax = new SAXSource(new InputSource(is));
    final Parser parser = new SAXWrapper(sax, name, trg, ctx.prop);
    try {
      ctx.lock.before(true);
      return add(parser, ctx, trg, name, cmd);
    } finally {
      ctx.lock.after(true);
    }
  }

  /**
   * Adds a document to the database.
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

    final String path = target + (target.isEmpty() ? "" : "/") + name;
    final Performance p = new Performance();
    try {
      final MemBuilder build = new MemBuilder(parser, ctx.prop);
      if(cmd != null) cmd.build = build;
      final MemData md = build.build(path);
      ctx.data.insert(ctx.data.meta.size, -1, md);
      ctx.data.flush();
      ctx.update();
      return Util.info(PATHADDED, path, p);
    } catch(final IOException ex) {
      Util.debug(ex);
      throw new BaseXException(ex);
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
