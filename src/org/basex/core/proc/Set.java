package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.File;
import org.basex.core.Prop;
import org.basex.util.Token;

/**
 * Evaluates the 'set' command. Sets internal processing options.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Set extends Proc {
  /** Set option. */
  public static final String CHOP = "chop";
  /** Set option. */
  public static final String DEBUG = "debug";
  /** Set option. */
  public static final String ENTITY = "entity";
  /** Set option. */
  public static final String FTINDEX = "ftindex";
  /** Set option. */
  public static final String TXTINDEX = "textindex";
  /** Set option. */
  public static final String ATTRINDEX = "attrindex";
  /** Set option. */
  public static final String RUNS = "runs";
  /** Set option. */
  public static final String MAINMEM = "mainmem";
  /** Set option. */
  public static final String SERIALIZE = "serialize";
  /** Set option. */
  public static final String INFO = "info";
  /** Set option. */
  public static final String XMLOUTPUT = "xmloutput";
  /** Set option. */
  public static final String DBPATH = "dbpath";
  /** All flag. */
  public static final String ALL = "ALL";


  @Override
  protected boolean exec() {
    final String option = cmd.arg(0).toLowerCase();
    final String ext = cmd.arg(1);

    if(option.equals(CHOP)) {
      Prop.chop = toggle(Prop.chop, INFOCHOP, ext);
    } else if(option.equals(DEBUG)) {
      Prop.debug = toggle(Prop.debug, INFODEBUG, ext);
    } else if(option.equals(ENTITY)) {
      Prop.entity = toggle(Prop.entity, INFOENTITIES, ext);
    } else if(option.equals(FTINDEX)) {
      Prop.ftindex = toggle(Prop.ftindex, INFOFTINDEX, ext);
    } else if(option.equals(TXTINDEX)) {
      Prop.textindex = toggle(Prop.textindex, INFOTXTINDEX, ext);
    } else if(option.equals(ATTRINDEX)) {
      Prop.attrindex = toggle(Prop.attrindex, INFOATVINDEX, ext);
    } else if(option.equals(MAINMEM)) {
      Prop.mainmem = toggle(Prop.mainmem, INFOMAINMEM, ext);
    } else if(option.equals(RUNS)) {
      Prop.runs = Math.max(1, Token.toInt(ext));
      info(INFORUNS + ": " + Prop.runs);
    } else if(option.equals(SERIALIZE)) {
      Prop.serialize = toggle(Prop.serialize, INFOSERIALIZE, ext);
    } else if(option.equals(INFO)) {
      Prop.allInfo = ext.equalsIgnoreCase(ALL);
      if(Prop.allInfo) info(INFOINFO + INFOON + INFOALL);
      Prop.info = Prop.allInfo ? true : toggle(Prop.info, INFOINFO, ext);
    } else if(option.equals(XMLOUTPUT)) {
      Prop.xmloutput = toggle(Prop.xmloutput, INFOXMLOUTPUT, ext);
    } else if(option.equals(DBPATH)) {
      if(!new File(ext).exists()) return error(INFOPATHERR + ext);
      Prop.dbpath = ext;
      info(INFONEWPATH + ext);
      // the following options are kinda hidden
    } else if(option.equals("fsstat")) {
      Prop.fsstat = toggle(Prop.fsstat, option + " ", ext);
    } else if(option.equals("fscont")) {
      Prop.fscont = toggle(Prop.fscont, option + " ", ext);
    } else if(option.equals("fsmeta")) {
      Prop.fsmeta = toggle(Prop.fsmeta, option + " ", ext);
    } else if(option.equals("fstextmax")) {
      Prop.fstextmax = Math.max(1, Token.toInt(ext));
      info(option + ": " + Prop.fstextmax);
    } else if(option.equals("xqerrcode")) {
      Prop.xqerrcode = toggle(Prop.xqerrcode, option + " ", ext);
    } else if(option.equals("onthefly")) {
      Prop.onthefly = toggle(Prop.onthefly, option + " ", ext);
    } else if(option.equals("intparse")) {
      Prop.intparse = toggle(Prop.intparse, option + " ", ext);
    } else if(option.equals("language")) {
      Prop.language = ext;
      info(option + ": " + ext);
    } else {
      throw new IllegalArgumentException();
    }
    return true;
  }
  
  /**
   * Toggles the specified flag and returns the result.
   * @param f flag to be toggled
   * @param m info message
   * @param e extended value
   * @return result of toggling
   */
  private boolean toggle(final boolean f, final String m, final String e) {
    final boolean val = e.length() == 0 ? !f :
      e.equalsIgnoreCase(ON) || !e.equalsIgnoreCase(OFF);
    info(flag(m, val));
    return val;
  }

  /**
   * Returns an info message for the specified flag.
   * @param feature name of feature
   * @param flag current flag status
   * @return ON/OFF message
   */
  protected static String flag(final String feature, final boolean flag) {
    return feature + (flag ? INFOON : INFOOFF);
  }
}
