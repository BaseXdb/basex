<? echo '<?xml version="1.0" encoding="ISO-8859-1" ?>';
$subpageOf ="documentation.php";
$webpage= basename($_SERVER['SCRIPT_NAME']);
include("inc/header.inc");
include("inc/nav.inc"); 
?>

<!-- ===== ... ===== -->

<div id="main">
<h1>Example for executing XPath requests</h1>
<p>
<pre>
<span class="orange">package</span> org.basex.test.examples;

<span class="orange">import</span> java.io.FileOutputStream;
<span class="orange">import</span> org.basex.core.Commands;
<span class="orange">import</span> org.basex.core.proc.Create;
<span class="orange">import</span> org.basex.core.proc.Proc;
<span class="orange">import</span> org.basex.data.Data;
<span class="orange">import</span> org.basex.data.Nodes;
<span class="orange">import</span> org.basex.data.Result;
<span class="orange">import</span> org.basex.io.ConsoleOutput;
<span class="orange">import</span> org.basex.io.PrintOutput;
<span class="orange">import</span> org.basex.query.QueryException;
<span class="orange">import</span> org.basex.query.QueryProcessor;
<span class="orange">import</span> org.basex.query.xpath.XPathProcessor;

<span class="comment">/**
 * This class serves an example for executing XPath requests.
 */</span> 
<span class="orange">public final class</span> XPathExample {
<span class="comment">  /** Input XML file. */</span>
<span class="orange">  private static final</span> String XMLFILE = <span class="red">"input.xml"</span>;
<span class="comment">  /** Name of the resulting database. */</span>
<span class="orange">  private static final</span> String DBNAME = <span class="red">"input"</span>;
<span class="comment">  /** Sample query. */</span>
<span class="orange">  private static final</span> String QUERY = <span class="red">"//li"</span>;
<span class="comment">  /** Result file. */</span>
<span class="orange">  private static final</span> String RESULT = <span class="red">"result.txt"</span>;

<span class="comment">  /** Private constructor. */</span>
<span class="orange">  private</span> XPathExample() { }

<span class="comment">   /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */</span>   
  <span class="orange">public static void</span> main(<span class="orange">final</span> String[] args) <span class="orange">throws</span> Exception {
<span class="comment">    // First example, creating a database and
    // writing the query result to RESULT:</span>
    System.out.println(<span class="red">"First example, writing result to "</span> + RESULT + <span class="red">":"</span>);

<span class="comment">    // execute the specified command</span>
    Proc.execute(Commands.CREATEXML, XMLFILE);
<span class="comment">    // writing result as well-formed XML</span>
    Proc.execute(Commands.SET, <span class="red">"xmloutput on"</span>);

<span class="comment">    // create a process for the XPath command </span>
    <span class="orange">final</span> Proc proc = Proc.get(Commands.XPATH, QUERY);

<span class="comment">    // create standard output stream</span>
    <span class="orange">final</span> PrintOutput file = <span class="orange">new</span> PrintOutput(<span class="orange">new</span> FileOutputStream(RESULT));

<span class="comment">    // launch process</span>
    <span class="orange">if</span>(proc.execute()) {
<span class="comment">      // successful execution: print result</span>
      proc.output(file);
    } <span class="orange">else</span> {
<span class="comment">      // execution failed: print result</span>
      proc.info(file);
    }
<span class="comment">    // close output stream</span>
    file.close();
    System.out.println();
    
<span class="comment">    // SECOND EXAMPLE:</span>
    System.out.println(<span class="red">"Second example, writing result to standard output:"</span>);

<span class="comment">    // Execute XPath request</span>
    <span class="orange">try</span> {
<span class="comment">      // create new database; "input" = database name, "input.xml" = source doc.</span>
      <span class="orange">final</span> Data data = Create.xml(DBNAME, XMLFILE);

<span class="comment">      // create query instance</span>
      <span class="orange">final</span> QueryProcessor xpath = <span class="orange">new</span> XPathProcessor(QUERY);
<span class="comment">      // create context set, referring to the root node (0)</span>
      <span class="orange">final</span> Nodes nodes = <span class="orange">new</span> Nodes(0, data);
<span class="comment">      // execute query</span>
      <span class="orange">final</span> Result result = xpath.query(nodes);

<span class="comment">      // print output to file</span>
      <span class="orange">final</span> ConsoleOutput console = <span class="orange">new</span> ConsoleOutput(System.out);
      result.serialize(console, <span class="orange">false</span>);
      console.flush();
    } <span class="orange">catch</span>(<span class="orange">final</span> QueryException e) {
<span class="comment">      // dump stack trace</span>
      e.printStackTrace();
    }
  }
}
</pre>
</p>

</div>


                
<!-- ===== ... ===== -->
                

<? include("inc/footer.inc"); ?>
