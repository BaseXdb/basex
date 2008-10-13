package org.basex.core;

import static org.basex.Text.*;
import static org.basex.util.Token.*;
import org.basex.core.Commands.COMMANDS;
import org.basex.core.Commands.CREATE;
import org.basex.core.Commands.DROP;
import org.basex.core.Commands.FS;
import org.basex.core.Commands.INDEX;
import org.basex.core.Commands.INFO;
import org.basex.core.Commands.SET;
import org.basex.core.Commands.UPDATE;
import org.basex.core.proc.Cs;
import org.basex.core.proc.Check;
import org.basex.core.proc.Close;
import org.basex.core.proc.Copy;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.CreateFS;
import org.basex.core.proc.CreateIndex;
import org.basex.core.proc.CreateMAB;
import org.basex.core.proc.Delete;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.DropIndex;
import org.basex.core.proc.Exit;
import org.basex.core.proc.Export;
import org.basex.core.proc.Find;
import org.basex.core.proc.Fs;
import org.basex.core.proc.GetInfo;
import org.basex.core.proc.GetResult;
import org.basex.core.proc.Help;
import org.basex.core.proc.Info;
import org.basex.core.proc.InfoDB;
import org.basex.core.proc.InfoIndex;
import org.basex.core.proc.InfoTable;
import org.basex.core.proc.Insert;
import org.basex.core.proc.List;
import org.basex.core.proc.Open;
import org.basex.core.proc.Optimize;
import org.basex.core.proc.Ping;
import org.basex.core.proc.Prompt;
import org.basex.core.proc.Set;
import org.basex.core.proc.Update;
import org.basex.core.proc.XMark;
import org.basex.core.proc.XPath;
import org.basex.core.proc.XPathMV;
import org.basex.core.proc.XQEnv;
import org.basex.core.proc.XQuery;
import org.basex.query.QueryException;
import org.basex.query.QueryParser;
import org.basex.query.xpath.XPParser;
import org.basex.query.xpath.XPSuggest;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQParser;
import org.basex.util.Array;
import org.basex.util.Levenshtein;
import org.basex.util.StringList;

