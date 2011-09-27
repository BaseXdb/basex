package org.basex.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.basex.core.BaseXException;
import org.basex.core.Command;
import org.basex.core.CommandParser;
import org.basex.core.Context;
import org.basex.core.User;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.Exit;
import org.basex.core.cmd.Replace;
import org.basex.core.cmd.Store;
import org.basex.query.QueryException;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * This class offers methods to locally execute database commands.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class LocalSession extends Session {
  /** Database context. */
  private final Context ctx;

  /**
   * Default constructor.
   * @param context context
   */
  public LocalSession(final Context context) {
    this(context, null);
  }

  /**
   * Constructor, specifying login data.
   * @param context context
   * @param user user name
   * @param pass password
   * @throws LoginException login exception
   */
  public LocalSession(final Context context, final String user,
      final String pass) throws LoginException {
    this(context, user, pass, null);
  }

  /**
   * Constructor, specifying login data and an output stream.
   * @param context context
   * @param user user name
   * @param pass password
   * @param output output stream
   * @throws LoginException login exception
   */
  public LocalSession(final Context context, final String user,
      final String pass, final OutputStream output) throws LoginException {

    this(context, output);
    final User usr = ctx.users.get(user);
    if(usr == null || !Token.eq(usr.password, Token.token(Token.md5(pass))))
      throw new LoginException();
  }

  /**
   * Constructor, specifying an output stream.
   * @param context context
   * @param output output stream
   */
  public LocalSession(final Context context, final OutputStream output) {
    super(output);
    ctx = new Context(context, null);
    ctx.user = context.user;
  }

  @Override
  public void create(final String name, final InputStream input)
    throws IOException {
    execute(new CreateDB(name), input);
  }

  @Override
  public void add(final String name, final String target,
      final InputStream input) throws IOException {
    execute(new Add(null, name, target), input);
  }

  @Override
  public void replace(final String path, final InputStream input)
      throws IOException {
    execute(new Replace(path), input);
  }

  @Override
  public void store(final String target, final InputStream input)
      throws IOException {
    execute(new Store(target), input);
  }

  /**
   * Executes a command, passing the specified input.
   * @param cmd command
   * @param input input stream
   * @throws BaseXException database exception
   */
  private void execute(final Command cmd, final InputStream input)
      throws BaseXException {
    cmd.setInput(input);
    cmd.execute(ctx);
    info = cmd.info();
  }

  @Override
  public LocalQuery query(final String query) throws BaseXException {
    return new LocalQuery(query, ctx, out);
  }

  @Override
  public synchronized void close() {
    try {
      execute(new Exit());
    } catch(final IOException ex) {
      Util.debug(ex);
    }
  }

  @Override
  protected void execute(final String str, final OutputStream os)
      throws BaseXException {
    try {
      execute(new CommandParser(str, ctx).parseSingle(), os);
    } catch(final QueryException ex) {
      throw new BaseXException(ex);
    }
  }

  @Override
  protected void execute(final Command cmd, final OutputStream os)
      throws BaseXException {
    cmd.execute(ctx, os);
    info = cmd.info();
  }
}
