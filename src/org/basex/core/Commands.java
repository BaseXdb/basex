package org.basex.core;

import static org.basex.Text.*;

/**
 * This class defines the project commands.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
@SuppressWarnings("all")
public interface Commands {
  /** Commands. */
  enum COMMANDS {
    // DATABASE COMMANDS
    DUMMYDATABASE(), CREATE(true), OPEN(true), INFO(true), CHECK(),
    CLOSE(true), LIST(true), DROP(true), OPTIMIZE(true), EXPORT(true),
    // QUERY COMMANDS
    DUMMYQUERY(), XPATH(true), XPATHMV(false), XQUERY(true), XQUENV(),
    FIND(true), XMARK(), CD(true), FS(true),
    // UPDATE COMMANDS
    DUMMYUPDATE(), COPY(true), DELETE(true), INSERT(true), UPDATE(true),
    // GENERAL COMMANDS
    DUMMYGENERAL(), SET(true), HELP(true), PING(), PROMPT(), EXIT(true), QUIT(),
    // SERVER COMMANDS
    GETRESULT(), GETINFO(), STOP();
    
    /** Flag for official (public) commands. */
    boolean official;

    /** Default Constructor. */
    private COMMANDS() {
      this(false);
    }

    /**
     * Constructor, initializing the constants.
     * @param o official command
     */
    private COMMANDS(final boolean o) {
      official = o;
    }

    /**
     * Returns a help string.
     * @param detail show details
     * @param all show all commands (also hidden ones)
     * @return string
     */
    public String help(final boolean detail, final boolean all) {
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
  
  /** Create Commands. */
  enum CREATE { DATABASE, DB, MAB2, MAB, FS, XML, INDEX }
  
  /** Index Types. */
  enum INDEX { TEXT, ATTRIBUTE, FULLTEXT }
  
  /** Info commands. */
  enum INFO { NULL, DATABASE, DB, INDEX, TABLE }
  
  /** Drop commands. */
  enum DROP { DATABASE, DB, INDEX }
  
  /** Filesystem commands. */
  enum FS { CAT, CD, CP, DU, LOCATE, LS, MKDIR, PWD, RM, TOUCH }
  
  /** Insert commands. */
  enum UPDATE { FRAGMENT, ELEMENT, TEXT, ATTRIBUTE, COMMENT, PI }
  
  /** Set commands. */
  enum SET {
    INFO(INFOINFO), DEBUG(INFODEBUG), SERIALIZE(INFOSERIALIZE), 
    XMLOUTPUT(INFOXMLOUTPUT), MAINMEM(INFOMM), CHOP(INFOCHOP),
    ENTITY(INFOENTITIES), TEXTINDEX(INFOTXTINDEX), ATTRINDEX(INFOATVINDEX),
    FTINDEX(INFOFTINDEX);
    
    /** Description. */
    public final String desc;
    
    /**
     * Constructor.
     * @param d descriptions
     */
    SET(final String d) {
      desc = d;
    }
  }
}
