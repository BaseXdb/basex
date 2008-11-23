<? $top ="documentation"; include("inc/header.inc"); ?>

<p>
If you can't find an answer on your question, please write to<code> info@basex.org</code>.
</p>
<p>
<b><a href="#1">1 Introduction</a></b><br/>
<a href="#1.1">1.1 What is BaseX? </a><br/>
<a href="#1.2">1.2 On what systems does BaseX run? </a><br/>
<a href="#1.3">1.3 Who maintains BaseX? </a><br/>
<a href="#1.4">1.5 Where to get the latest version?</a><br/>
<a href="#1.5">1.6 Where do I find the source code?</a><br/>
<a href="#1.6">1.7 How can I help/contribute?</a><br/>
<a href="#1.7">1.8 I found a bug, how do I file bug report?</a><br/>

<br/><b><a href="#2">2 BaseX Console</a></b><br/>
<a href="#2.1">2.1 How can I launch the console mode of BaseX?</a><br/>
<a href="#2.2">2.2 What can I do in the console mode?</a><br/>
<a href="#2.3">2.3 What is XPath/XQuery?</a><br/>
<a href="#2.4">2.4 What is Whitespace Chopping?</a><br/>
<a href="#2.5">2.5 What is Entity Parsing?</a><br/>
<a href="#2.6">2.6 How can i display the name of attributes?</a><br/>

<br/><b><a href="#3">3 BaseX GUI</a></b><br/>
<a href="#3.1">3.1 Views and Panels</a><br/>
<a href="#3.2">3.2 What is realtime filtering?</a><br/>
<a href="#3.3">3.3 What does the Filter button exactly do?</a><br/>
<a href="#3.4">3.4 How can I search without XPath/XQuery?</a><br/>
<a href="#3.5">3.5 Is the Search field in the Query panel the same as the Search field right under the buttons?</a><br/>
<a href="#3.6">3.6 How can I configure the look and feel of BaseX?</a><br/>
<a href="#3.7">3.7 What is the Main-Memory Mode?</a><br/>
<a href="#3.8">3.8 How can I import my file system?</a><br/>
<a href="#3.9">3.9 How can I import my data?</a><br/>

<br/><b><a href="#4">4 General Issues</a></b><br/>
<a href="#4.1">4.1 What additional files (config etc.) does BaseX use and where are they?</a><br/>
<a href="#4.2">4.2 How do you measure the performance of the queries?</a><br/>
<a href="#4.3">4.3 What indexing techniques are available and what do they do?</a><br/>
<a href="#4.4">4.4 Is it possible to start BaseX as a Server?</a><br/>

<br/><b><a href="#5">5 Features</a></b><br/>
<a href="#5.1">5.1 Can I have more information on the XPath/XQuery implementations?</a><br/>
<a href="#5.2">5.2 OK, you support several Indexes. What exactly can I use and how?</a><br/>
<a href="#5.3">5.3 What about the different indexes BaseX offers: which data structures are applied?</a><br/>
<a href="#5.4">5.4 Tell me more about XQuery Full-Text. How can I use it?</a><br/>
<a href="#5.5">5.5 Do you support XML Updates?</a><br/>
</p>

<p><br/></p>

<a name="1"></a><h2>1 Introduction</h2>

<a name="1.1"></a><h3>1.1 What is BaseX?</h3>
<p>
BaseX is a native XML database.  It features an XPath/XQuery implementation,
supports updates and has a visual frontend, facilitating visual access to the
stored data.
</p>

<a name="1.2"></a><h3>1.2 On what systems does BaseX run? </h3>
<p>
BaseX is completely written in Java.  As such it is plattform-independant and
should run on any plattform that has a Java Virtual Machine available.  BaseX
uses Java Version 1.5 or later, so this might be a limitation.  BaseX has been
tested on Windows (2000, XP, Vista), Max OS X (10.4, 10.5), Linux(SuSE xxx,
Debian) and OpenBSD (4.2).
</p>

<a name="1.3"></a><h3>1.3 Who maintains BaseX? </h3>
<p>
BaseX is developed by the <a href="http://www.inf.uni-konstanz.de/dbis">Database and Information Systems Group</a> at the University of Konstanz.
Main developer and project leader is <a href="http://www.inf.uni-konstanz.de/~gruen">Christian Gr&uuml;n</a>.
Several <a href="contact.php">group members</a> 
are contributing to the project.
</p>

