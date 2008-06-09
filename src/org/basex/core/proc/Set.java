package org.basex.core.proc;

import static org.basex.Text.*;

import java.lang.reflect.Field;

import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.io.IO;
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
      Prop.mainmem = toggle(Prop.mainmem, INFOMM, ext);
    } else if(option.equals(RUNS)) {
      Prop.runs = Math.max(1, Token.toInt(ext));
      info(INFORUNS + Prop.runs);
    } else if(option.equals(SERIALIZE)) {
      Prop.serialize = toggle(Prop.serialize, INFOSERIALIZE, ext);
    } else if(option.equals(INFO)) {
      Prop.allInfo = ext.equalsIgnoreCase(ALL);
      if(Prop.allInfo) info(INFOINFO + ": " + INFOON + " (" + INFOALL + ")");
      Prop.info = Prop.allInfo ? true : toggle(Prop.info, INFOINFO, ext);
    } else if(option.equals(XMLOUTPUT)) {
      Prop.xmloutput = toggle(Prop.xmloutput, INFOXMLOUTPUT, ext);
    } else if(option.equals(DBPATH)) {
      if(!new IO(ext).exists()) return error(INFOPATHERR + ext);
      Prop.dbpath = ext;
      info(INFONEWPATH + ext);
      // the following options are kinda hidden
    } else {
      try {
        final Field f = Prop.class.getField(option);
        final Object o = f.get(null);
        if(o instanceof Boolean) {
          f.setBoolean(null, toggle(((Boolean) o).booleanValue(), option, ext));
        } else if(o instanceof String) {
          f.set(null, ((String) o).toString());
          info(option + ": " + ext);
        } else if(o instanceof Integer) {
          f.setInt(null, ((Integer) o).intValue());
          info(option + ": " + ext);
        } else {
          throw new Exception();
        }
      } catch(final Exception ex) {
        throw new IllegalArgumentException();
      }
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
    info("%: %", m, BaseX.flag(val));
    return val;
  }
}
