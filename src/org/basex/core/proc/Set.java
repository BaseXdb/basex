package org.basex.core.proc;

import static org.basex.Text.*;
import static org.basex.core.Commands.*;
import org.basex.BaseX;
import org.basex.Text;
import org.basex.core.Process;
import org.basex.core.Prop;

/**
 * Evaluates the 'set' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Set extends Process {
  /**
   * Constructor.
   * @param option option
   * @param val optional value
   */
  public Set(final Object option, final String... val) {
    super(STANDARD, (option instanceof Object[] ?
        ((Object[]) option)[0] : option).toString(),
        val.length == 0 ? null : val[0]);
  }

  @Override
  protected boolean exec() {
    final String key = args[0].toUpperCase();
    CmdSet s = null;
    try {
      s = Enum.valueOf(CmdSet.class, key);
    } catch(final Exception ex) { }

    try {
      final Object type = prop.object(key);
      String val = args[1];

      if(type instanceof Boolean) {
        final boolean all = ALL.equals(val);
        final boolean b = val == null ? !((Boolean) type).booleanValue() :
          val.equalsIgnoreCase(ON) || val.equalsIgnoreCase(TRUE);
        prop.set(key, b);
        val = BaseX.flag(b);
        if(s == CmdSet.INFO) {
          if(all) {
            prop.set(Prop.ALLINFO, true);
            prop.set(Prop.INFO, true);
            val = INFOON + " (" + INFOALL + ")";
          } else {
            prop.set(Prop.ALLINFO, false);
          }
        }
      } else if(type instanceof Integer) {
        prop.set(key, Integer.parseInt(val));
      } else if(type instanceof String) {
        prop.set(key, val);
      } else {
        BaseX.notexpected();
      }
      return info((s == null ? key :
        Text.class.getField("INFO" + s).get(null).toString()) + ": " + val);
    } catch(final Exception ex) {
      BaseX.debug(ex);
      return error("Could not assign \"%\"", key);
    }
  }
}
