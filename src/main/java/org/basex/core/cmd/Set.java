package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.AProp;
import org.basex.core.MainProp;
import org.basex.core.Context;
import org.basex.core.Commands.CmdSet;
import org.basex.core.User;
import org.basex.util.Util;

/**
 * Evaluates the 'set' command and modifies database properties.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Set extends AGet {
  /** Info strings (same order as options defined in {@link CmdSet}). */
  private static final String[] STRINGS = {
    INFOQUERY, INFODEBUG, INFOSERIALIZE, INFOCHOP, INFOTEXTINDEX,
    INFOATTRINDEX, INFOFTINDEX, INFOPATHINDEX, INFODBPATH
  };

  /**
   * Default constructor.
   * @param key property
   * @param value value to set (optional, depending on the property)
   */
  public Set(final Object key, final Object value) {
    super(User.READ, (key instanceof Object[] ?
        ((Object[]) key)[0] : key).toString(),
        value == null ? "" : value.toString());
  }

  @Override
  protected boolean run() {
    String key = args[0].toUpperCase();
    final String val = args[1];

    CmdSet s = null;
    try { s = Enum.valueOf(CmdSet.class, key); } catch(final Exception ex) { }

    try {
      String v = set(key, val, prop);
      if(v == null && !context.client()) {
        // disallow modification of database path if any database is opened
        if(key.equals(MainProp.DBPATH[0]) && context.datas.size() > 0) {
          return error(SETVAL, key, val);
        }
        v = set(key, val, mprop);
      }
      if(v == null) return whichKey();

      final CmdSet[] cs = CmdSet.values();
      for(int c = 0; c < cs.length; ++c) if(cs[c] == s) key = STRINGS[c];
      return info(key + COLS + v);
    } catch(final Exception ex) {
      Util.debug(ex);
      return error(SETVAL, key, val);
    }
  }

  @Override
  public boolean updating(final Context ctx) {
    // command may set options that influence other commands
    return true;
  }

  /**
   * Sets the specified value.
   * @param key key
   * @param val value
   * @param prop property
   * @return final value
   */
  public static String set(final String key, final String val,
      final AProp prop) {

    final Object type = prop.get(key);
    if(type == null) return null;

    String v = val;
    if(type instanceof Boolean) {
      final boolean b = val == null || val.isEmpty() ?
          !((Boolean) type) : Util.yes(val);
      prop.set(key, b);
      v = Util.flag(b);
    } else if(type instanceof Integer) {
      prop.set(key, Integer.parseInt(val));
      v = String.valueOf(prop.get(key));
    } else if(type instanceof String) {
      prop.set(key, val);
    } else {
      Util.notexpected();
    }
    return v;
  }
}