/**
 * This is a parser for command strings, creating {@link Process} instances.
 * Several commands can be formulated in one string and separated by semicolons.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class CommandParser extends QueryParser {
  /** Context. */
  public Context ctx;
  
  /**
   * Constructor, parsing the input queries.
   * @param in query input
   */
  public CommandParser(final String in) {
    init(in);
  }

  /**
   * Constructor, parsing the input queries.
   * @param in query input
   * @param c context
   */
  public CommandParser(final String in, final Context c) {
    this(in);
    ctx = c;
  }

  /**
   * Parses the input and returns a command list.
   * @return commands
   * @throws QueryException query exception
   */
  public Process[] parse() throws QueryException {
    Process[] list = new Process[0];
    if(!more()) return list;

    boolean fsmode = Prop.fsmode;
    while(true) {
      Process proc;
      COMMANDS cmd = null;
      if(fsmode) {
        cmd = COMMANDS.BASH;
        FS fs = FS.EXT;
        String args = name(null);
        try {
          fs = Enum.valueOf(FS.class, args.toUpperCase());
          if(fs == Commands.FS.EXIT) fsmode = false;
          args = "";
        } catch(final IllegalArgumentException ex) {
        }
        final String a = string(null);
        proc = new Fs(fs.name(), a == null ? args : args + " " + a);
      } else {
        cmd = consume(COMMANDS.class, null);
        proc = parse(cmd);
        if(proc instanceof Fs) fsmode = true;
      }
      list = Array.add(list, proc);
      consumeWS();
      if(!more()) return list;
      if(!consume(';')) help(null, cmd);
    }
  }

  /**
   * Parse command.
   * @param cmd command
   * @return process
   * @throws QueryException query exception
   */
  private Process parse(final COMMANDS cmd) throws QueryException {
    switch(cmd) {
      case CREATE:
        switch(consume(CREATE.class, cmd)) {
          case DATABASE: case DB: case XML:
            return new CreateDB(path(cmd), name(null));
          case MAB: case MAB2:
            return new CreateMAB(path(cmd), name(null));
          case FS:
            return new CreateFS(path(cmd), name(null));
          case INDEX:
            return new CreateIndex(consume(INDEX.class, cmd));
        }
        break;
      case OPEN:
        return new Open(name(cmd));
      case INFO:
        switch(consume(INFO.class, cmd)) {
          case NULL:
            return new Info();
          case DATABASE: case DB:
            return new InfoDB();
          case INDEX:
            return new InfoIndex();
          case TABLE:
            String arg1 = number(null);
            final String arg2 = arg1 != null ? number(null) : null;
            if(arg1 == null) arg1 = xpath(null);
            return new InfoTable(arg1, arg2);
        }
        break;
      case CHECK:
        return new Check(name(cmd));
      case CLOSE:
        return new Close();
      case LIST:
        return new List();
      case DROP:
        switch(consume(DROP.class, cmd)) {
          case DATABASE: case DB:
            return new DropDB(name(null));
          case INDEX:
            return new DropIndex(consume(INDEX.class, cmd));
        }
        break;
      case OPTIMIZE:
        return new Optimize();
      case EXPORT:
        return new Export(path(cmd));
      case XPATH:
        return new XPath(xpath(null));
      case XPATHMV:
        return new XPathMV(number(cmd), number(cmd), xpath(cmd));
      case XQUERY:
        return new XQuery(xquery(cmd));
      case XQUENV:
        return new XQEnv(xquery(null));
      case FIND:
        return new Find(string(cmd));
      case XMARK:
        return new XMark(number(cmd));
      case CS:
        return new Cs(xpath(null));
      case COPY:
        return new Copy(number(cmd), xpath(cmd), xpath(cmd));
      case DELETE:
        return new Delete(xpath(cmd));
      case INSERT:
        final UPDATE ins = consume(UPDATE.class, cmd);
        switch(ins) {
          case FRAGMENT:
            return new Insert(ins, string(cmd, true), number(cmd), xpath(cmd));
          case ELEMENT: case TEXT: case COMMENT:
            return new Insert(ins, name(cmd), number(cmd), xpath(cmd));
          case PI:
            return new Insert(ins, name(cmd), name(cmd), number(cmd),
                xpath(cmd));
          case ATTRIBUTE:
            return new Insert(ins, name(cmd), name(cmd), xpath(cmd));
        }
        break;
      case UPDATE:
        final UPDATE upd = consume(UPDATE.class, cmd);
        switch(upd) {
          case ELEMENT: case TEXT: case COMMENT:
            return new Update(upd, name(cmd), xpath(cmd));
          case PI:
          case ATTRIBUTE:
            return new Update(upd, name(cmd), name(cmd), xpath(cmd));
          default:
        }
        break;
      case SET:
        final String opt = name(cmd).toUpperCase();
        String val = string(null, false);
        try {
          final Object o = Prop.class.getField(opt.toLowerCase()).get(null);
          if(val != null) {
            if(o instanceof Boolean) {
              val = val.toUpperCase();
              final boolean info = opt.equals(SET.INFO.name());
              if(!val.equals(ON) && !val.equals(OFF) &&
                  !(info && val.equals(ALL))) {
                final StringList sl = new StringList();
                sl.add(OFF);
                sl.add(ON);
                if(info) sl.add(ALL);
                help(sl, cmd);
              }
            } else if(o instanceof Integer) {
              if(toInt(val) == Integer.MIN_VALUE) help(null, cmd);
            }
          }
          return new Set(opt, val);
        } catch(final IllegalAccessException ex) {
          help(list(SET.class, opt), cmd);
        } catch(final NoSuchFieldException ex) {
          help(list(SET.class, opt), cmd);
        }
        break;
      case HELP:
        String hc = name(null);
        if(hc != null && !hc.toUpperCase().equals(ALL)) {
          qp = qm;
          hc = consume(COMMANDS.class, cmd).toString();
        }
        return new Help(hc);
      case PING:
        return new Ping();
      case PROMPT:
        return new Prompt();
      case GETRESULT:
        return new GetResult(number(null));
      case GETINFO:
        return new GetInfo(number(null));
      case BASH:
        return new Fs();
      case EXIT:
      case QUIT:
        return new Exit();
      default: // DUMMY CASES
    }
    return null;
  }

  /**
   * Parses and returns a string delimited by a space.
   * @param cmd referring command; if specified, the result mustn't be empty
   * @param spc accept spaces as delimiters
   * @return path
   * @throws QueryException query exception
   */
  private String string(final COMMANDS cmd, final boolean spc)
      throws QueryException {
    final StringBuilder tb = new StringBuilder();
    consumeWS();
    char q = 0;
    while(more()) {
      char ch = curr();
      if(q != 0 && ch == '\\') {
        consume();
        if(more()) ch = curr();
      } else {
        if((spc && ch <= ' ' || ch == ';') && q == 0) break;
        if(quote(ch)) q = q == 0 ? ch : q == ch ? 0 : q;
      }
      tb.append(ch);
      consume();
    }
    return finish(cmd, tb);
  }

  /**
   * Parses and returns a string delimited by a space.
   * @param cmd referring command; if specified, the result mustn't be empty
   * @return path
   * @throws QueryException query exception
   */
  private String string(final COMMANDS cmd) throws QueryException {
    return string(cmd, false);
  }

  /**
   * Parses and returns a file path.
   * @param cmd referring command; if specified, the result mustn't be empty
   * @return path
   * @throws QueryException query exception
   */
  private String path(final COMMANDS cmd) throws QueryException {
    return string(cmd, true);
  }

  /**
   * Parses and returns an xpath expression (prototype).
   * @param cmd referring command; if specified, the result mustn't be empty
   * @return path
   * @throws QueryException query exception
   */
  private String xpath(final COMMANDS cmd) throws QueryException {
    consumeWS();
    final StringBuilder sb = new StringBuilder();
    if(more() && !curr(';')) {
      final XPParser p = ctx != null ? new XPSuggest(qu, ctx) :
        new XPParser(qu);
      p.qp = qp;
      p.parse(false);
      sb.append(qu.substring(qp, p.qp));
      qp = p.qp;
    }
    return finish(cmd, sb);
  }

  /**
   * Parses and returns an xquery expression (prototype).
   * @param cmd referring command; if specified, the result mustn't be empty
   * @return path
   * @throws QueryException query exception
   */
  private String xquery(final COMMANDS cmd) throws QueryException {
    consumeWS();
    final StringBuilder sb = new StringBuilder();
    if(more() && !curr(';')) {
      final XQParser p = new XQParser(new XQContext());
      p.init(qu);
      p.qp = qp;
      p.parse(null, false);
      sb.append(qu.substring(qp, p.qp));
      qp = p.qp;
    }
    return finish(cmd, sb);
  }

  /**
   * Parses and returns a name.
   * @param cmd referring command; if specified, the result mustn't be empty.
   * @return name
   * @throws QueryException query exception
   */
  private String name(final COMMANDS cmd) throws QueryException {
    consumeWS();
    final StringBuilder sb = new StringBuilder();
    while(curr('.') || letterOrDigit(curr())) sb.append(consume());
    return finish(cmd, sb);
  }

  /**
   * Parses and returns a name.
   * @param cmd referring command; if specified, the result mustn't be empty.
   * @param s input string
   * @return name
   * @throws QueryException query exception
   */
  private String finish(final COMMANDS cmd, final StringBuilder s)
      throws QueryException {
    if(s.length() != 0) return s.toString();
    if(cmd != null) help(null, cmd);
    return null;
  }

  /**
   * Parses and returns a number.
   * @param cmd referring command; if specified, the result mustn't be empty.
   * @return name
   * @throws QueryException query exception
   */
  private String number(final COMMANDS cmd) throws QueryException {
    consumeWS();
    final StringBuilder tb = new StringBuilder();
    if(curr() == '-') tb.append(consume());
    while(digit(curr())) tb.append(consume());
    if(tb.length() != 0) return tb.toString();
    if(cmd != null) help(null, cmd);
    return null;
  }

  /**
   * Returns the index of the found string or throws an error.
   * @param <E> token type
   * @param cmp possible completions
   * @param par parent command
   * @return index
   * @throws QueryException query exception
   */
  private <E extends Enum<E>> E consume(final Class<E> cmp, final COMMANDS par)
      throws QueryException {

    final String token = name(null);
    try {
      final String t = token == null ? "NULL" : token.toUpperCase();
      if(!t.startsWith("DUMMY")) return Enum.valueOf(cmp, t);
    } catch(final IllegalArgumentException ex) { }

    final StringList alt = list(cmp, token);
    if(token == null) {
      if(par == null) error(alt, CMDNO);
      help(alt, par);
    }

    // find similar commands
    final byte[] name = lc(token(token));
    for(final Enum<?> e : cmp.getEnumConstants()) {
      if(e instanceof COMMANDS && !((COMMANDS) e).official) continue;
      final byte[] sm = lc(token(e.name()));
      if(Levenshtein.similar(name, sm)) error(alt, CMDSIMILAR, name, sm);
    }

    if(par == null) error(alt, CMDWHICH, token);
    help(alt, par);
    return null;
  }

  /**
   * Prints some command info.
   * @param alt input alternatives
   * @param cmd input completions
   * @throws QueryException query exception
   */
  private void help(final StringList alt, final COMMANDS cmd)
      throws QueryException {
    
    if(cmd == COMMANDS.BASH) throw new QueryException(
        org.basex.fs.Help.help(""));
    error(alt, PROCSYNTAX, cmd.help(true, true));
  }

  /**
   * Returns the command list.
   * @param <T> token type
   * @param en enumeration
   * @param i user input
   * @return completions
   */
  private <T extends Enum<T>> StringList list(final Class<T> en,
      final String i) {
    final StringList list = new StringList();
    final String t = i == null ? "" : i.toUpperCase();
    for(final Enum<?> e : en.getEnumConstants()) {
      if(e instanceof COMMANDS && !((COMMANDS) e).official) continue;
      if(e.name().startsWith(t)) list.add(e.name().toLowerCase());
    }
    list.sort();
    return list;
  }

  /**
   * Throws an error.
   * @param comp input completions
   * @param m message
   * @param e extension
   * @throws QueryException query exception
   */
  public void error(final StringList comp, final String m, final Object... e)
      throws QueryException {
    final QueryException qe = new QueryException(m, e);
    qe.complete(this, comp);
    throw qe;
  }
}
