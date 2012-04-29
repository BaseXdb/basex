package org.basex.core;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdAlter;
import org.basex.core.Commands.CmdCreate;
import org.basex.core.Commands.CmdDrop;
import org.basex.core.Commands.CmdIndex;
import org.basex.core.Commands.CmdIndexInfo;
import org.basex.core.Commands.CmdInfo;
import org.basex.core.Commands.CmdOptimize;
import org.basex.core.Commands.CmdPerm;
import org.basex.core.Commands.CmdRepo;
import org.basex.core.Commands.CmdShow;
import org.basex.core.cmd.*;
import org.basex.core.cmd.List;
import org.basex.core.cmd.Set;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This is a parser for command strings, creating {@link Command} instances.
 * Several commands can be formulated in one string and separated by semicolons.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class CommandParser extends InputParser {
  /** Context. */
  private final Context ctx;

  /** Password reader. */
  private PasswordReader passwords;
  /** Suggest possible completions. */
  private boolean suggest;

  /**
   * Constructor.
   * @param in input
   * @param c context
   */
  public CommandParser(final String in, final Context c) {
    super(in);
    ctx = c;
  }

  /**
   * Attaches a password reader.
   * @param pr password reader
   * @return self reference
   */
  public CommandParser password(final PasswordReader pr) {
    passwords = pr;
    return this;
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
    if(more()) throw help(null, cmd);
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
      if(!consume(';')) throw help(null, cmd);
    }
  }

  /**
   * Parses the input and returns a command list.
   * @param s suggest flag
   * @return commands
   * @throws QueryException query exception
   */
  public Command[] parse(final boolean s) throws QueryException {
    suggest = s;
    return parse();
  }

  /**
   * Parses a single command.
   * @param cmd command definition
   * @param s single command expected
   * @return resulting command
   * @throws QueryException query exception
   */
  private Command parse(final Cmd cmd, final boolean s) throws QueryException {
    switch(cmd) {
      case CREATE:
        switch(consume(CmdCreate.class, cmd)) {
          case BACKUP:
            return new CreateBackup(glob(cmd));
          case DATABASE: case DB:
            return new CreateDB(name(cmd), s ? remaining(null) : string(null));
          case INDEX:
            return new CreateIndex(consume(CmdIndex.class, cmd));
          case USER:
            return new CreateUser(name(cmd), password());
          case EVENT:
            return new CreateEvent(name(cmd));
        }
        break;
      case COPY:
        return new Copy(name(cmd), name(cmd));
      case ALTER:
        switch(consume(CmdAlter.class, cmd)) {
          case DATABASE: case DB:
            return new AlterDB(name(cmd), name(cmd));
          case USER:
            return new AlterUser(name(cmd), password());
        }
        break;
      case OPEN:
        return new Open(string(cmd));
      case CHECK:
        return new Check(string(cmd));
      case ADD:
        String arg = key(C_TO, null) ? string(cmd) : null;
        return new Add(arg, s ? remaining(cmd) : string(cmd));
      case STORE:
        arg = key(C_TO, null) ? string(cmd) : null;
        return new Store(arg, s ? remaining(cmd) : string(cmd));
      case RETRIEVE:
        return new Retrieve(string(cmd));
      case DELETE:
        return new Delete(string(cmd));
      case RENAME:
        return new Rename(string(cmd), string(cmd));
      case REPLACE:
        return new Replace(string(cmd), s ? remaining(null) : string(null));
      case INFO:
        switch(consume(CmdInfo.class, cmd)) {
          case NULL:
            return new Info();
          case DATABASE: case DB:
            return new InfoDB();
          case INDEX:
            return new InfoIndex(consume(CmdIndexInfo.class, null));
          case STORAGE:
            String arg1 = number(null);
            final String arg2 = arg1 != null ? number(null) : null;
            if(arg1 == null) arg1 = xquery(null);
            return new InfoStorage(arg1, arg2);
        }
        break;
      case CLOSE:
        return new Close();
      case LIST:
        final String in = string(null);
        return in == null ? new List() : new ListDB(in);
      case DROP:
        switch(consume(CmdDrop.class, cmd)) {
          case DATABASE: case DB:
            return new DropDB(glob(cmd));
          case INDEX:
            return new DropIndex(consume(CmdIndex.class, cmd));
          case USER:
            return new DropUser(glob(cmd), key(ON, null) ? glob(cmd) : null);
          case BACKUP:
            return new DropBackup(glob(cmd));
          case EVENT:
            return new DropEvent(name(cmd));
        }
        break;
      case OPTIMIZE:
        switch(consume(CmdOptimize.class, cmd)) {
          case NULL:
            return new Optimize();
          case ALL:
            return new OptimizeAll();
        }
        break;
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
        return new Password(password());
      case HELP:
        return new Help(name(null));
      case EXIT:
        return new Exit();
      case FLUSH:
        return new Flush();
      case KILL:
        return new Kill(string(cmd));
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
          case EVENTS:
            return new ShowEvents();
          default:
        }
        break;
      case GRANT:
        final CmdPerm perm = consume(CmdPerm.class, cmd);
        if(perm == null) throw help(null, cmd);
        final String db = key(ON, null) ? glob(cmd) : null;
        key(C_TO, cmd);
        return new Grant(perm, glob(cmd), db);
      case REPO:
        switch(consume(CmdRepo.class, cmd)) {
          case INSTALL:
            return new RepoInstall(string(cmd), new InputInfo(this));
          case DELETE:
            return new RepoDelete(string(cmd), new InputInfo(this));
          case LIST:
            return new RepoList();
          default:
        }
        break;
      default:
    }
    throw Util.notexpected("command specified, but not implemented yet");
  }

  /**
   * Parses and returns a string, delimited by a space or semicolon.
   * Quotes can be used to include spaces.
   * @param cmd referring command; if specified, the result must not be empty
   * @return string
   * @throws QueryException query exception
   */
  String string(final Cmd cmd) throws QueryException {
    final StringBuilder sb = new StringBuilder();
    consumeWS();
    boolean q = false;
    while(more()) {
      final char c = curr();
      if(!q && (c <= ' ' || c == ';')) break;
      if(c == '"') q ^= true;
      else sb.append(c);
      consume();
    }
    return finish(cmd, sb);
  }

  /**
   * Parses and returns the remaining string. Quotes at the beginning and end
   * of the argument will be stripped.
   * @param cmd referring command; if specified, the result must not be empty
   * @return remaining string
   * @throws QueryException query exception
   */
  private String remaining(final Cmd cmd) throws QueryException {
    final StringBuilder sb = new StringBuilder();
    consumeWS();
    while(more()) sb.append(consume());
    String arg = finish(cmd, sb);
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
    final StringBuilder sb = new StringBuilder();
    if(more() && !curr(';')) {
      final QueryParser p = new QueryParser(input, new QueryContext(ctx));
      p.ip = ip;
      p.parse(null);
      sb.append(input.substring(ip, p.ip));
      ip = p.ip;
    }
    return finish(cmd, sb);
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
    final StringBuilder sb = new StringBuilder();
    while(letterOrDigit(curr()) || curr('-')) sb.append(consume());
    return finish(cmd, !more() || curr(';') || ws(curr()) ? sb : null);
  }

  /**
   * Parses and returns a password string.
   * @return password string
   * @throws QueryException query exception
   */
  private String password() throws QueryException {
    final String pw = string(null);
    return pw != null ? pw : passwords == null ? "" : passwords.password();
  }

  /**
   * Parses and returns a glob expression, which extends the {@link #name}
   * with asterisks, question marks and commands. See {@link IOFile#regex}
   * for more details.
   * @param cmd referring command; if specified, the result must not be empty
   * @return glob expression
   * @throws QueryException query exception
   */
  private String glob(final Cmd cmd) throws QueryException {
    consumeWS();
    final StringBuilder sb = new StringBuilder();
    while(true) {
      final char c = curr();
      if(!letterOrDigit(c) && c != '-' && c != '*' && c != '?' && c != ',') {
        return finish(cmd, !more() || curr(';') || ws(curr()) ? sb : null);
      }
      sb.append(consume());
    }
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
    final int p = ip;
    final boolean ok = (consume(key) ||
        consume(key.toLowerCase(Locale.ENGLISH))) && (curr(0) || ws(curr()));
    if(!ok) {
      ip = p;
      if(cmd != null) throw help(null, cmd);
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
  private String finish(final Cmd cmd, final StringBuilder s) throws QueryException {
    if(s != null && s.length() != 0) return s.toString();
    if(cmd != null) throw help(null, cmd);
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
    final StringBuilder sb = new StringBuilder();
    if(curr() == '-') sb.append(consume());
    while(digit(curr())) sb.append(consume());
    return finish(cmd, !more() || curr(';') || ws(curr()) ? sb : null);
  }

  /**
   * Consumes all whitespace characters from the beginning of the remaining
   * query.
   */
  private void consumeWS() {
    while(ip < il && input.charAt(ip) <= ' ') ++ip;
    im = ip - 1;
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

    if(!(suggest && token != null && token.length() <= 1)) {
    try {
      // return command reference; allow empty strings as input ("NULL")
      final String t = token == null ? "NULL" :
        token.toUpperCase(Locale.ENGLISH);
      return Enum.valueOf(cmp, t);
    } catch(final IllegalArgumentException ex) { /* will not happen. */ }
    }

    final Enum<?>[] alt = list(cmp, token);
    if(token == null) {
      // show command error or available command extensions
      throw par == null ? error(list(alt), EXPECTING_CMD) :
        help(list(alt), par);
    }

    // output error for similar commands
    final byte[] name = uc(token(token));
    final Levenshtein ls = new Levenshtein();
    for(final Enum<?> s : list(cmp, null)) {
      final byte[] sm = uc(token(s.name()));
      if(ls.similar(name, sm, 0) && Cmd.class.isInstance(s))
        throw error(list(alt), UNKNOWN_SIMILAR_X, name, sm);
    }

    // show unknown command error or available command extensions
    throw par == null ? error(list(alt), UNKNOWN_TRY_X, token) :
      help(list(alt), par);
  }

  /**
   * Returns help output as query exception instance.
   * Prints some command info.
   * @param alt input alternatives
   * @param cmd input completions
   * @return QueryException query exception
   */
  private QueryException help(final StringList alt, final Cmd cmd) {
    return error(alt, SYNTAX_X, cmd.help(true));
  }

  /**
   * Returns the command list.
   * @param <T> token type
   * @param en enumeration
   * @param i user input
   * @return completions
   */
  private static <T extends Enum<T>> Enum<?>[] list(final Class<T> en, final String i) {
    Enum<?>[] list = new Enum<?>[0];
    final String t = i == null ? "" : i.toUpperCase(Locale.ENGLISH);
    for(final Enum<?> e : en.getEnumConstants()) {
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
   * Returns a query exception instance.
   * @param comp input completions
   * @param m message
   * @param e extension
   * @return query exception
   */
  private QueryException error(final StringList comp, final String m, final Object... e) {
    return new QueryException(info(), new QNm(), m, e).suggest(this, comp);
  }

  /**
   * Converts the specified commands into a string list.
   * @param comp input completions
   * @return string list
   */
  private static StringList list(final Enum<?>[] comp) {
    final StringList list = new StringList();
    for(final Enum<?> c : comp) list.add(c.name().toLowerCase(Locale.ENGLISH));
    return list;
  }
}