<a name="1.4"></a><h3>1.4 Where to get the latest version?</h3>
<p>
The latest version is found at <a href='download.php'>www.basex.org</a>
and on the <a href="http://sourceforge.net/projects/basex">Sourceforge Project Site</a>.
</p>

<a name="1.5"></a><h3>1.5 Is there access to the source code?</h3>
<p>
The BaseX sources are available in a Subversion repository via Sourceforge.
<a href="http://sourceforge.net/projects/basex">Sourceforge Project Site</a>.
You also find them as part of the complete, zipped distribution of BaseX:
<a href="http://sourceforge.net/projects/BaseX-Complete.zip">BaseX-Complete.zip</a>
</p>

<a name="1.6"></a><h3>1.6 How can I help/contribute?</h3>
<p>
BaseX is still work in progress. So we are definitely interested in all kind
of experiences you gain during usage. Please provide feedback, bug reports,
report about missing features, your application domain. This all can be done
by posting to <code>info@basex.org</code>.
</p>

<a name="1.7"></a><h3>1.7 I found a bug, how do I file bug report?</h3>
<p>
There is a bug reporting system on SourceForge. Otherwise send an email to <code>info@basex.org</code>.
</p>

<a name="2"></a><h2>2 BaseX Console</h2>

<a name="2.1"></a><h3>2.1 How can I launch the console mode of BaseX?</h3>
<p>
Just enter<code> java -cp BaseX.jar org.base.BaseX </code>on the command line.
The Java option<code> -Xmx... </code>reserve more memory, and the<code> -h </code>
flag of BaseX lists all available flags:
</p>
<blockquote>
<code>
java -Xmx512m -cp BaseX.jar org.basex.BaseX -h

BaseX 5.0; DBIS, University of Konstanz
Usage: BaseX [options] [query]
  [query]    specify query file
  -c         chop whitespaces
  -d         debug mode
  -e         skip entity parsing
  -o [file]  specify output file
  -p         handle query file as XPath
  -q         send BaseX commands
  -v/V       show (all) process info
  -x         print result as xml
  -z         skip query output

</code>
</blockquote>

<a name="2.2"></a><h3>2.2 What can I do in the console mode?</h3>
<p>
Type in<code> help </code> to get a list of all
<a href="commands.php">BaseX commands</a>. Several commands can
be separated by semicolons. To evaluate commands
without entering the console mode, you can use the
<code>-q</code> option on the command line:
</p>
<blockquote>
<pre>
java -Xmx512m -cp BaseX.jar org.basex.BaseX -vq "create xml input.xml; xpath /"

Process Info ON
Database 'input' created in 155.17 ms.
&lt;html&gt;
  &lt;!-- Header --&gt;
  &lt;head id="0"&gt;
    &lt;title&gt;XML&lt;/title&gt;
  &lt;/head&gt;
  &lt;!-- Body --&gt;
  &lt;body id="1" bgcolor="#FFFFFF" text="#000000" link="#0000CC"&gt;
    &lt;h1&gt;Databases &amp; XML&lt;/h1&gt;
    &lt;div align="right"&gt;
      &lt;b&gt;Assignments&lt;/b&gt;
      &lt;ul&gt;
        &lt;li&gt;Exercise 1&lt;/li&gt;
        &lt;li&gt;Exercise 2&lt;/li&gt;
      &lt;/ul&gt;
    &lt;/div&gt;
  &lt;/body&gt;
&lt;/html&gt;

Parsing   : 110.35 ms
Compiling : 0.81 ms
Evaluating: 0.05 ms
Printing  : 2.06 ms
Total Time: 113.35 ms
Results   : 1 Item
Printed   : 360 Bytes
</pre>
</blockquote>

<a name="2.3"></a><h3>2.3 What is XPath/XQuery?</h3>
<ul>
  <li>XPath is a simple query language for finding information in an
XML document. XPath is used to navigate through elements and attributes
in an XML document
(<a href="http://www.w3.org/TR/xpath">XPath - W3C Website</a>).</li>
  <li> XQuery is a complete query language from the W3C that
