package org.basex.modules;

import java.io.*;


import org.apache.lucene.queryparser.classic.*;
import org.basex.data.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;

public class Lucene extends QueryModule{

   public ANode[] search(String query) throws QueryException{
     int[] pres = null;
     try{
       pres = LuceneQuery.query(query, queryContext.context);
     } catch(ParseException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     } catch(IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }

     ANode[] nodes = new ANode[pres.length];
     for(int i = 0; i < pres.length; i++){
       DBNode n = new DBNode(queryContext.data(),pres[i]);
       nodes[i] = n;
     }
     return nodes;
   }


}
