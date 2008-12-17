package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXSlider;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.gui.view.View;

/**
 * Dialog window for changing some project's preferences.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DialogPrefs extends Dialog {
  /** Language label. */
  BaseXLabel creds;
  /** Language Combo Box. */
  BaseXCombo lang;
  /** Directory path. */
  private BaseXTextField path;
  /** Focus checkbox. */
  private BaseXCheckBox focus;
  /** Show names checkbox. */
  private BaseXCheckBox names;
  /** Simple file dialog checkbox. */
  private BaseXCheckBox simpfd;
  /** Simple file dialog checkbox. */
  private BaseXCheckBox javalook;
  /** Dot size in plot view. */
  private BaseXSlider dots;

  /**
   * Default Constructor.
   * @param gui reference to main frame
   */
  public DialogPrefs(final GUI gui) {
    super(gui, PREFSTITLE, false);

    // create checkboxes
    final BaseXBack pp = new BaseXBack();
    pp.setLayout(new TableLayout(11, 1, 0, 0));
    pp.add(new BaseXLabel(DATABASEPATH, true, true));

    BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(1, 2, 6, 0));
    
    path = new BaseXTextField(Prop.dbpath, HELPDBPATH, this);
    path.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) { action(null); }
    });
    
    final BaseXButton button = new BaseXButton(BUTTONBROWSE, HELPBROWSE, this);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        chooseDir(gui);
      }
    });
    
    BaseXLayout.setWidth(path, 300);
    BaseXLayout.setHeight(path, button.getPreferredSize().height);
    p.add(path);
    p.add(button);
    pp.add(p);

    BaseXLabel label = new BaseXLabel(PREFINTER, true, true);
    label.setBorder(10, 0, 8, 0);
    pp.add(label);

    // checkbox for realtime mouse focus
    javalook = new BaseXCheckBox(PREFLF, HELPLF, GUIProp.javalook, this);
    pp.add(javalook);

    // checkbox for realtime mouse focus
    focus = new BaseXCheckBox(PREFFOCUS, HELPFOCUS, GUIProp.mousefocus, this);
    pp.add(focus);

    // checkbox for simple file dialog
    simpfd = new BaseXCheckBox(SIMPLEFD, HELPSIMPLEFD, GUIProp.simplefd, this);
    pp.add(simpfd);

    // enable only if current document contains name attributes
    names = new BaseXCheckBox(PREFNAMES, HELPNAMES, GUIProp.shownames, this);
    final Data data = GUI.context.data();
    names.setEnabled(data != null && data.fs == null && data.nameID != 0);
    pp.add(names);

    // checkbox for realtime mouse focus
    p = new BaseXBack();
    p.setLayout(new TableLayout(1, 2, 6, 0));
    dots = new BaseXSlider(-10, 10, GUIProp.plotdots, null, this);
    BaseXLayout.setWidth(dots, 150);
    p.add(new BaseXLabel(PREFDOTS));
    p.add(dots);
    pp.add(p);

    // checkbox for simple file dialog
    label = new BaseXLabel(PREFLANG, true, true);
    label.setBorder(16, 0, 8, 0);
    pp.add(label);

    p = new BaseXBack();
    p.setLayout(new TableLayout(1, 2, 12, 0));

    lang = new BaseXCombo(Prop.LANGUAGES, HELPLANG, false, this);
    lang.setSelectedItem(Prop.language);
    lang.addItemListener(new ItemListener() {
      public void itemStateChanged(final ItemEvent ie) {
        creds.setText(credits(lang.getSelectedItem().toString()));
        action(null);
      }
      
    });
    p.add(lang);
    creds = new BaseXLabel(credits(Prop.language));
    p.add(creds);
    
    pp.add(p);

    set(pp, BorderLayout.CENTER);
    finish(gui);
  }
  
  /**
   * Opens the directory chooser and sets the new path.
   * @param parent parent reference
   */
  void chooseDir(final JFrame parent) {
    final BaseXFileChooser fc = new BaseXFileChooser(DIALOGFC, path.getText(),
        parent);
    if(fc.select(BaseXFileChooser.Mode.DIR)) path.setText(fc.getDir());
  }
  
  /**
   * Returns the translation credits for the specified language.
   * @param lng language
   * @return credits
   */
  protected String credits(final String lng) {
    for(int i = 0; i < Prop.LANGUAGES.length; i++) {
      if(lng.equals(Prop.LANGUAGES[i]))
        return "Translated by " + Prop.LANGCREDS[i];
    }
    return "";
  }

  @Override
  public void action(final String cmd) {
    Prop.dbpath = path.getText();
    Prop.language = lang.getSelectedItem().toString();
    GUIProp.mousefocus = focus.isSelected();
    GUIProp.shownames = names.isSelected();
    GUIProp.simplefd = simpfd.isSelected();
    GUIProp.javalook = javalook.isSelected();
    GUIProp.plotdots = dots.value();
    View.notifyLayout();
  }
}
