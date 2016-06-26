package org.basex.http.rest;

import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.http.*;
import org.basex.io.out.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Abstract class for performing REST operations.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
abstract class RESTCmd extends Command {
  /** REST session. */
  final RESTSession session;
  /** Commands. */
  final ArrayList<Command> cmds;

  /** Return code (may be {@code null}). */
  HTTPCode code;

  /**
   * Constructor.
   * @param session REST session
   */
  RESTCmd(final RESTSession session) {
    super(max(session.cmds));
    this.session = session;
    cmds = session.cmds;
  }

  @Override
  public void databases(final LockResult lr) {
    for(final Command cmd : cmds) {
      // collect local locks and merge it with global lock list
      final LockResult tmp = new LockResult();
      cmd.databases(tmp);
      lr.union(tmp);
    }
  }

  @Override
  public boolean updating(final Context ctx) {
    boolean up = false;
    for(final Command cmd : cmds) up |= cmd.updating(ctx);
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
   * @param cmd command
   * @return string result
   * @throws HTTPException HTTP exception
   */
  final String run(final Command cmd) throws HTTPException {
    final ArrayOutput ao = new ArrayOutput();
    run(cmd, ao);
    return ao.toString();
  }

  /**
   * Runs the specified command.
   * @param cmd command
   * @param os output stream
   * @throws HTTPException HTTP exception
   */
  final void run(final Command cmd, final OutputStream os) throws HTTPException {
    try {
      final boolean ok = pushJob(cmd).run(context, os);
      error(cmd.info());
      if(!ok) throw HTTPCode.BAD_REQUEST_X.get(cmd.info());
    } finally {
      popJob();
    }
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
   * @param session REST session
   */
  static void open(final RESTSession session) {
    final String db = session.http.db();
    if(!db.isEmpty()) session.add(new Open(db, session.http.dbpath()));
  }

  /**
   * Returns the maximum permission from the specified commands.
   * @param cmds commands to be checked
   * @return permission
   */
  private static Perm max(final ArrayList<Command> cmds) {
    Perm p = Perm.NONE;
    for(final Command cmd : cmds) p = p.max(cmd.perm);
    return p;
  }

  /**
   * Parses and sets database options.
   * Throws an exception if an option is unknown.
   * @param session REST session
   * @throws IOException I/O exception
   */
  static void parseOptions(final RESTSession session) throws IOException {
    for(final Entry<String, String[]> param : session.http.params.map().entrySet())
      parseOption(session, param, true);
  }

  /**
   * Parses and sets a single database option.
   * @param session REST session
   * @param param current parameter
   * @param force force execution
   * @return success flag, indicates if value was found
   * @throws BaseXException database exception
   */
  static boolean parseOption(final RESTSession session, final Entry<String, String[]> param,
      final boolean force) throws BaseXException {

    final String key = param.getKey().toUpperCase(Locale.ENGLISH);
    final MainOptions options = session.context.options;
    final boolean found = options.option(key) != null;
    if(found || force) options.assign(key, param.getValue()[0]);
    return found;
  }
}
