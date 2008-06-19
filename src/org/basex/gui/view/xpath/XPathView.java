package org.basex.gui.view.xpath;

import static org.basex.gui.GUIConstants.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.FontMetrics;
import java.awt.Dimension;
import java.util.Arrays;
import javax.swing.JComboBox;
import javax.swing.plaf.basic.BasicComboPopup;
import org.basex.core.Commands;
import org.basex.core.Context;
import org.basex.core.proc.Optimize;
import org.basex.data.Data;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIConstants.FILL;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.view.View;
import org.basex.util.StringList;
import org.basex.util.Token;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPParser;

/**
 * This class offers a real tree view.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author ...
 */
public final class XPathView extends View {
  /** Database Context. */
  public static Context context = new Context();
  /** Input field for XPath queries. */
  final BaseXTextField input;
  /** Header string. */
  final BaseXLabel header;
  /** Button box. */
  final BaseXBack back;
  /** BasicComboPopup Menu. */
  public BasicComboPopup pop;
  /** StringList with all entries. */
  public StringList all = new StringList();
  /** JComboBox. */
  public JComboBox box;
  /** String for temporary input. */
  public String tempIn;
  /** String for temporary input. */
  public String temp;
  /** Int value to count slashes. */
  public int slashC = 0;
  /** Boolean value if BasicComboPopup is initialized. */
  public boolean popInit = false;
  /** Boolean value for atts only. */
  public boolean atts = false;
  /** Boolean value for node-tests only. */
  public boolean nodes = false;

