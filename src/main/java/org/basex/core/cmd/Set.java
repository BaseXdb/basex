package org.basex.core.cmd;

import static org.basex.core.Commands.*;
import static org.basex.core.Text.*;

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
    INFOQUERY, INFODEBUG, INFOSERIALIZE, INFOCHOP, INFOENTITY, INFOTEXTINDEX,
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
        value == null ? null : value.toString());
  }

  @Override
  protected boolean run() {
    String key = args[0].toUpperCase();
    String val = args[1];

    CmdSet s = null;
    try { s = Enum.valueOf(CmdSet.class, key); } catch(final Exception ex) { }

    try {
      final Object type = prop.get(key);
      if(type == null) return whichKey();

      if(type instanceof Boolean) {
        final boolean b = val == null ? !((Boolean) type).booleanValue() :
          val.equalsIgnoreCase(ON) || val.equalsIgnoreCase(TRUE);
        prop.set(key, b);
        val = AInfo.flag(b);
      } else if(type instanceof Integer) {
        if(val == null) val = "0";
        prop.set(key, Integer.parseInt(val));
      } else if(type instanceof String) {
        if(val == null) val = "";
        prop.set(key, val);
      } else {
        Util.notexpected();
      }
      final CmdSet[] cs = CmdSet.values();
      for(int c = 0; c < cs.length; ++c) if(cs[c] == s) key = STRINGS[c];
      return info(key + ": " + val);
    } catch(final Exception ex) {
      Util.debug(ex);
      return error(SETVAL, key, val);
    }
  }
}