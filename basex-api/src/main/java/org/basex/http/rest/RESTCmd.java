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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class RESTCmd extends Command {
  /** REST session. */
  final RESTSession session;

  /** Return code (may be {@code null}). */
  HTTPCode code;

  /**
   * Constructor.
   * @param session REST session
   */
  RESTCmd(final RESTSession session) {
    // permissions will be tested if single commands are run
    super(Perm.NONE);
    this.session = session;
    jc().type(RESTText.REST);
  }

  @Override
  public void addLocks() {
    final Locks locks = jc().locks;
    final LockList reads = locks.reads, writes = locks.writes;

    for(final Command cmd : session) {
      cmd.addLocks();
      // if command updates the context, it may affect any database that has been opened before.
      // hence, all read locks will be added to list of write locks
      final Locks cmdLocks = cmd.jc().locks;
      if(cmdLocks.writes.contains(Locking.CONTEXT)) writes.add(reads);
      // merge lock lists
      reads.add(cmdLocks.reads);
      writes.add(cmdLocks.writes);
    }
  }

  @Override
  public boolean updating(final Context ctx) {
    for(final Command cmd : session) updating |= cmd.updating(ctx);
    return updating;
  }

  @Override
  protected final boolean run() {
    try {
      run0();
      return true;
    } catch(final IOException ex) {
      return error(ex.getMessage());
    } finally {
      Close.close(context);
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
      // only return info of last command
      final String info = cmd.info();
      error(info);
      if(!ok) {
        if(cmd instanceof Open) code = HTTPCode.NOT_FOUND_X;
        throw HTTPCode.BAD_REQUEST_X.get(info);
      }
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
      final FElem elem = new FElem(header);
      // don't show last attribute (input path)
      final int ll = list.size() - skip;
      for(int l = 1; l < ll; l++) {
        elem.add(new QNm(lc(table.header.get(l))), list.get(l));
      }
      elem.add(list.get(0));
      root.add(elem);
    }
  }

  /**
   * Parses and sets database options.
   * Throws an exception if an option is unknown.
   * @param session REST session
   * @throws IOException I/O exception
   */
  static void parseOptions(final RESTSession session) throws IOException {
    for(final Entry<String, String[]> param : session.conn.requestCtx.queryStrings().entrySet()) {
      parseOption(session, param, true);
    }
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
    final MainOptions options = session.conn.context.options;
    final boolean found = options.option(key) != null;
    if(found || force) options.assign(key, param.getValue()[0]);
    return found;
  }
}
