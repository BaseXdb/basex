<? echo '<?xml version="1.0" encoding="ISO-8859-1" ?>';
$subpageOf ="documentation.php";
$webpage= basename($_SERVER['SCRIPT_NAME']);
include("inc/header.inc");
include("inc/nav.inc"); 
?>

<!-- ===== ... ===== -->

<div id="main">
<h1>Example for executing XQuery requests</h1>

<p>
<pre>
<span class="orange">package</span> org.basex.test.examples;

<span class="orange">import</span> org.basex.core.Commands;
<span class="orange">import</span> org.basex.core.proc.Proc;
<span class="orange">import</span> org.basex.data.Result;
<span class="orange">import</span> org.basex.io.ConsoleOutput;
<span class="orange">import</span> org.basex.query.QueryException;
<span class="orange">import</span> org.basex.query.QueryProcessor;
<span class="orange">import</span> org.basex.query.xquery.XQueryProcessor;

<span class="comment">/**
 * This class serves an example for executing XQuery requests.
 */</span>
<span class="orange">public final</span> class XQueryExample {
<span class="comment">  /** Sample query. */</span>
  <span class="orange">private static final</span> String QUERY = <span class="red">"&lt;xml&gt;This is a test&lt;/xml&gt;/text()"</span>;

<span class="comment">  /** Private constructor. */</span>
  <span class="orange">private</span> XQueryExample() { }
<span class="comment">  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */</span>
  <span class="orange">public static void</span> main(<span class="orange">final</span> String[] args) <span class="orange">throws</span> Exception {

<span class="comment">    // FIRST EXAMPLE:</span>
    System.out.println(<span class="red">"First example:"</span>);

<span class="comment">    // create standard output stream</span>
    <span class="orange">final</span> ConsoleOutput out = <span class="orange">new</span> ConsoleOutput(System.out);

<span class="comment">    // Create a BaseX process</span>
    <span class="orange">final</span> Proc proc = Proc.get(Commands.XQUERY, QUERY);
<span class="comment">    // launch process</span>
    <span class="orange">if</span>(proc.execute()) {
<span class="comment">      // successful execution: print result</span>
      proc.output(out);
    } <span class="orange">else</span> {
<span class="comment">      // execution failed: print result</span>
      proc.info(out);
    }
    out.flush();
    System.out.println();
    
<span class="comment">    // SECOND EXAMPLE (ALTERNATIVE):</span>
    System.out.println(<span class="red">"Second example:"</span>);

<span class="comment">    // Execute XQuery request</span>
    <span class="orange">try</span> {
<span class="comment">      // create query instance</span>
      <span class="orange">final</span> QueryProcessor xquery = <span class="orange">new</span> XQueryProcessor(QUERY);
<span class="comment">      // execute query; no initial context set is specified (null)</span>
      <span class="orange">final</span> Result result = xquery.query(null);
<span class="comment">      // print output</span>
      result.serialize(out, <span class="orange">false</span>);
      out.println();
    } <span class="orange">catch</span>(<span class="orange">final</span> QueryException e) {
<span class="comment">      // dump stack trace</span>
      e.printStackTrace();
    }

<span class="comment">    // close output stream</span>
    out.close();
  }
}

</pre>
</p>

</div>


                
<!-- ===== ... ===== -->
                

<? include("inc/footer.inc"); ?>
