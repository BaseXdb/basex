package org.basex.http.rest;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.http.*;
import org.basex.io.out.*;
import org.basex.util.options.*;

/**
 * Abstract class for performing REST operations.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
abstract class RESTCmd extends Command {
  /** REST session. */
  final RESTSession session;

  /** Response status (may be {@code null}). */
  HTTPStatus status;

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
        if(cmd instanceof Open) status = HTTPStatus.NOT_FOUND_X;
        throw HTTPStatus.BAD_REQUEST_X.get(info);
      }
    } finally {
      popJob();
    }
  }

  /**
   * Assigns database options.
   * Throws an exception if an option is unknown.
   * @param session REST session
   * @throws IOException I/O exception
   */
  static void assignOptions(final RESTSession session) throws IOException {
    final HTTPConnection conn = session.conn;
    for(final Entry<String, String[]> entry : conn.requestCtx.queryStrings().entrySet()) {
      assign(conn.context.options, entry, true);
    }
  }

  /**
   * Assigns an option.
   * @param options options
   * @param entry current parameter
   * @param enforce force assignment
   * @return success flag
   * @throws HTTPException HTTP exception
   */
  static boolean assign(final Options options, final Entry<String, String[]> entry,
      final boolean enforce) throws HTTPException {

    String key = entry.getKey();
    if(options instanceof MainOptions) key = key.toUpperCase(Locale.ENGLISH);
    if(options.option(key) == null) {
      if(enforce) throw HTTPStatus.UNKNOWN_PARAM_X.get(key);
      return false;
    }

    try {
      options.assign(key, entry.getValue()[0]);
      return true;
    } catch(final BaseXException ex) {
      throw HTTPStatus.BAD_REQUEST_X.get(ex);
    }
  }
}
