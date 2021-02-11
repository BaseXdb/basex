package org.basex.core.parse;

import static org.basex.core.Text.*;

/**
 * This class defines the available command-line commands.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
@SuppressWarnings("all")
public interface Commands {
  /** Root node of command scripts. */
  String COMMANDS = "commands";

  /** Command string: "add". */
  String ADD = "add";
  /** Command string: "alter-backup". */
  String ALTER_BACKUP = "alter-backup";
  /** Command string: "alter-db". */
  String ALTER_DB = "alter-db";
  /** Command string: "alter-password". */
  String ALTER_PASSWORD = "alter-password";
  /** Command string: "alter-user". */
  String ALTER_USER = "alter-user";
  /** Command string: "check". */
  String CHECK = "check";
  /** Command string: "close". */
  String CLOSE = "close";
  /** Command string: "copy". */
  String COPY = "copy";
  /** Command string: "create-backup". */
  String CREATE_BACKUP = "create-backup";
  /** Command string: "create-db". */
  String CREATE_DB = "create-db";
  /** Command string: "create-index". */
  String CREATE_INDEX = "create-index";
  /** Command string: "create-user". */
  String CREATE_USER = "create-user";
  /** Command string: "delete". */
  String DELETE = "delete";
  /** Command string: "drop-backup". */
  String DROP_BACKUP = "drop-backup";
  /** Command string: "drop-db". */
  String DROP_DB = "drop-db";
  /** Command string: "drop-index". */
  String DROP_INDEX = "drop-index";
  /** Command string: "drop-user". */
  String DROP_USER = "drop-user";
  /** Command string: "execute". */
  String EXECUTE = "execute";
  /** Command string: "exit". */
  String EXIT = "exit";
  /** Command string: "export". */
  String EXPORT = "export";
  /** Command string: "find". */
  String FIND = "find";
  /** Command string: "flush". */
  String FLUSH = "flush";
  /** Command string: "get". */
  String GET = "get";
  /** Command string: "grant". */
  String GRANT = "grant";
  /** Command string: "help". */
  String HELP = "help";
  /** Command string: "info". */
  String INFO = "info";
  /** Command string: "info-db". */
  String INFO_DB = "info-db";
  /** Command string: "info-index". */
  String INFO_INDEX = "info-index";
  /** Command string: "info-storage". */
  String INFO_STORAGE = "info-storage";
  /** Command string: "jobs-list". */
  String JOBS_LIST = "jobs-list";
  /** Command string: "jobs-stop". */
  String JOBS_STOP = "jobs-stop";
  /** Command string: "jobs-result". */
  String JOBS_RESULT = "jobs-result";
  /** Command string: "kill". */
  String KILL = "kill";
  /** Command string: "list". */
  String LIST = "list";
  /** Command string: "open". */
  String OPEN = "open";
  /** Command string: "optimize". */
  String OPTIMIZE = "optimize";
  /** Command string: "optimize-all". */
  String OPTIMIZE_ALL = "optimize-all";
  /** Command string: "password". */
  String PASSWORD = "password";
  /** Command string: "quit". */
  String QUIT = "quit";
  /** Command string: "rename". */
  String RENAME = "rename";
  /** Command string: "replace". */
  String REPLACE = "replace";
  /** Command string: "repo-delete". */
  String REPO_DELETE = "repo-delete";
  /** Command string: "repo-install". */
  String REPO_INSTALL = "repo-install";
  /** Command string: "repo-list". */
  String REPO_LIST = "repo-list";
  /** Command string: "restore". */
  String RESTORE = "restore";
  /** Command string: "retrieve". */
  String RETRIEVE = "retrieve";
  /** Command string: "run". */
  String RUN = "run";
  /** Command string: "inspect". */
  String INSPECT = "inspect";
  /** Command string: "set". */
  String SET = "set";
  /** Command string: "show-backups". */
  String SHOW_BACKUPS = "show-backups";
  /** Command string: "show-sessions". */
  String SHOW_SESSIONS = "show-sessions";
  /** Command string: "show-users". */
  String SHOW_USERS = "show-users";
  /** Command string: "store". */
  String STORE = "store";
  /** Command string: "test". */
  String TEST = "test";
  /** Command string: "xquery". */
  String XQUERY = "xquery";

  /** Command attribute: "path". */
  String PATH = "path";
  /** Command attribute: "name". */
  String NAME = "name";
  /** Command attribute: "pattern". */
  String PATTERN = "pattern";
  /** Command attribute: "newname". */
  String NEWNAME = "newname";
  /** Command attribute: "input". */
  String INPUT = "input";
  /** Command attribute: "query". */
  String QUERY = "query";
  /** Command attribute: "type". */
  String TYPE = "type";
  /** Command attribute: "id". */
  String ID = "id";
  /** Command attribute: "start". */
  String START = "start";
  /** Command attribute: "end". */
  String END = "end";
  /** Command attribute: "database". */
  String DATABASE = "database";
  /** Command attribute: "option". */
  String OPTION = "option";
  /** Command attribute: "permission". */
  String PERMISSION = "permission";
  /** Command attribute: "target". */
  String TARGET = "target";
  /** Command attribute: "newpath". */
  String NEWPATH = "newpath";
  /** Command attribute: "file". */
  String FILE = "file";
  /** Command attribute: "value". */
  String VALUE = "value";
  /** Command attribute: "command". */
  String COMMAND = "command";

  /** Create commands. */
  enum CmdCreate { DATABASE, DB, INDEX, USER, BACKUP }
  /** Info commands. */
  enum CmdInfo { NULL, DATABASE, DB, INDEX, STORAGE }
  /** Drop commands. */
  enum CmdDrop { DATABASE, DB, INDEX, USER, BACKUP }
  /** Optimize commands. */
  enum CmdOptimize { NULL, ALL }
  /** Show commands. */
  enum CmdShow { SESSIONS, USERS, BACKUPS }
  /** Jobs commands. */
  enum CmdJobs { LIST, STOP, RESULT }
  /** Permission commands. */
  enum CmdPerm { NONE, READ, WRITE, CREATE, ADMIN }
  /** Index types. */
  enum CmdIndex { TEXT, ATTRIBUTE, TOKEN, FULLTEXT }
  /** Index types. */
  enum CmdIndexInfo { NULL, TEXT, ATTRIBUTE, TOKEN, FULLTEXT, PATH, ELEMNAME, ATTRNAME }
  /** Alter types. */
  enum CmdAlter { DATABASE, DB, PASSWORD, USER, BACKUP }
  /** Repo types. */
  enum CmdRepo { INSTALL, DELETE, LIST }

  /** Command definitions. */
  enum Cmd {
    ADD(HELPADD), ALTER(HELPALTER), CHECK(HELPCHECK), CLOSE(HELPCLOSE), COPY(HELPCOPY),
    CREATE(HELPCREATE), DELETE(HELPDELETE), DROP(HELPDROP), EXIT(HELPEXIT), EXPORT(HELPEXPORT),
    FIND(HELPFIND), FLUSH(HELPFLUSH), GET(HELPGET), GRANT(HELPGRANT), HELP(HELPHELP),
    INFO(HELPINFO), INSPECT(HELPINSPECT), JOBS(HELPJOBS), KILL(HELPKILL), LIST(HELPLIST),
    OPEN(HELPOPEN), OPTIMIZE(HELPOPTIMIZE), PASSWORD(HELPPASSWORD), QUIT(HELPEXIT),
    RENAME(HELPRENAME), REPLACE(HELPREPLACE), REPO(HELPREPO), RESTORE(HELPRESTORE),
    RETRIEVE(HELPRETRIEVE), RUN(HELPRUN), EXECUTE(HELPEXECUTE), SET(HELPSET), SHOW(HELPSHOW),
    STORE(HELPSTORE), TEST(HELPTEST), XQUERY(HELPXQUERY);

    /** Help texts. */
    private final String[] help;

    /**
     * Default constructor.
     * @param help help texts or {@code null} if command is hidden
     */
    Cmd(final String... help) {
      this.help = help;
    }

    /**
     * Returns a help string.
     * @param detail show details
     * @return string
     */
    public final String help(final boolean detail) {
      final StringBuilder sb = new StringBuilder();
      if(help == null) {
        if(detail) sb.append(NOHELP);
      } else {
        sb.append(this + " " + help[0] + NL + "  " + help[1] + NL);
        if(detail) sb.append(NL + help[2]);
      }
      return sb.toString();
    }
  }
}
