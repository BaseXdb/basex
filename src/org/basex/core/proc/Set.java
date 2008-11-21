package org.basex.core.proc;

import static org.basex.Text.*;
import static org.basex.core.Commands.*;
import java.lang.reflect.Field;
import org.basex.BaseX;
import org.basex.Text;
import org.basex.core.Process;
import org.basex.core.Prop;

/**
 * Evaluates the 'set' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Set extends Process {
  /**
   * Constructor.
   * @param option option
   * @param val optional value
   */
  public Set(final Object option, final String val) {
    super(STANDARD, option.toString(), val);
  }
  
  @Override
  protected boolean exec() {
    final String option = args[0];

    CmdSet s = null;
    try {
      s = Enum.valueOf(CmdSet.class, option);
    } catch(final Exception ex) { }
    
    try {
      final Field f = Prop.class.getField(option.toLowerCase());
      final Object key = f.get(null);
      String val = args[1];
      
      if(key instanceof Boolean) {
        if(s == CmdSet.INFO && ALL.equals(val)) {
          Prop.allInfo = true;
          Prop.info = true;
          val = INFOON + " (" + INFOALL + ")";
        } else {
          final boolean b = val == null ? !((Boolean) key).booleanValue() :
            val.equalsIgnoreCase(ON) || !val.equalsIgnoreCase(OFF);
          f.setBoolean(null, b);
          val = BaseX.flag(b);
        }
      } else if(key instanceof String) {
        f.set(null, val);
      } else if(key instanceof Integer) {
        f.setInt(null, Integer.parseInt(val));
      } else {
        BaseX.notexpected();
      }
      return info((s == null ? option :
        Text.class.getField("INFO" + s).get(null).toString()) + ": " + val);
    } catch(final Exception ex) {
      BaseX.debug(ex);
      return error("Could not assign \"%\"", option);
    }
  }
}
