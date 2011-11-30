package org.basex.data;

import static org.basex.build.BuildText.*;
import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.io.IO;
import org.basex.util.Atts;
import org.basex.util.Performance;
import org.basex.util.Util;
import org.basex.util.hash.TokenMap;
import org.basex.util.list.IntList;

/**
 * This class modifies an existing database instance.
 *
 * @author Dave Glick, Data Research and Analysis Corp.
 */
public final class InsertBuilder extends Builder {
  /** The target insert pre */
  private final int ipre;
  /** The target insert parent pre */
  private final int ipar;
  /** The target database */
  private final Data data;
  
  /** The optimal buffer size */
  private final int buf = IO.BLOCKSIZE >> IO.NODEPOWER;
  
  /** Flag for inside document */
  private boolean inDoc;
  /** Cache of document pres */
  private IntList docPres;
  /** Flag for a dummy document */
  private boolean dummy;
  /** Cache of namespace scopes */
  private TokenMap nsScope;
  /** All current pres, stores the pre value of the insert operation, not the absolute pre value */
  private Stack<PreStackItem> preStack;
  /** Cache of new namespace nodes */
  private Set<NSNode> newNodes;
  /** Cache of pres with ns flag */
  private IntList flagPres;
  /** Cache of insertion sizes for delayed set */
  private Stack<int[]> sizeStack; //[0] = pre, [1] = size

  /** The current insert pre */
  private int pre;
  /** The current insert count - named dpre to match Data.insert() */
  private int dpre;
  /** The current insert distance */
  private int dis;
  
  /**
   * Used for internally caching nested pre and tag names
   */
  private class PreStackItem
  {
    /** The source pre */
    public final int dp;
    /** The tag name */
    public final byte[] tag;
    
    /**
     * @param p pre
     * @param t tag name
     */
    public PreStackItem(int p, byte[] t)
    {
      dp = p;
      tag = t;
    }
  }
 
  /**
   * @param tpre the target pre
   * @param tpar the target parent
   * @param d the target database
   * @param parse the source parser
   */
  public InsertBuilder(final int tpre, final int tpar, Data d, Parser parse) {
    super(parse);   // We don't really need the name or prop for this Builder, but have to provide ones anyway
    ipre = tpre;
    ipar = tpar;
    data = d;
  }

