package org.basex.core.parse;

import static org.basex.core.parse.Commands.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.Check;
import org.basex.core.cmd.List;
import org.basex.core.cmd.Set;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This is a parser for XML input, creating {@link Command} instances.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class XMLParser extends CommandParser {
  /**
   * Constructor.
   * @param input input
   * @param context context
   */
  XMLParser(final String input, final Context context) {
    super(input, context);
  }

  @Override
  protected void parse(final ArrayList<Command> cmds) throws QueryException {
    try {
      final DBNode node = new DBNode(IO.get(input));
      String query = "/*";
      if(!execute(COMMANDS, node).isEmpty()) {
        query = COMMANDS + query;
        // ensure that the root contains no text nodes as children
        final String ws = COMMANDS + "/text()[normalize-space()]";
        try(QueryProcessor qp = new QueryProcessor(ws, ctx)) {
          qp.context(node);
          if(!qp.value().isEmpty())
            throw error(Text.SYNTAX_X, '<' + COMMANDS + "><...></" + COMMANDS + '>');
        }
      }
      try(QueryProcessor qp = new QueryProcessor(query, ctx)) {
        qp.context(node);
        for(final Item ia : qp.value()) cmds.add(command(ia).baseURI(uri));
      }
    } catch(final IOException ex) {
      throw error(Text.STOPPED_AT + '%', ex);
    }
  }

  /**
   * Returns a command.
   * @param root command node
   * @return command
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  private Command command(final Item root) throws IOException, QueryException {
    final String e = ((ANode) root).qname().toJava().toString();
    if(e.equals(ADD) && check(root, PATH + '?', '<' + INPUT))
      return new Add(value(root, PATH), xml(root));
    if(e.equals(ALTER_BACKUP) && check(root, NAME, NEWNAME))
      return new AlterBackup(value(root, NAME), value(root, NEWNAME));
    if(e.equals(ALTER_DB) && check(root, NAME, NEWNAME))
      return new AlterDB(value(root, NAME), value(root, NEWNAME));
    if(e.equals(ALTER_PASSWORD) && check(root, NAME, '#' + PASSWORD + '?'))
      return new AlterPassword(value(root, NAME), password(root));
    if(e.equals(ALTER_USER) && check(root, NAME, NEWNAME))
      return new AlterUser(value(root, NAME), value(root, NEWNAME));
    if(e.equals(CHECK) && check(root, INPUT))
      return new Check(value(root, INPUT));
    if(e.equals(CLOSE) && check(root))
      return new Close();
    if(e.equals(COPY) && check(root, NAME, NEWNAME))
      return new Copy(value(root, NAME), value(root, NEWNAME));
    if(e.equals(CREATE_BACKUP) && check(root, NAME))
      return new CreateBackup(value(root, NAME));
    if(e.equals(CREATE_DB) && check(root, NAME, '<' + INPUT + '?'))
      return new CreateDB(value(root, NAME), xml(root));
    if(e.equals(CREATE_INDEX) && check(root, TYPE))
      return new CreateIndex(value(root, TYPE));
    if(e.equals(CREATE_USER) && check(root, NAME, '#' + PASSWORD + '?'))
      return new CreateUser(value(root, NAME), password(root));
    if(e.equals(DELETE) && check(root, PATH))
      return new Delete(value(root, PATH));
    if(e.equals(DROP_BACKUP) && check(root, NAME))
      return new DropBackup(value(root, NAME));
    if(e.equals(DROP_DB) && check(root, NAME))
      return new DropDB(value(root, NAME));
    if(e.equals(DROP_INDEX) && check(root, TYPE))
      return new DropIndex(value(root, TYPE));
    if(e.equals(DROP_USER) && check(root, NAME, PATTERN + '?'))
      return new DropUser(value(root, NAME), value(root, PATTERN));
    if(e.equals(EXIT) && check(root))
      return new Exit();
    if(e.equals(EXPORT) && check(root, PATH))
      return new Export(value(root, PATH));
    if(e.equals(FIND) && check(root, '#' + QUERY))
      return new Find(value(root));
    if(e.equals(FLUSH) && check(root))
      return new Flush();
    if(e.equals(GET) && check(root, OPTION + '?'))
      return new Get(value(root, OPTION));
    if(e.equals(GRANT) && check(root, NAME, PERMISSION, PATTERN + '?'))
      return new Grant(value(root, PERMISSION), value(root, NAME), value(root, PATTERN));
    if(e.equals(HELP) && check(root, '#' + COMMAND + '?'))
      return new Help(value(root));
    if(e.equals(INFO) && check(root))
      return new Info();
    if(e.equals(INFO_DB) && check(root))
      return new InfoDB();
    if(e.equals(INFO_INDEX) && check(root, TYPE + '?'))
      return new InfoIndex(value(root, TYPE));
    if(e.equals(INFO_STORAGE) && check(root, START + '?', END + '?'))
      return new InfoStorage(value(root, START), value(root, END));
    if(e.equals(JOBS_LIST))
      return new JobsList();
    if(e.equals(JOBS_STOP) && check(root, ID))
      return new JobsStop(value(root, ID));
    if(e.equals(JOBS_RESULT) && check(root, ID))
      return new JobsResult(value(root, ID));
    if(e.equals(KILL) && check(root, TARGET))
      return new Kill(value(root, TARGET));
    if(e.equals(LIST) && check(root, NAME + '?', PATH + '?'))
      return new List(value(root, NAME), value(root, PATH));
    if(e.equals(OPEN) && check(root, NAME, PATH + '?'))
      return new Open(value(root, NAME), value(root, PATH));
    if(e.equals(OPTIMIZE) && check(root))
      return new Optimize();
    if(e.equals(OPTIMIZE_ALL) && check(root))
      return new OptimizeAll();
    if(e.equals(PASSWORD) && check(root, '#' + PASSWORD + '?'))
      return new Password(password(root));
    if(e.equals(QUIT) && check(root))
      return new Exit();
    if(e.equals(RENAME) && check(root, PATH, NEWPATH))
      return new Rename(value(root, PATH), value(root, NEWPATH));
    if(e.equals(REPLACE) && check(root, PATH, '<' + INPUT))
      return new Replace(value(root, PATH), xml(root));
    if(e.equals(REPO_DELETE) && check(root, NAME))
      return new RepoDelete(value(root, NAME), null);
    if(e.equals(REPO_INSTALL) && check(root, PATH))
      return new RepoInstall(value(root, PATH), null);
    if(e.equals(REPO_LIST) && check(root))
      return new RepoList();
    if(e.equals(RESTORE) && check(root, NAME))
      return new Restore(value(root, NAME));
    if(e.equals(RETRIEVE) && check(root, PATH))
      return new Retrieve(value(root, PATH));
    if(e.equals(RUN) && check(root, FILE))
      return new Run(value(root, FILE));
    if(e.equals(EXECUTE) && check(root, '<' + INPUT))
      return new Execute(xml(root));
    if(e.equals(INSPECT) && check(root))
      return new Inspect();
    if(e.equals(SET) && check(root, OPTION, '#' + VALUE + '?'))
      return new Set(value(root, OPTION), value(root));
    if(e.equals(SHOW_BACKUPS) && check(root))
      return new ShowBackups();
    if(e.equals(SHOW_SESSIONS) && check(root))
      return new ShowSessions();
    if(e.equals(SHOW_USERS) && check(root, DATABASE + '?'))
      return new ShowUsers(value(root, DATABASE));
    if(e.equals(STORE) && check(root, PATH + '?', '<' + INPUT))
      return new Store(value(root, PATH), xml(root));
    if(e.equals(TEST) && check(root, PATH))
      return new Test(value(root, PATH));
    if(e.equals(XQUERY) && check(root, '#' + QUERY))
      return new XQuery(value(root));
    throw error(Text.UNKNOWN_CMD_X, '<' + e + "/>");
  }

  /**
   * Returns the value of the specified attribute.
   * @param root root node
   * @param att name of attribute
   * @return value
   * @throws QueryException query exception
   */
  private String value(final Item root, final String att) throws QueryException {
    return execute("string(@" + att + ')', root);
  }

  /**
   * Returns a string value (text node).
   * @param root root node
   * @return string value
   * @throws QueryException query exception
   */
  private String value(final Item root) throws QueryException {
    return execute("string(.)", root);
  }

  /**
   * Returns a password (text node).
   * @param root root node
   * @return password string
   * @throws QueryException query exception
   */
  private String password(final Item root) throws QueryException {
    final String pw = execute("string(.)", root);
    return pw.isEmpty() && pwReader != null ? pwReader.password() : pw;
  }

  /**
   * Returns an xml value (text node).
   * @param root root node
   * @return xml value
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  private String xml(final Item root) throws IOException, QueryException {
    try(QueryProcessor qp = new QueryProcessor("node()", ctx)) {
      return qp.context(root).value().serialize().toString().trim();
    }
  }

  /**
   * Executes the specified query and returns a string representation.
   * @param query query
   * @param context context node
   * @return string representation
   * @throws QueryException query exception
   */
  private String execute(final String query, final Item context) throws QueryException {
    try(QueryProcessor qp = new QueryProcessor(query, ctx)) {
      qp.context(context);
      final Iter iter = qp.iter();
      final Item item = iter.next();
      return item == null ? "" : item.toJava().toString().trim();
    }
  }

  /**
   * Checks the syntax of the specified command. Returns an error with the expected syntax if the
   * check fails. The passed on strings describe the arguments of a command. They may be:
   * <ul>
   *   <li> attribute names</li>
   *   <li> labels for text nodes if prefixed with "#"</li>
   *   <li> labels for text or descendant nodes if prefixed with "<"</li>
   * </ul>
   * Arguments are optional if they suffixed with "?". Examples:
   * <ul>
   *   <li> <code>{"name","#input?"}</code> indicates that the command must have one "name"
   *     attribute and may have one text node, but nothing else</li>
   *   <li> <code>{}</code> means that the command must not have any arguments }</li>
   * </ul>
   * @param root root node
   * @param checks checks to be performed
   * @return success flag
   * @throws QueryException query exception
   */
  private boolean check(final Item root, final String... checks) throws QueryException {
    // prepare validating query
    final TokenList mand = new TokenList(), opt = new TokenList();
    String t = null;
    boolean ot = true;
    boolean n = false;
    for(String check : checks) {
      final boolean o = Strings.endsWith(check, '?');
      check = check.replace("?", "");
      if(!check.isEmpty() && !Character.isLetter(check.charAt(0))) {
        // textual contents
        t = check.substring(1);
        ot = o;
        n = check.charAt(0) == '<';
      } else {
        (o ? opt : mand).add(check);
      }
    }

    // build validating query
    final TokenBuilder tb = new TokenBuilder();
    tb.add("declare variable $A external;");
    tb.add("declare variable $O external;");
    tb.add(".");
    // check existence of mandatory attributes
    tb.add("[every $e in $A satisfies @*/name() = $e]");
    // check existence of unknown attributes
    tb.add("[every $e in @* satisfies $e/name() = ($A,$O)]");
    // ensure that all values are non-empty
    tb.add("[every $e in @* satisfies data($e)]");
    if(t == null) {
      // ensure that no children exist
      tb.add("[not(node())]");
    } else if(!ot) {
      // ensure that children exist
      tb.add("[node()]");
      if(!n) tb.add("[not(*)]");
    }

    // run query
    try(QueryProcessor qp = new QueryProcessor(tb.toString(), ctx)) {
      qp.context(root);
      qp.bind("A", StrSeq.get(mand.toArray())).bind("O", StrSeq.get(opt.toArray()));
      if(!qp.value().isEmpty()) return true;
    }
    // build error string
    final TokenBuilder syntax = new TokenBuilder();
    final byte[] nm = ((ANode) root).qname().string();
    syntax.reset().add('<').add(nm);
    for(final byte[] m : mand) syntax.add(' ').add(m).add("=\"...\"");
    for(final byte[] o : opt) syntax.add(" (").add(o).add("=\"...\")");
    if(t != null) {
      syntax.add('>');
      if(ot) syntax.add('(');
      syntax.add('[').add(t).add(']');
      if(ot) syntax.add(')');
      syntax.add("</").add(nm).add('>');
    } else {
      syntax.add("/>");
    }
    throw error(Text.SYNTAX_X, syntax);
  }

  /**
   * Returns a query exception instance.
   * @param msg message
   * @param ext message extension
   * @return query exception
   */
  private static QueryException error(final String msg, final Object... ext) {
    return new QueryException(null, QNm.EMPTY, msg, ext);
  }
}
