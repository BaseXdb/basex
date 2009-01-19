package org.basex.gui.dialog;

import static org.basex.Text.*;

//import java.awt.BorderLayout;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXCheckBox;
//import org.basex.gui.layout.BaseXLabel;
//import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.TableLayout;

/**
 * Dialog window for additional TreeMap information.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DialogMapInfo extends Dialog {
  /** show information. */
  private BaseXCheckBox info;
  
  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogMapInfo(final GUI main) {
    super(main, MAPINFOTITLE, false);
    
    final BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(6, 1, 0, 5));

    // create checkbox
    info = new BaseXCheckBox(MAPINFO, HELPMAPINFO,
        GUIProp.mapinfo, 0, this);
    p.add(info);
    
    finish(GUIProp.mapinfoloc);
    action(null);
  }

  @Override
  public void action(final String cmd) {
    gui.notify.layout();
  }

  @Override
  public void close() {
    close(GUIProp.mapinfoloc);
    GUIProp.write();
  }

  @Override
  public void cancel() {
    close();
  }
}
