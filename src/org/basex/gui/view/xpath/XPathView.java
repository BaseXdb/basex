package org.basex.gui.view.xpath;

import static org.basex.gui.GUIConstants.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JComboBox;
import javax.swing.plaf.basic.BasicComboPopup;
import org.basex.core.Commands;
import org.basex.data.Data;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants.FILL;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.view.View;
import org.basex.util.StringList;
import org.basex.util.Token;

/**
 * This class offers a text field for XPath input.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author ...
 */
public final class XPathView extends View {
  /** Input completions. */
  private static final String[] COMP = { "*", "comment()", "node()",
    "processing-instruction()", "text()", "ancestor-or-self::", "ancestor::",
      "attribute::", "child::", "descendant-or-self::", "descendant::",
      "following-sibling::", "following::", "parent::", "preceding-sibling::",
      "preceding::", "self::" };
  
  /** Input field for XPath queries. */
  protected final BaseXTextField input;
  /** BasicComboPopup Menu. */
  protected ComboPopup pop;
  /** JComboBox. */
  protected final JComboBox box;
  /** String for temporary input. */
  protected String tempIn;

  /** Header string. */
  private final BaseXLabel header;
  /** Button box. */
  private final BaseXBack back;

  /**
   * Default Constructor.
   */
  public XPathView() {
    super(null);

    back = new BaseXBack(FILL.NONE);
    back.setLayout(new BorderLayout());
    header = new BaseXLabel(GUIConstants.XPATHVIEW, 10);
    back.add(header, BorderLayout.NORTH);

    box = new JComboBox();
    box.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        if(e.getModifiers() == 16) completeInput();
      }
    });
    pop = new ComboPopup(box);

    input = new BaseXTextField(null);
    input.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        final int count = box.getItemCount();
        if(count == 0) return;
        int p = box.getSelectedIndex();
        
        final int c = e.getKeyCode();
        if(c == KeyEvent.VK_DOWN) {
          if(++p == count) p = 0;
          box.setSelectedIndex(p);
        } else if(c == KeyEvent.VK_UP) {
          if(--p < 0) p = count - 1;
          box.setSelectedIndex(p);
        } else if(c == KeyEvent.VK_ENTER) {
          if(box.getSelectedItem() != null) completeInput();
        }
      }

      @Override
      public void keyReleased(final KeyEvent e) {
        final int c = e.getKeyCode();
        if(c == KeyEvent.VK_DOWN || c == KeyEvent.VK_UP) return;

        if(c == KeyEvent.VK_ESCAPE) {
          pop.setVisible(false);
          return;
        }
        
        final boolean enter = c == KeyEvent.VK_ENTER;
        if(!enter) showPopup();

        if(GUIProp.execrt || enter) {
          GUI.get().execute(Commands.XPATH, input.getText());
        }

        /*
        final XPParser parser = new XPParser(query);
        try {
          parser.parse();
        } catch(final QueryException ex) {
          //System.out.println(ex.getMessage());
        }*/
      }
    });

    setBorder(10, 10, 10, 10);
    setLayout(new BorderLayout(0, 4));
    back.add(input, BorderLayout.CENTER);

    add(back, BorderLayout.NORTH);

    refreshLayout();
  }

  /**
   * Shows the popup menu with correct entries only.
   */
  protected void showPopup() {
    final StringList sl = new StringList();
    
    final String query = input.getText();
    final int slash = Math.max(query.lastIndexOf('/'),
        Math.max(query.lastIndexOf('['), query.lastIndexOf('(')));
    final int axis = query.lastIndexOf("::");
    final boolean test = axis > slash;

    tempIn = "";
    if(test) tempIn = query.substring(0, axis + 2);
    else if(slash != -1) tempIn = query.substring(0, slash + 1);

    if(query.length() != 0) {
      String suf = input.getText().substring(tempIn.length());
      final String[] all = createList();
      if(test) {
        final boolean attest = tempIn.endsWith("attribute::");
        for(final String a : all) {
          if(a.endsWith("::")) continue;
          if(attest ^ a.startsWith("@")) continue;
          final String at = a.substring(attest ? 1 : 0);
          if(at.startsWith(suf) && !at.equals(suf)) sl.add(at);
        }
      } else {
        for(final String a : all) {
          if(a.startsWith(suf) && !a.equals(suf)) sl.add(a);
        }
      }
    }
    createCombo(sl);
  }

  /**
   * Creates a combo box list.
   * @return list
   */
  private String[] createList() {
    final StringList sl = new StringList();
    final Data data = GUI.context.data();

    for(int i = 1; i <= data.tags.size(); i++) {
      if(data.tags.counter(i) == 0) continue;
      sl.add(Token.string(data.tags.key(i)));
    }
    for(int i = 1; i <= data.atts.size(); i++) {
      if(data.atts.counter(i) == 0) continue;
      sl.add("@" + Token.string(data.atts.key(i)));
    }
    sl.sort();

    for(final String c : COMP) sl.add(c);
    return sl.finish();
  }

  /**
   * Creates and shows the combo box.
   * @param sl strings to be added
   */
  public void createCombo(final StringList sl) {
    if(sl.size == 0) {
      box.setSelectedItem(null);
      pop.setVisible(false);
      return;
    }
    
    if(comboChanged(sl)) {
      box.setSelectedItem(null);
      box.removeAllItems();
      for(int i = 0; i < sl.size; i++) box.addItem(sl.list[i]);
      pop = new ComboPopup(box);
    }
    final int width = Math.min(getWidth(),
        input.getFontMetrics(input.getFont()).stringWidth(tempIn));
    pop.show(input, width, input.getHeight());
  }

  /**
   * Tests if the combo box entries have changed.
   * @param sl strings to be compared
   * @return result of check
   */
  public boolean comboChanged(final StringList sl) {
    if(sl.size != box.getItemCount()) return true;
    for(int i = 0; i < sl.size; i++) {
      if(!sl.list[i].equals(box.getItemAt(i))) return true;
    }
    return false;
  }

  /**
   * Completes the input with the current combo box choice.
   */
  public void completeInput() {
    final String sel = box.getSelectedItem().toString();
    input.setText(tempIn + sel);
    showPopup();
  }

  @Override
  protected void refreshInit() { }

  @Override
  protected void refreshFocus() { }

  @Override
  protected void refreshContext(final boolean more, final boolean quick) { }

  @Override
  protected void refreshLayout() {
    header.setFont(GUIConstants.lfont);
    header.setForeground(COLORS[16]);
  }

  @Override
  protected void refreshMark() { }

  @Override
  protected void refreshUpdate() { }

  /** Combo Popupmenu class, overriding the default constructor. */
  class ComboPopup extends BasicComboPopup {
    /**
     * Constructor.
     * @param combo combo box reference
     */
    ComboPopup(final JComboBox combo) {
      super(combo);
      final int h = combo.getMaximumRowCount();
      setPreferredSize(new Dimension(getPreferredSize().width,
          getPopupHeightForRowCount(h) + 2));
    }
  }
}