provides an easy way to query, transform, and integrate XML data.
(<a href="http://www.w3.org/TR/xquery">XQuery - W3C Website</a>).</li>
</ul>

<a name="2.4"></a><h3>2.4 What is Whitespace Chopping?</h3>
<div>This option removes all whitespaces between tags and contents. In the current
version of BaseX, there is no DTD or XML schema detection which would make
it possible to explicitly ignore special whitespaces.</div>

<a name="2.5"></a><h3>2.5 What is Entity Parsing?</h3>
<div>The parsing of entities in the document can be skipped or switched on with this
option. Entities are important for DTD declarations and definitions of special
characters.</div>

<a name="2.6"></a><h3>2.6 How can i display the name of attributes?</h3>
<div>There is an option in the Options/Preferences menu where it's possible to choose,
replace tags with @name attributes.</div>

<a name="3"></a><h2>3 BaseX GUI</h2>

<a name="3.1"></a><h3>3.1 Views and Panels</h3>
<div>
  <dl>
    <dt>Text View</dt>
    <dd>Displays query results and other textual output.</dd>

    <dt>Map View</dt>
    <dd>This visualization represents all data in a TreeMap. All nodes of the XML document
                are represented as rectangles, filling the complete area.</dd>

    <dt>Tree View</dt>
    <dd>This visualization displays all XML nodes in a usual tree view.</dd>

    <dt>Table View</dt>
    <dd>This visualization displays all XML nodes in a table with rows and columns.</dd>

    <dt>Scatterplot View</dt>
    <dd>This visualizations displays all XML nodes in a scatterplot.</dd>
  </dl>
</div>

<a name="3.2"></a><h3>3.2 What is Realtime Filtering?</h3>
<div>
If realtime filtering is enabled, all visualizations directly show the query results
while entering the query. If this feature is disabled, the query results are highlighted
in the visualizations and can be ecplicitly filtered using the 'Filter' button.
</div>

<a name="3.3"></a><h3>3.3 What does the Filter button exactly do?</h3>
<div>
After pressing this button, the visualizations display the previously highlighted
XML nodes, omitting all the other nodes of the document.
If realtime filtering is enabled, the Filter button will be disabled, and
all results are automatically filtered.
</div>

<a name="3.4"></a><h3>3.4 How can I search without XPath/XQuery?</h3>

<div>
The Search field triggers a simple search query in the XML document. The following syntax is supported:<br/>
<center>
<table>
<tr>
  <th align='left'>Query &nbsp; &nbsp; &nbsp;</th>
  <th align='left'>Description</th>
</tr>
<tr>
  <td align='left'><i>foo</i></td>
  <td align='left'>Find tags and texts containing <i>foo</i></td>
</tr>
<tr>
  <td align='left'>=<i>foo</i></td>
  <td align='left'>Find exact matching text nodes</td>
</tr>
<tr>
  <td align='left'>@<i>foo</i></td>
  <td align='left'>Find attributes and attribute values</td>
</tr>
<tr>
  <td align='left'>@=<i>foo</i></td>
  <td align='left'>Find exact attribute values</td>
</tr>
</table>
</center>
</div>

<a name="3.5"></a><h3>3.5 The search field in the query panel and the search
field right beyond the buttons, do they differ?</h3>

<div>
Not really, both offer the same functionality, but in the query panel there is
a dropdown menu where it's possible to choose tags or attributes of the XML
document.
</div>

<a name="3.6"></a><h3>3.6 How can I configure the look and feel of BaseX?</h3>
<div>
All panels are freely adjust- and dragable.  Changing of colors, fonts and the
layout is possible via the 'Layout' entry in the 'Options' menu.
</div>

<a name="3.7"></a><h3>3.7 What is the Main-Memory Mode?</h3>
<div>
The Main-Memory Mode speeds up querying but disables updates.
The table data is kept in memory and the text of a document is still accesseed
from disk.
</div>

