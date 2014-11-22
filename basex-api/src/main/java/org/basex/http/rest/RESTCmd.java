package org.basex.http.rest;

import static org.basex.http.rest.RESTText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.http.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Abstract class for performing REST operations.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class RESTCmd extends Command {
  /** REST session. */
  final RESTSession session;
  /** Command. */
  final ArrayList<Command> cmds;

  /** Return code (may be {@code null}). */
  HTTPCode code;

  /**
   * Constructor.
   * @param rs REST session
   */
  RESTCmd(final RESTSession rs) {
    super(max(rs.cmds));
    cmds = rs.cmds;
    session = rs;
  }

  @Override
  public void databases(final LockResult lr) {
    for(final Command c : cmds) c.databases(lr);

    // lock globally if context-dependent is found (context will be changed by commands)
    final boolean wc = lr.write.contains(DBLocking.CTX) || lr.write.contains(DBLocking.COLL);
    final boolean rc = lr.read.contains(DBLocking.CTX) || lr.read.contains(DBLocking.COLL);
    if(wc || rc && !lr.write.isEmpty()) {
      lr.writeAll = true;
      lr.readAll = true;
    } else if(rc) {
      lr.readAll = true;
    }
  }

  @Override
  public boolean updating(final Context ctx) {
    boolean up = false;
    for(final Command c : cmds) up |= c.updating(ctx);
    return up;
  }

  @Override
  protected final boolean run() {
    try {
      run0();
      return true;
    } catch(final IOException ex) {
      return error(ex.getMessage());
    } finally {
      new Close().run(context);
    }
  }

  /**
   * Runs the command.
   * @throws IOException I/O exception
   */
  protected abstract void run0() throws IOException;

  /**
   * Runs the specified command.
   * @param c command
   * @return string result
   * @throws HTTPException HTTP exception
   */
  final String run(final Command c) throws HTTPException {
    final ArrayOutput ao = new ArrayOutput();
    run(c, ao);
    return ao.toString();
  }

  /**
   * Runs the specified command.
   * @param c command
   * @param os output stream
   * @throws HTTPException HTTP exception
   */
  final void run(final Command c, final OutputStream os) throws HTTPException {
    final boolean ok = c.run(context, os);
    error(c.info());
    if(!ok) throw HTTPCode.BAD_REQUEST_X.get(c.info());
  }

  /**
   * Lists the table contents.
   * @param table table reference
   * @param root root node
   * @param header table header
   * @param skip number of columns to skip
   */
  static void list(final Table table, final FElem root, final QNm header, final int skip) {
    for(final TokenList list : table.contents) {
      final FElem el = new FElem(header);
      // don't show last attribute (input path)
      final int ll = list.size() - skip;
      for(int l = 1; l < ll; l++) {
        el.add(new QNm(lc(table.header.get(l))), list.get(l));
      }
      el.add(list.get(0));
      root.add(el);
    }
  }

  /**
   * Adds a command or opening the addressed database.
   * @param rs REST session
   */
  static void open(final RESTSession rs) {
    final String db = rs.http.db();
    if(!db.isEmpty()) rs.add(new Open(db, rs.http.dbpath()));
  }

  /**
   * Returns a string representation of the used serialization parameters.
   * @param http HTTP context
   * @return serialization parameters
   */
  static SerializerOptions serial(final HTTPContext http) {
    final SerializerOptions sopts = http.sopts();
    if(http.wrapping) {
      sopts.set(SerializerOptions.WRAP_PREFIX, REST_PREFIX);
      sopts.set(SerializerOptions.WRAP_URI, REST_URI);
    }
    return sopts;
  }

  /**
   * Returns the maximum permission from the specified commands.
   * @param cmds commands to be checked
   * @return permission
   */
  private static Perm max(final ArrayList<Command> cmds) {
    Perm p = Perm.NONE;
    for(final Command c : cmds) p = p.max(c.perm);
    return p;
  }

  /**
   * Parses and sets database options.
   * @param rs REST session
   * Throws an exception if an option is unknown.
   * @throws IOException I/O exception
   */
  static void parseOptions(final RESTSession rs) throws IOException {
    for(final Entry<String, String[]> param : rs.http.params.map().entrySet())
      parseOption(rs, param, true);
  }

  /**
   * Parses and sets a single database option.
   * @param rs REST session
   * @param param current parameter
   * @param force force execution
   * @return success flag, indicates if value was found
   * @throws BaseXException database exception
   */
  static boolean parseOption(final RESTSession rs, final Entry<String, String[]> param,
      final boolean force) throws BaseXException {

    final String key = param.getKey().toUpperCase(Locale.ENGLISH);
    final MainOptions options = rs.context.options;
    final boolean found = options.option(key) != null;
    if(found || force) options.assign(key, param.getValue()[0]);
    return found;
  }
}