  /**
   * Default Constructor.
   */
  public XPathView() {
    super(null);

    back = new BaseXBack(FILL.NONE);
    back.setLayout(new BorderLayout());
    header = new BaseXLabel(GUIConstants.XPATHVIEW, 10);
    back.add(header, BorderLayout.NORTH);
    input = new BaseXTextField(null);

    input.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        int c = e.getKeyCode();
          if(c == KeyEvent.VK_SLASH) {
            slashC++;
            if(slashC <= 2) {
            showPopAll();
            } else {
              pop.hide();
            }
          } else if(c == KeyEvent.VK_DELETE || c == KeyEvent.VK_BACK_SPACE) {
            if(input.getText().length() == 0) {
              slashC = 0;
              tempIn = "";
              if(popInit) {
              pop.hide();
              }
            } else if(input.getText().endsWith("/")) {
              if ('/' ==
                input.getText().charAt(input.getText().length() - 1)) {
                slashC = 1;
                showPopAll();
            } else if ('/' ==
              input.getText().charAt(input.getText().length() - 2)) {
              slashC = 2;
              showPopAll();
              }
            } else {
              tempIn = input.getText();
              slashC = 0;
              pop.hide();
            }
          } else if(c == KeyEvent.VK_DOWN) {
            if(box.getSelectedItem() != null &&
                box.getSelectedIndex() < box.getItemCount() - 1) {
              int tmp = box.getSelectedIndex();
              box.setSelectedIndex(tmp + 1);
            } else {
              box.setSelectedIndex(0);
            } 
          } else if(c == KeyEvent.VK_UP) {
            if(box.getSelectedItem() != null) {
              int tmp = box.getSelectedIndex();
              box.setSelectedIndex(tmp - 1);
            } else {
              box.setSelectedIndex(box.getItemCount() - 1);
            }
          } else if(c == KeyEvent.VK_ENTER) {
            if(box.getSelectedItem() != null) {
              input.setText(tempIn + box.getSelectedItem().toString());
              if(box.getSelectedItem().toString().endsWith("::")) {
                if(box.getSelectedItem().toString().equals("attribute::")) {
                  tempIn = input.getText();
                  atts = true;
                  showSpecPop();
                } else {
                  tempIn = input.getText();
                  nodes = true;
                  showSpecPop();
                }
                }
              slashC = 0;
              if(box.getSelectedItem() != null) {
                pop.hide();
                }
            }
          } else if(c == KeyEvent.VK_SHIFT) {
            return;
          } else {
            if(popInit) {
              if(tempIn.length() == 0) {
                pop.hide();
              } else {
                slashC = 0;
                showSpecPop();
              }
            }
        }
        if(c == KeyEvent.VK_ESCAPE) return;
        final String query = input.getText();
        final XPParser parser = new XPParser(query);
        try {
          parser.parse();
          GUI.get().execute(Commands.XPATH, query);
        } catch(final QueryException ex) {
          //System.out.println(ex.getMessage());
        }
      }
    });

    setBorder(10, 10, 10, 10);
    setLayout(new BorderLayout(0, 4));
    back.add(input, BorderLayout.CENTER);

    add(back, BorderLayout.NORTH);

    refreshLayout();
  }

  /**
   * Shows the BasicComboPopup with all Entries.
   */
  public void showPopAll() {
    tempIn = input.getText();
    box = new JComboBox(all.finish());
    box.setSelectedItem(null);
    popInit = true;
    box.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        if(e.getModifiers() == 16) {
          input.setText(tempIn + box.getSelectedItem());
          if(box.getSelectedItem().toString().endsWith("::")) {
          if(box.getSelectedItem().toString().equals("attribute::")) {
            tempIn = input.getText();
            atts = true;
            showSpecPop();
          } else {
            tempIn = input.getText();
            nodes = true;
            showSpecPop();
          }
          }
          if(box.getSelectedItem() != null) {
          pop.hide();
          }
        }
      }
    });
      pop = new BasicComboPopup(box);
      FontMetrics fm = input.getFontMetrics(input.getFont());
      int width = fm.stringWidth(input.getText());
      if(width >= back.getWidth()) {
        width = back.getWidth();
      }
      pop.show(input, width, input.getHeight());
  }

  /**
   * Shows the special BasicComboPopup with correct entries only.
   */
  public void showSpecPop() {
    pop.hide();
    box.removeAllItems();
    if(tempIn.endsWith("/") || (!atts && !nodes)) {
    temp = input.getText().substring(tempIn.length());
    for(int i = 0; i < all.finish().length; i++) {
      if(all.finish()[i].startsWith(temp)) {
        box.addItem(all.finish()[i]);
      }
    }
    } else if(atts) {
      for(int i = 0; i < all.finish().length; i++) {
        if(all.finish()[i].startsWith("@")) {
          box.addItem(all.finish()[i]);
        }
        atts = false;
      }
    } else if(nodes) {
      for(int i = 0; i < all.finish().length; i++) {
        if(!all.finish()[i].endsWith("::")) {
          box.addItem(all.finish()[i]);
        }
        nodes = false;
      }
    }
    if(box.getItemCount() != 0) {
      box.setSelectedItem(null);
      FontMetrics fm = input.getFontMetrics(input.getFont());
      int width = fm.stringWidth(input.getText());
      if(width >= back.getWidth()) {
        width = back.getWidth();
      }
      pop = new BasicComboPopup(box);
      if(box.getItemCount() < 10) {
      pop.setPreferredSize(new Dimension(pop.getPreferredSize().width,
          box.getItemCount() * 20));
      }
      pop.show(input, width, input.getHeight());
    }
  }

  @Override
  protected void refreshContext(final boolean more, final boolean quick) {
  // TODO Auto-generated method stub

  }

  @Override
  protected void refreshFocus() {
    repaint();
    // TODO Auto-generated method stub

  }

  @Override
  protected void refreshInit() {
    all = new StringList();
    if(GUI.context.data() != null) {
      String[] test = keys(GUI.context.data());
      for(int i = 1; i < test.length; i++) {
        all.add(test[i]);
      }
      String[] cmdList = { "ancestor-or-self::", "ancestor::",
          "attribute::", "child::", "comment()", "descendant-or-self::",
          "following-sibling::", "following::", "namespace::", "node()",
          "parent::", "preceding-sibling::", "preceding::",
          "processing-instruction()", "self::", "text()" };
      for(int j = 0; j < cmdList.length; j++) {
        all.add(cmdList[j]);
      }
    }
  }

  @Override
  protected void refreshLayout() {
    header.setFont(GUIConstants.lfont);
    header.setForeground(COLORS[16]);
  }

  @Override
  protected void refreshMark() {
  // TODO Auto-generated method stub

  }

  @Override
  protected void refreshUpdate() {
  // TODO Auto-generated method stub

  }

  /**
   * Returns a string array with all distinct keys
   * and the keys of the specified set.
   * @param data data reference
   * @return key array
   */
  String[] keys(final Data data) {
    if(!data.tags.stats()) new Optimize().stats(data);

    final StringList sl = new StringList();
    sl.add("");
    for(int i = 1; i <= data.tags.size(); i++) {
      if(data.tags.counter(i) == 0) continue;
      sl.add(Token.string(data.tags.key(i)));
    }
    for(int i = 1; i <= data.atts.size(); i++) {
      if(data.atts.counter(i) == 0) continue;
      sl.add("@" + Token.string(data.atts.key(i)));
    }
    final String[] vals = sl.finish();
    Arrays.sort(vals);
    vals[0] = "(" + (vals.length - 1) + " entries)";
    return vals;
  }
}