<a name="3.8"></a><h3>3.8 How can I import my file system? What does that mean?</h3>
<div>
To import a file system, go to 'File/Import Filesystem...'.  You will be
prompted for the next steps.  A file hierarchy traversal is performed and the
directory structure of the filesystem is mapped into an XML representation.
Additionally some metadata is extracted from some known file types.  What you
get is an 'XML view' of your current file system.  You can query the filesystem
just as any other database instance.  Manipulation (update, deletion) of the
XML database instance representing a file hierarchy has no effect on the real
filesystem.
</div>

<a name="3.9"></a><h3>3.9 How can I import my data aka how do i create a new database instance aka how to shred an XML file?</h3>
<div>
<ol>
  <li>If your input data is an XML file,  you can use the 'New' command.  BaseX
will create a new database with the name of your XML file. (GUI only)</li>
  <li>You can use the <code>create</code> command. (GUI and console)</li>
</ol>
</div>

<a name="4"></a><h2>4 General Issues</h2>

<a name="4.1"></a><h3>4.1 Where does BaseX store data and configuration files?</h3>
<div>
<ul>
<li>The databases are stored in a <code>BaseXData</code> directory in your home directory.
The path can be changed in the GUI via the Options/Preferences menu.</li>
<li>The two configuration files <code>.basex</code>/<code>.basewin</code> are stored
in the same directory. This path can't be changed by the user.</li>
</ul>
</div>

<a name="4.2"></a><h3>4.2 How do you measure the performance of the queries?</h3>
<div>
The measurements include parsing, compilation, evaluation and
printing time of a query.
There are different ways to retrieve the performance info:
<ul>
<li>Console: use "-v" flag as command line argument</li>
<li>Console Mode: enter <code>set info</code> or
  <code>set info all</code" in the console mode</li>
<li>GUI Mode: display performance results in the QueryInfo view</li>
</ul>
</div>

<a name="4.3"></a><h3>4.3 What indexing techniques are available and what do they do?</h3>
<div>
Indexes can speedup queries by magnitudes.
Currently, four indexes exist:<br/>
<ul>
<li> Text Index: All text nodes are indexed to speedup XPath predicates.</li>
<li> Attribute Value Index: All attributes are indexed to speedup XPath predicates.</li>
<li> Full-Text Index: A full-text index is created to speedup content based queries in XPath.</li>
</ul>
Note that the indexes only speedup XPath and simple user queries. The
query processor will optimize queries completely automatic whenever
possible. Expect XQuery to make use of the indexes in the next official
release.<br/>
</div>

<a name="4.4"></a><h3>4.4 Is it possible to start BaseX as a Server?</h3>
<div>
In addition to the standalone version, it is possible to run BaseX
in a Client/Server environment:
<blockquote>
<div>
<h3>Server</h3>
To start BaseX in server mode, type in:<br/><br/>
<code>java -cp BaseX.jar org.basex.BaseXServer</code><br/><br/>
If BaseXServer is succesfully started this is coming up:<br/><br/>
<code>BaseXServer 5.0 Server started.</code><br/>
<code>Waiting for requests...</code><br/><br/>
To list all available flags, add<code> -h </code>to the call, e.g.:
<pre>
java -cp BaseX.jar org.basex.BaseXServer -h

BaseX 5.0; DBIS, University of Konstanz
Usage: java BaseXServer [options]
 stop     stop server
 -d       debug mode
 -h       show this help
 -p&lt;port&gt; specify server port
 -v       verbose mode
</pre>

<h3>Client</h3>
To run a BaseX client, please type:<br/><br/>
<code>java -cp BaseX.jar org.basex.BaseXClient -s server.org</code><br/><br/>
The<code> -s </code> flag specifies the computer on which the server is running.<br/>
If BaseXClient is succesfully connected to a BaseXServer this is coming up:<br/><br/>
<code>BaseX 5.0 [Client]</code><br/>
<code>Try "help" to get some information.</code><br/><br/>
To list all available flags, just add<code> -h </code>to the call, e.g.:
<pre>
java -cp BaseX.jar org.basex.BaseXClient -h

BaseX 5.0; DBIS, University of Konstanz
Usage: BaseXClient [options] [query]
 -d        debug mode
 -h        show this help
 -o [file] specify output file
 -p&lt;port&gt;  specify server port
 -q&lt;cmd&gt;   send BaseX commands
 -s&lt;name&gt;  specify server name
 -v/V      show (all) process info
 -x        print result as xml
 -z        skip query output
