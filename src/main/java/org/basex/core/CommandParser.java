package org.basex.core;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdAlter;
import org.basex.core.Commands.CmdCreate;
import org.basex.core.Commands.CmdDrop;
import org.basex.core.Commands.CmdIndex;
import org.basex.core.Commands.CmdIndexInfo;
import org.basex.core.Commands.CmdInfo;
import org.basex.core.Commands.CmdPerm;
import org.basex.core.Commands.CmdShow;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.AlterDB;
import org.basex.core.cmd.AlterUser;
import org.basex.core.cmd.Backup;
import org.basex.core.cmd.Check;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.CreateFS;
import org.basex.core.cmd.CreateIndex;
import org.basex.core.cmd.CreateMAB;
import org.basex.core.cmd.CreateUser;
import org.basex.core.cmd.Cs;
import org.basex.core.cmd.Delete;
import org.basex.core.cmd.DropBackup;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.DropIndex;
import org.basex.core.cmd.DropUser;
import org.basex.core.cmd.Exit;
import org.basex.core.cmd.Export;
import org.basex.core.cmd.Find;
import org.basex.core.cmd.Get;
import org.basex.core.cmd.Grant;
import org.basex.core.cmd.Help;
import org.basex.core.cmd.Info;
import org.basex.core.cmd.InfoDB;
import org.basex.core.cmd.InfoIndex;
import org.basex.core.cmd.InfoStorage;
import org.basex.core.cmd.Kill;
import org.basex.core.cmd.List;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.Optimize;
import org.basex.core.cmd.Password;
import org.basex.core.cmd.Restore;
import org.basex.core.cmd.Run;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.ShowBackups;
import org.basex.core.cmd.ShowDatabases;
import org.basex.core.cmd.ShowSessions;
import org.basex.core.cmd.ShowUsers;
import org.basex.core.cmd.XQuery;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryParser;
import org.basex.util.Array;
import org.basex.util.InputParser;
import org.basex.util.Levenshtein;
import org.basex.util.StringList;
import org.basex.util.TokenBuilder;

