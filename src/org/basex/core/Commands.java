package org.basex.core;

import static org.basex.Text.*;

/**
 * This class defines the available command-line commands.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
@SuppressWarnings("all")
public interface Commands {
  /** Command definitions. */
  enum Cmd {
    // DATABASE COMMANDS
    DUMMYDATABASE(false), CREATE(true), OPEN(true), INFO(true),
    CLOSE(true), LIST(true), DROP(true), OPTIMIZE(true), EXPORT(true),
    // QUERY COMMANDS
    DUMMYQUERY(false), XQUERY(true), XQUERYMV(false), RUN(true),
    FIND(true), CS(true),
    // UPDATE COMMANDS
    DUMMYUPDATE(false), COPY(true), DELETE(true), INSERT(true), UPDATE(true),
    // GENERAL COMMANDS
    DUMMYGENERAL(false), SET(true), HELP(true), PING(false), PROMPT(false),
    EXIT(true), QUIT(false),
    // SERVER COMMANDS
    GETRESULT(false), GETINFO(false), STOP(false);

    /** Flag for official (public) commands. */
    final boolean official;

    /**
     * Constructor, initializing the constants.
     * @param o official command flag
     */
    private Cmd(final boolean o) {
      official = o;
    }

    /**
     * Returns a help string.
     * @param detail show details
     * @param all show all commands (also hidden ones)
     * @return string
     */
    public final String help(final boolean detail, final boolean all) {
      final StringBuilder sb = new StringBuilder();

      final Object args = help(0);
      if(name().startsWith("DUMMY")) {
        sb.append(NL + args + NL + NL);
      } else if(args != null && (official || detail || all)) {
        sb.append(name().toLowerCase() + " " + args + NL + "  " + help(1) + NL);
        if(detail) sb.append(NL + help(2) + NL);
      } else {
        if(detail) sb.append(NOHELP + NL);
      }
      return sb.toString();
    }

    /**
     * Returns the specified help text for the current command.
     * @param n help offset
     * @return text or null
     */
    private String help(final int n) {
      try {
        return org.basex.Text.class.getField(name() + n).get(null).toString();
      } catch(final Exception ex) {
        return null;
      }
    }
  }

  /** Create Command definitions. */
  enum CmdCreate { DATABASE, DB, MAB2, MAB, FS, XML, INDEX }

  /** Index Types definition. */
  enum CmdIndex { TEXT, ATTRIBUTE, FULLTEXT }

  /** Info command definitions. */
  enum CmdInfo { NULL, DATABASE, DB, INDEX, TABLE }

  /** Drop command definitions. */
  enum CmdDrop { DATABASE, DB, INDEX }

  /** Filesystem command definitions. */
  enum CmdFS { CAT, CD, CP, DU, EXT, EXIT, HELP, LOCATE,
    L, LS, MKDIR, PWD, RM, TOUCH }

  /** Insert command definitions. */
  enum CmdUpdate { FRAGMENT, ELEMENT, TEXT, ATTRIBUTE, COMMENT, PI }

  /** Set command definitions. */
  enum CmdSet {
    INFO, DEBUG, SERIALIZE, XMLOUTPUT, MAINMEM, CHOP,
    ENTITY, TEXTINDEX, ATTRINDEX, FTINDEX
  }
}