  @Override
  public Data build() throws IOException {   
    final Performance perf = Util.debug ? new Performance() : null;
    Util.debug(tit() + DOTS);
    
    //Put the target database in update mode
    data.meta.update();
    
    dpre = 0;
    inDoc = false;
    docPres = new IntList();
    dummy = false;
    nsScope = new TokenMap();
    preStack = new Stack<PreStackItem>();
    newNodes = new HashSet<NSNode>();
    flagPres = new IntList();  
    sizeStack = new Stack<int[]>();    
            
    // Resize the buffer to a reasonable value since we don't know how big the stream is
    data.buffer(buf);
    
    // Find all namespaces in scope to avoid duplicate declarations
    NSNode n = data.ns.current;
    do {
      for(int i = 0; i < n.vals.length; i += 2)
        nsScope.add(data.ns.pref(n.vals[i]), data.ns.uri(n.vals[i + 1]));
      final int pos = n.fnd(ipar);
      if(pos < 0) break;
      n = n.ch[pos];
    } while(n.pre <= ipar && ipar < n.pre + data.size(n.pre, Data.ELEM));
    
    // Store the root namespace so it can be reset later
    NSNode t = data.ns.current;
    
    // find nearest namespace node on the ancestor axis of the insert
    // location. possible candidates for this node are collected and
    // the match with the highest pre value between ancestors and candidates
    // is determined.
    // collect possible candidates for namespace root
    final List<NSNode> cand = new LinkedList<NSNode>();
    NSNode cn = data.ns.root;
    cand.add(cn);
    for(int cI; (cI = cn.fnd(ipar)) > -1;) {
      // add candidate to stack
      cn = cn.ch[cI];
      cand.add(0, cn);
    }

    cn = data.ns.root;
    if(cand.size() > 1) {
      // compare candidates to ancestors of par
      int ancPre = ipar;
      // take first candidate from stack
      NSNode curr = cand.remove(0);
      while(ancPre > -1 && cn == data.ns.root) {
        // this is the new root
        if(curr.pre == ancPre) cn = curr;
        // if the current candidate's pre value is lower than the current
        // ancestor of par or par itself we have to look for a potential
        // match for this candidate. therefore we iterate through ancestors
        // till we find one with a lower than or the same pre value as the
        // current candidate.
        else if(curr.pre < ancPre) {
          while((ancPre = data.parent(ancPre, data.kind(ancPre))) > curr.pre);
          if(curr.pre == ancPre) cn = curr;
        }
        // no potential for infinite loop, cause dummy root always a match,
        // in this case ancPre ends iteration
        if(cand.size() > 0) curr = cand.remove(0);
      }
    }
    data.ns.setNearestRoot(cn, ipar);   
    
    try
    {
      // add document node and parse document
      parser.parse(this);
      
      //We should have closed all the tags we opened
      if(preStack.size() != 0) error(DOCOPEN, parser.detail(), preStack.peek().tag); 
      
      // Insert any remaining buffer
      if(data.bp != 0) data.insert(ipre + dpre - 1 - (dpre - 1) % buf);
    }
    catch(Exception e)
    {
      //If something went wrong, rollback any inserted buffers and rethrow
      data.delete(ipre, dpre - (dpre % buf));    
      data.bp = 0;
      
      //TODO: This appears to work except...
      //Text indexed during the invalid operation will remain in the index
      //Other indexes such as the tag, namespace, and attribute indexes will not be reset and will contain indexed values from the invalid operation
      //Unique identifiers generated during the invalid operation will not be reused by the next valid operation
      
      if(e instanceof IOException)
        throw (IOException)e;
      if(e instanceof RuntimeException)
        throw (RuntimeException)e;
    }
    finally
    {
      // reset buffer to old size
      data.buffer(1);
      
      // Reset the root namespace
      data.ns.setRoot(t);      
    }      
    
    //Increase document count
    data.meta.ndocs += docPres.size();
        
    // Now that all the buffers have been flushed, set sizes for inserted nodes
    while(sizeStack.size() != 0)
    {
      int[] size = sizeStack.pop();
      data.size(size[0], Data.ELEM, size[1]);
    }

    // set ns flags
    for(final int toFlag : flagPres.toArray())
      data.table.write2(toFlag, 1, data.name(toFlag) | 1 << 15);

    // increase size of ancestors
    int p = ipar;
    while(p >= 0) {
      final int k = data.kind(p);
      data.size(p, k, data.size(p, k) + dpre);
      p = data.parent(p, k);
    }
    data.updateDist(ipre +  dpre,  dpre);

    // NSNodes have to be checked for pre value shifts after insert
    data.ns.update(ipre, dpre, true, newNodes);
    
    // Update document index
    final IntList il = data.docindex.docs();
    int i = il.sortedIndexOf(ipre);
    if(i < 0) i = -i - 1;
    il.insert(i, docPres.toArray());
    il.move(dpre, i + docPres.size());
    data.docindex.update();

    // delete old empty root node
    if(dummy) data.delete(0);
      
    Util.gc(perf);
    return data;
  }
  
  /**
   * Updates the current state for each build event
   */
  private void update()
  {        
    // Flush the buffer if we're at capacity
    if(dpre != 0 && dpre % buf == 0)
      data.insert(ipre + dpre - buf);
    
    // Update positions
    pre = ipre + dpre;
    int dpar = preStack.size() > 0 ? preStack.peek().dp : -1;
    dis = dpar >= 0 ? dpre - dpar : ipar >= 0 ? pre - ipar : 0;
  }
  
