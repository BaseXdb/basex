package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXListChooser;
import org.basex.gui.layout.BaseXSlider;
import org.basex.gui.layout.TableLayout;

/**
 * Dialog window for specifying the TreeMap layout.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DialogMapLayout extends Dialog {
  /** Map layouts. */
  private BaseXListChooser choice;  
  /** Layout slider. */
  private BaseXSlider prop;
  /** Simple layout. */
  private BaseXCheckBox simple;
  /** Show attributes. */
  private BaseXCheckBox atts;
  /** Temporary reference to the old map layout. */
  private final BaseXLabel propLabel;
  
  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogMapLayout(final GUI main) {
    super(main, MAPLAYOUTTITLE, false);
    
    final BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(6, 1, 0, 5));

    // create list
    choice = new BaseXListChooser(this, MAPLAYOUTCHOICE, HELPMAPLAYOUT);
    choice.setSize(500, 300);
    choice.setIndex(GUIProp.maplayout);
    p.add(choice);

    // create checkbox
    simple = new BaseXCheckBox(MAPSIMPLE, HELPMAPSIMPLE,
        GUIProp.mapsimple, 0, this);
    p.add(simple);
    atts = new BaseXCheckBox(MAPATTS, HELPMAPATTS, GUIProp.mapatts, this);
    if(gui.context.data().fs != null) {
      atts.setEnabled(false);
    }
    p.add(atts);

    // create slider
    propLabel = new BaseXLabel(MAPPROP);
    p.add(propLabel);
    prop = new BaseXSlider(gui, -234, 248, GUIProp.mapprop, HELPMAPALIGN, this);
    BaseXLayout.setWidth(prop, p.getPreferredSize().width);
    p.add(prop);
    set(p, BorderLayout.CENTER);

    finish(GUIProp.maplayoutloc);
    action(null);
  }

  @Override
  public void action(final String cmd) {
    GUIProp.maplayout = choice.getIndex();
    final int prp = prop.value();
    GUIProp.mapprop = prp;
    GUIProp.mapsimple = simple.isSelected();
    GUIProp.mapatts = atts.isSelected();
    propLabel.setText(MAPPROP + " " + (prp > 2 && prp < 7 ? "Centered" :
      prp < 3 ? "Vertical" : "Horizontal"));
    gui.notify.layout();
  }

  @Override
  public void close() {
    close(GUIProp.maplayoutloc);
    GUIProp.write();
  }

  @Override
  public void cancel() {
    close();
  }
}
