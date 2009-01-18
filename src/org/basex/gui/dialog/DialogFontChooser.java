package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.EmptyBorder;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXListChooser;
import org.basex.gui.layout.TableLayout;

/**
 * Dialog window for changing the used fonts.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DialogFontChooser extends Dialog {
  /** Font name chooser. */
  private BaseXListChooser font;
  /** Font name chooser. */
  private BaseXListChooser font2;
  /** Font type chooser. */
  private BaseXListChooser type;
  /** Font size chooser. */
  private BaseXListChooser size;
  /** Anti-Aliasing mode. */
  private BaseXCheckBox aalias;
  
  /**
   * Default Constructor.
   * @param main reference to the main window
   */
  public DialogFontChooser(final GUI main) {
    super(main, FONTTITLE, false);
    
    final String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().
      getAvailableFontFamilyNames();

    final BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(1, 4, 6, 2));
    
    font = new BaseXListChooser(this, fonts, HELPFONT);
    font.setSize(150, 112);
    p.add(font);
    font2 = new BaseXListChooser(this, fonts, HELPFONT);
    font2.setSize(150, 112);
    p.add(font2);
    type = new BaseXListChooser(this, FONTTYPES, HELPFONT);
    type.setSize(80, 112);
    p.add(type);
    size = new BaseXListChooser(this, FTSZ, HELPFONT);
    size.setSize(50, 112);
    p.add(size);

    font.setValue(GUIProp.font);
    font2.setValue(GUIProp.monofont);
    type.setValue(FONTTYPES[GUIProp.fonttype]);
    size.setValue(Integer.toString(GUIProp.fontsize));

    set(p, BorderLayout.CENTER);
    
    aalias = new BaseXCheckBox(FAALIAS, HELPFALIAS, GUIProp.fontalias, this);
    aalias.setBorder(new EmptyBorder(5, 4, 0, 0));
    aalias.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        action(null);
      }
    });
    set(aalias, BorderLayout.SOUTH);

    finish(GUIProp.fontsloc);
    font.focus();
  }

  @Override
  public void action(final String cmd) {
    GUIProp.font = font.getValue();
    GUIProp.monofont = font2.getValue();
    GUIProp.fonttype = type.getIndex();
    GUIProp.fontsize = size.getNum();
    GUIProp.fontalias = aalias.isSelected();
    GUIConstants.initFonts();
    gui.notify.layout();
  }

  @Override
  public void close() {
    close(GUIProp.fontsloc);
    GUIProp.write();
  }

  @Override
  public void cancel() {
    close();
  }
}