  @Override
  public void close() throws IOException {
    parser.close();
  }

  @Override
  public void startDoc(byte[] value) throws IOException
  {    
    update();

    // If addDoc is first event, check if data is empty, and if so set dummy = true
    if(data.empty() && dpre == 0)
    {
      dummy = true;
    }
    
    // Cache the document pre
    docPres.add(pre);

    // Add document
    data.doc(pre, 0, value);
    data.ns.open();
    preStack.push(new PreStackItem(dpre, value));
    dpre++;
    inDoc = true;
  }

  @Override
  public void endDoc() throws IOException
  {
    inDoc = false;
    int spre = preStack.pop().dp + ipre;
    data.ns.close(spre);    
    sizeStack.push(new int[]{spre, pre - spre + 1});
  }

  @Override
  public void startElem(byte[] nm, Atts att) throws IOException
  {
    update();
    
    // Add element
    final int as = att.size;
    boolean ne = data.ns.open();
    data.elem(dis, data.tagindex.index(nm, null, false),
        Math.min(IO.MAXATTS, as + 1), 0, data.ns.uri(nm, true), ne);
    preStack.push(new PreStackItem(dpre, nm)); 
    dpre++;    
    
    // Get and store attribute references
    for(int a = 0; a < as; ++a) {
      addAttr(att.key[a], att.val[a], Math.min(IO.MAXATTS, a + 1));
    }
    
    // Make sure this is the only root node for a document
    if(preStack.size() == 1 && inDoc && dis > 0)
    {
      error(MOREROOTS, parser.detail(), nm);
    }
  }
  
  /**
   * @param nm attribute name
   * @param value attribute value
   * @param dist distance to the parent element
   */
  private void addAttr(byte[] nm, byte[] value, int dist)
  {
    update();
    
    // add attribute
    data.attr(pre, dist, data.atnindex.index(nm, null, false),
        value, data.ns.uri(nm, false), false);
    dpre++;    
  }

  @Override
  public void emptyElem(byte[] nm, Atts att) throws IOException
  {
    startElem(nm, att);
    endElem();
  }

  @Override
  public void endElem() throws IOException
  {
    int spre = preStack.pop().dp + ipre;
    data.ns.close(spre);    
    sizeStack.push(new int[]{spre, pre - spre + 1});
  }

  @Override
  public void startNS(byte[] pref, byte[] uri)
  {
    // see if prefix has been declared/ is part of current ns scope
    final byte[] old = nsScope.get(pref);
    if(old == null || !eq(old, uri)) {
      // we have to keep track of all new NSNodes that are added
      // to the Namespace structure, as their pre values must not
      // be updated. I.e. if an NSNode N with pre value 3 existed
      // prior to inserting and two new nodes are inserted at
      // location pre == 3 we have to make sure N and only N gets
      // updated.
      newNodes.add(data.ns.add(pref, uri, pre));
    }
  }

  @Override
  public void text(byte[] value) throws IOException
  {
    // chop whitespaces in text nodes
    final byte[] t = data.meta.chop ? trim(value) : value;

    // check if text appears before or after root node
    final boolean ignore = !inDoc || preStack.size() == 1;
    if((data.meta.chop && t.length != 0 || !ws(t)) && ignore)
      error(inDoc ? AFTERROOT : BEFOREROOT, parser.detail());

    if(t.length != 0 && !ignore) addText(t, Data.TEXT);    
  }

  @Override
  public void comment(byte[] value) throws IOException
  {
    addText(value, Data.COMM);    
  }

  @Override
  public void pi(byte[] pi) throws IOException
  {
    addText(pi, Data.PI);    
  }

  /**
   * @param value text content
   * @param kind text kind (text, comment, etc.)
   */
  private void addText(byte[] value, byte kind)
  {    
    update();    
    data.text(pre, dis, value, kind);
    dpre++;
  }

  @Override
  public void encoding(String enc) { }

}