/**
 * This is a parser for command strings, creating {@link Command} instances.
 * Several commands can be formulated in one string and separated by semicolons.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class CommandParser extends InputParser {
  /** Context. */
  private final Context ctx;
  /** Flag for gui command. */
  private boolean gui;

  /**
   * Constructor, parsing the input queries.
   * @param in query input
   * @param c context
   */
  public CommandParser(final String in, final Context c) {
    super(in);
    ctx = c;
  }

  /**
   * Parses the input as single command and returns the result.
   * @return command
   * @throws QueryException query exception
   */
  public Command parseSingle() throws QueryException {
    final Cmd cmd = consume(Cmd.class, null);
    final Command command = parse(cmd, true);
    consumeWS();
    if(more()) help(null, cmd);
    return command;
  }

  /**
   * Parses the input and returns a command list.
   * @return commands
   * @throws QueryException query exception
   */
  public Command[] parse() throws QueryException {
    Command[] list = new Command[0];
    while(true) {
      final Cmd cmd = consume(Cmd.class, null);
      list = Array.add(list, parse(cmd, false));
      consumeWS();
      if(!more()) return list;
      if(!consume(';')) help(null, cmd);
    }
  }

  /**
   * Parses the input and returns a command list.
   * @param g gui flag
   * @return commands
   * @throws QueryException query exception
   */
  public Command[] parse(final boolean g) throws QueryException {
    gui = g;
    return parse();
  }

  /**
   * Parses a single command.
   * @param cmd command definition
   * @param s single command expected
   * @return resulting command
   * @throws QueryException query exception
   */
  private Command parse(final Cmd cmd, final boolean s)
      throws QueryException {
    switch(cmd) {
      case CREATE:
        switch(consume(CmdCreate.class, cmd)) {
          case DATABASE: case DB:
            return new CreateDB(name(cmd), s ? remaining(null) : string(null));
          case INDEX:
            return new CreateIndex(consume(CmdIndex.class, cmd));
          case FS:
            return new CreateFS(name(cmd), string(cmd));
          case MAB:
            return new CreateMAB(string(cmd), name(null));
          case USER:
            return new CreateUser(name(cmd), string(null));
        }
        break;
      case ALTER:
        switch(consume(CmdAlter.class, cmd)) {
          case DATABASE: case DB:
            return new AlterDB(name(cmd), name(cmd));
          case USER:
            return new AlterUser(name(cmd), string(null));
        }
        break;
      case OPEN:
        return new Open(string(cmd));
      case CHECK:
        return new Check(string(cmd));
      case ADD:
        String arg1 = key(AS, null) ? string(cmd) : null;
        String arg2 = key(TO, null) ? string(cmd) : null;
        return new Add(s ? remaining(cmd) : string(cmd), arg1, arg2);
      case DELETE:
        return new Delete(string(cmd));
      case INFO:
        switch(consume(CmdInfo.class, cmd)) {
          case NULL:
            return new Info();
          case DATABASE: case DB:
            return new InfoDB();
          case INDEX:
            return new InfoIndex(consume(CmdIndexInfo.class, null));
          case STORAGE:
            arg1 = number(null);
            arg2 = arg1 != null ? number(null) : null;
            if(arg1 == null) arg1 = xquery(null);
            return new InfoStorage(arg1, arg2);
        }
        break;
      case CLOSE:
        return new Close();
      case LIST:
        return new List();
      case DROP:
        switch(consume(CmdDrop.class, cmd)) {
          case DATABASE: case DB:
            return new DropDB(name(cmd));
          case INDEX:
            return new DropIndex(consume(CmdIndex.class, cmd));
          case USER:
            return new DropUser(name(cmd), key(ON, null) ? name(cmd) : null);
          case BACKUP:
            return new DropBackup(name(cmd));
        }
        break;
      case OPTIMIZE:
        return new Optimize();
      case EXPORT:
        return new Export(string(cmd));
      case XQUERY:
        return new XQuery(xquery(cmd));
      case RUN:
        return new Run(string(cmd));
      case FIND:
        return new Find(string(cmd));
      case CS:
        return new Cs(xquery(cmd));
      case GET:
        return new Get(name(cmd));
      case SET:
        return new Set(name(cmd), string(null));
      case PASSWORD:
        return new Password(string(null));
      case HELP:
        String hc = name(null);
        String form = null;
        if(hc != null) {
          if(hc.equalsIgnoreCase("wiki")) {
            form = hc;
            hc = null;
          } else {
            qp = qm;
            hc = consume(Cmd.class, cmd).toString();
            form = name(null);
          }
        }
        return new Help(hc, form);
      case EXIT: case QUIT:
        return new Exit();
      case KILL:
        return new Kill(name(cmd));
      case BACKUP:
        return new Backup(name(cmd));
      case RESTORE:
        return new Restore(name(cmd));
      case SHOW:
        switch(consume(CmdShow.class, cmd)) {
          case DATABASES:
            return new ShowDatabases();
          case SESSIONS:
            return new ShowSessions();
          case USERS:
            return new ShowUsers(key(ON, null) ? name(cmd) : null);
          case BACKUPS:
            return new ShowBackups();
          default:
        }
        break;
      case GRANT:
        final CmdPerm perm = consume(CmdPerm.class, cmd);
        if(perm == null) help(null, cmd);
        final String db = key(ON, null) ? name(cmd) : null;
        key(TO, cmd);
        return db == null ? new Grant(perm, name(cmd)) :
          new Grant(perm, name(cmd), db);
      default:
    }
    return null;
  }

  /**
   * Parses and returns a string, delimited by a space or semicolon.
   * Quotes can be used to include spaces.
   * @param cmd referring command; if specified, the result must not be empty
   * @return string
   * @throws QueryException query exception
   */
  private String string(final Cmd cmd) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    consumeWS();
    boolean q = false;
    while(more()) {
      final char c = curr();
      if(!q && (c <= ' ' || c == ';')) break;
      if(c == '"') q ^= true;
      else tb.add(c);
      consume();
    }
    return finish(cmd, tb);
  }

  /**
   * Parses and returns the remaining string. Quotes at the beginning and end
   * of the argument will be stripped.
   * @param cmd referring command; if specified, the result must not be empty
   * @return remaining string
   * @throws QueryException query exception
   */
  private String remaining(final Cmd cmd) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    consumeWS();
    while(more()) tb.add(consume());
    String arg = finish(cmd, tb);
    if(arg != null) {
      // chop quotes; substrings are faster than replaces...
      if(arg.startsWith("\"")) arg = arg.substring(1);
      if(arg.endsWith("\"")) arg = arg.substring(0, arg.length() - 1);
    }
    return arg;
  }

  /**
   * Parses and returns an xquery expression.
   * @param cmd referring command; if specified, the result must not be empty
   * @return path
   * @throws QueryException query exception
   */
  private String xquery(final Cmd cmd) throws QueryException {
    consumeWS();
    final TokenBuilder tb = new TokenBuilder();
    if(more() && !curr(';')) {
      final QueryParser p = new QueryParser(query, new QueryContext(ctx));
      p.qp = qp;
      p.parse(null, false);
      tb.add(query.substring(qp, p.qp));
      qp = p.qp;
    }
    return finish(cmd, tb);
  }

  /**
   * Parses and returns a name. A name is limited to letters, digits,
   * underscores, dashes, and periods: {@code [A-Za-z0-9_-]+}.
   * @param cmd referring command; if specified, the result must not be empty
   * @return name
   * @throws QueryException query exception
   */
  private String name(final Cmd cmd) throws QueryException {
    consumeWS();
    final TokenBuilder tb = new TokenBuilder();
    while(letterOrDigit(curr()) || curr('-')) tb.add(consume());
    return finish(cmd, !more() || curr(';') || ws(curr()) ? tb : null);
  }

  /**
   * Parses and returns the specified keyword.
   * @param key token to be parsed
   * @param cmd referring command; if specified, the keyword is mandatory
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean key(final String key, final Cmd cmd) throws QueryException {
    consumeWS();
    final int p = qp;
    final boolean ok = (consume(key) || consume(key.toLowerCase())) &&
      (curr(0) || ws(curr()));
    if(!ok) {
      qp = p;
      if(cmd != null) help(null, cmd);
    }
    return ok;
  }

  /**
   * Parses and returns a string result.
   * @param cmd referring command; if specified, the result must not be empty
   * @param s input string, or {@code null} if invalid
   * @return string result, or {@code null}
   * @throws QueryException query exception
   */
  private String finish(final Cmd cmd, final TokenBuilder s)
      throws QueryException {
    if(s != null && s.size() != 0) return s.toString();
    if(cmd != null) help(null, cmd);
    return null;
  }

  /**
   * Parses and returns a number.
   * @param cmd referring command; if specified, the result must not be empty
   * @return name
   * @throws QueryException query exception
   */
  private String number(final Cmd cmd) throws QueryException {
    consumeWS();
    final TokenBuilder tb = new TokenBuilder();
    if(curr() == '-') tb.add(consume());
    while(digit(curr())) tb.add(consume());
    return finish(cmd, !more() || curr(';') || ws(curr()) ? tb : null);
  }

  /**
   * Consumes all whitespace characters from the beginning of the remaining
   * query.
   */
  private void consumeWS() {
    while(qp < ql && query.charAt(qp) <= ' ') ++qp;
    qm = qp - 1;
  }

  /**
   * Returns the index of the found string or throws an error.
   * @param cmp possible completions
   * @param par parent command
   * @param <E> token type
   * @return index
   * @throws QueryException query exception
   */
  private <E extends Enum<E>> E consume(final Class<E> cmp, final Cmd par)
      throws QueryException {

    final String token = name(null);

    if(!(gui && token != null && token.length() <= 1)) {
    try {
      // return command reference; allow empty strings as input ("NULL")
      final String t = token == null ? "NULL" : token.toUpperCase();
      return Enum.valueOf(cmp, t);
    } catch(final IllegalArgumentException ex) { /* will not happen. */ }
    }

    final Enum<?>[] alt = list(cmp, token);
    if(token == null) {
      // no command found
      if(par == null) error(list(alt), CMDNO);
      // show available command extensions
      help(list(alt), par);
    }

    // output error for similar commands
    final byte[] name = lc(token(token));
    final Levenshtein ls = new Levenshtein();
    for(final Enum<?> s : list(cmp, null)) {
      final byte[] sm = lc(token(s.name().toLowerCase()));
      if(ls.similar(name, sm, 0) && Cmd.class.isInstance(s))
        error(list(alt), CMDSIMILAR, name, sm);
    }

    // unknown command
    if(par == null) error(list(alt), CMDWHICH, token);
    // show available command extensions
    help(list(alt), par);
    return null;
  }

  /**
   * Prints some command info.
   * @param alt input alternatives
   * @param cmd input completions
   * @throws QueryException query exception
   */
  private void help(final StringList alt, final Cmd cmd) throws QueryException {
    error(alt, PROCSYNTAX, cmd.help(true, false));
  }

  /**
   * Returns the command list.
   * @param <T> token type
   * @param en enumeration
   * @param i user input
   * @return completions
   */
  private <T extends Enum<T>> Enum<?>[] list(final Class<T> en,
      final String i) {

    Enum<?>[] list = new Enum<?>[0];
    final String t = i == null ? "" : i.toUpperCase();
    for(final Enum<?> e : en.getEnumConstants()) {
      // ignore hidden commands
      if(Cmd.class.isInstance(e) && Cmd.class.cast(e).hidden()) continue;

      if(e.name().startsWith(t)) {
        final int s = list.length;
        final Enum<?>[] tmp = new Enum<?>[s + 1];
        System.arraycopy(list, 0, tmp, 0, s);
        tmp[s] = e;
        list = tmp;
      }
    }
    return list;
  }

  /**
   * Throws an error.
   * @param comp input completions
   * @param m message
   * @param e extension
   * @throws QueryException query exception
   */
  private void error(final StringList comp, final String m, final Object... e)
      throws QueryException {

    final QueryException qe = new QueryException(input(), "", null, m, e);
    qe.complete(this, comp);
    throw qe;
  }

  /**
   * Converts the specified commands into a string list.
   * @param comp input completions
   * @return string list
   */
  private StringList list(final Enum<?>[] comp) {
    final StringList list = new StringList();
    for(final Enum<?> c : comp) list.add(c.name().toLowerCase());
    return list;
  }
}