</pre>

</div></blockquote>

<a name="5"></a><h2>5 Features</h2>
<a name="5.1"></a><h3>5.1 Can I have more information on the XPath/XQuery implementations?</h3>
In BaseX, XPath and XQuery are based on different implementations. Whereas XPath was
especially tuned for efficient querying, XQuery strives for a high standard conformance.
As soon as the XQuery implementation includes the optimizations that have
been applied to XPath, the XPath implementation might get obsolete.

<a name="5.2"></a><h3>5.2 OK, you support several Indexes. What exactly can I use and how?</h3>
Text indexes allow a speedup of order of magnitudes for text-based queries.
Here are some examples for queries which are rewritten for index access:

<h3>Text-Based Queries:</h3>
<ul>
<li><code>//node()[text() = 'Usability']</code></li>
<li><code>//div[p = 'Usability' or p = 'Testing']</code></li>
<li><code>path/to/relevant[text() = 'Usability Testing']/and/so/on</code></li>
</ul>
<h3>Attribute Index:</h3>
<ul>
<li><code>//node()[@align = 'right']</code></li>
<li><code>descendant::elem[@id = '1']</code></li>
<li><code>range/query[@id &gt;= 1 and @id &lt;= 5]</code></li>
</ul>
<h3>Full-Text Index:</h3>
<ul>
<li><code>//node[text() ftcontains 'Usability']</code></li>
<li><code>//node[text() ftcontains 'Usebiliti' with fuzzy]</code></li>
<li><code>//book[chapter ftcontains ('web' ftor 'WWW' without stemming) ftand 'diversity'
 case sensitive with stemming distance at most 5 words]</code></li>
</ul>
The full-text index is optimized to support all full-text
features of the XQuery Full-Text recommendation.<br/><br/>
BaseX extends the specification by offering a fuzzy match option.
Fuzzy search is based on the Levenshtein algorithm; the longer
query terms are, the more errors will be tolerated.
<br/><br/>
Default "Case Sensitivity", "Stemming" and "Diacritics" options 
will be considered in the index creation. Consequently, all queries
will be sped up which use the default index options.

<a name="5.3"></a><h3>5.3 What about the different indexes BaseX offers: which data structures are applied?</h3>

<div>
<ul>
<li><b>Text Index: B-Tree</b><br/>
  Both the text and attribute index are based on a B-Tree
	and support fast exact and range queries.</li>
<li><b>Full-Text Index (Fuzzy Version)</b><br/>
  The standard full-text Index is implemented as sorted array
  structure. It is optimized for simple and fuzzy searches.</li>
<li><b>Full-Text Index (Full Version)</b><br/>
  A second full-text Index is implemented as a compressed trie.
	Its needs slightly more memory than the standard full-text index,
	but it supports more features, such as full wildcard search.
</li>
</ul>
</div>

<a name="5.4"></a><h3>5.4 Tell me more about XQuery Full-Text. How can I use it?</h3>

The full-text features can be used in XPath as well as in XQuery. Again, queries will be
evaluated much faster in XPath, and XQuery covers more features of the language specification.
The following example queries are based on the
<a href="http://www.w3.org/TR/xpath-full-text-10-use-cases/">W3C Use Cases</a>:<br/>&nbsp;<br/>

<b>Return all title tags which contain the word 'Usability'</b>
<br/><br/>
<code>  //title[text() ftcontains 'Usability']</code>
<br/><br/>
<b>Return all authors, containing 'Marigold'</b>
<br/><br/>
<code>  for    $i in //author score $s
  where  $i/text() ftcontains 'Marigold'
  return &lt;hit score="{ $s }"&gt;{ $i/text() }&lt;/hit&gt;</code>
<br/><br/>

<a name="5.5"></a><h3>5.5 Do you support XML Updates?</h3>
BaseX provides internal commands to perform updates on XML (see also <a href="commands.php">BaseX Commands</a>).
Moreover, the GUI provides a convenient way to perform updates on the data.

<br/>&nbsp;<br/>
</div>

<? include("inc/footer.inc"); ?>

