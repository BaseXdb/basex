package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import javax.swing.JFrame;

import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXListChooser;
import org.basex.gui.layout.BaseXSlider;
import org.basex.gui.layout.TableLayout;
import org.basex.gui.view.View;

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
  /** Singleton instance. */
  private static Dialog instance;

  /**
   * Returns singleton instance.
   * @param parent parent frame
   * @return class instance
   */
  public static Dialog get(final JFrame parent) {
    if(instance == null) instance = new DialogMapLayout(parent);
    return instance;
  }
  
  /**
   * Default constructor.
   * @param parent parent frame
   */
  private DialogMapLayout(final JFrame parent) {
    super(parent, MAPLAYOUTTITLE, false);
    
    final BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(6, 1, 0, 5));

    // create list
    choice = new BaseXListChooser(this, MAPLAYOUTCHOICE, HELPMAPLAYOUT);
    choice.setSize(200, 110);
    choice.setIndex(GUIProp.maplayout);
    p.add(choice);

    // create checkbox
    simple = new BaseXCheckBox(MAPSIMPLE, HELPMAPSIMPLE,
        GUIProp.mapsimple, 0, this);
    p.add(simple);
    atts = new BaseXCheckBox(MAPATTS, HELPMAPATTS,
        GUIProp.mapatts, this);
    // [JH] doesn't take data change in one session into affect
    if(GUI.context.data().fs != null) {
      atts.setEnabled(false);
    }
    p.add(atts);

    // create slider
    propLabel = new BaseXLabel(MAPPROP);
    p.add(propLabel);
    prop = new BaseXSlider(-234, 248, GUIProp.mapprop, HELPMAPALIGN, this);
    BaseXLayout.setWidth(prop, p.getPreferredSize().width);
    p.add(prop);
    set(p, BorderLayout.CENTER);

    finish(parent, GUIProp.maplayoutloc);
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
    View.notifyLayout();
  }

  @Override
  public void close() {
    close(GUIProp.maplayoutloc);
  }

  @Override
  public void cancel() {
    close();
  }
}
