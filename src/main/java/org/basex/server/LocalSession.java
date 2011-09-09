package org.basex.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.ListIterator;

import org.basex.core.BaseXException;
import org.basex.core.Command;
import org.basex.core.CommandParser;
import org.basex.core.Context;
import org.basex.core.User;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.Exit;
import org.basex.core.cmd.Replace;
import org.basex.query.QueryException;
import org.basex.util.Token;
import org.basex.util.Util;
import org.xml.sax.InputSource;

/**
 * This class offers methods to locally execute database commands.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class LocalSession extends Session {
  /** Currently registered queries. */
  private final LinkedList<LocalQuery> queries = new LinkedList<LocalQuery>();
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
    throws BaseXException {
    info = CreateDB.create(name, input, ctx);
  }

  @Override
  public void add(final String name, final String target,
      final InputStream input) throws BaseXException {
    info = Add.add(name, target, new InputSource(input), ctx, null, true);
  }

  @Override
  public void replace(final String path, final InputStream input)
      throws BaseXException {
    info = Replace.replace(path, new InputSource(input), ctx, true);
  }

  @Override
  public LocalQuery query(final String query) throws BaseXException {
    final LocalQuery q = out == null ?
        new LocalQuery(this, query, ctx) :
        new LocalQuery(this, query, ctx, out);
    queries.add(q);
    return q;
  }

  @Override
  public void close() {
    try {
      final ListIterator<LocalQuery> i = queries.listIterator();
      while(i.hasNext()) {
        i.next().closeListener();
        i.remove();
      }
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

  /**
   * Remove a query from the list of queries in this session.
   * @param q query to remove
   */
  void removeQuery(final LocalQuery q) {
    queries.remove(q);
  }
}
