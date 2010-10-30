package org.basex.core.cmd;

import static org.basex.core.Text.*;
import java.io.BufferedInputStream;
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
public class Add extends ACreate {
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

    if(args[1] != null) {
      // set specified document name
      io.name(args[1]);
    } else if(io instanceof IOContent) {
      // if no name exists, set database name as document name
      io.name(context.data.meta.name + IO.XMLSUFFIX);
    }

    final String name   = io.name();
    final String target = path(args[2]);
    final DirParser p = new DirParser(io, context.prop, target);
    try {
      return info(add(p, context, target + name));
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
   * @return info string
   * @throws BaseXException database exception
   */
  public static String add(final String name, final String target,
      final InputStream input, final Context ctx) throws BaseXException {

    final Data data = ctx.data;
    if(data == null) return PROCNODB;

    String trg = path(target);
    if(trg.length() != 0) trg += '/';

    final BufferedInputStream is = new BufferedInputStream(input);
    final SAXSource sax = new SAXSource(new InputSource(is));
    final Parser parser = new SAXWrapper(sax, name, trg, ctx.prop);
    try {
      ctx.lock.before(true);
      return add(parser, ctx, trg + name);
    } finally {
      ctx.lock.after(true);
    }
  }
  
  /**
   * Adds a document to the database.
   * @param parser parser instance
   * @param ctx database context
   * @param path database path
   * @return info string
   * @throws BaseXException database exception
   */
  public static String add(final Parser parser, final Context ctx,
      final String path) throws BaseXException {

    final Performance p = new Performance();
    try {
      final MemData md = MemBuilder.build(parser, ctx.prop, path);
      ctx.data.insert(ctx.data.meta.size, -1, md);
      ctx.data.flush();
      ctx.update();
      return Util.info(PATHADDED, path, p);
    } catch(final IOException ex) {
      Util.debug(ex);
      throw new BaseXException(ex);
    }
  }
}
