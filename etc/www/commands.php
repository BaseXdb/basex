<? echo '<?xml version="1.0" encoding="ISO-8859-1" ?>';
$subpageOf ="documentation.php";
$webpage= basename($_SERVER['SCRIPT_NAME']);
include("inc/header.inc");
include("inc/nav.inc"); 
?>

<!-- ===== ... ===== -->

<div id="main">
<h1>Documentation &ndash; Commands</h1>

<p>Here you find the BaseX commands which you can enter in the console mode
or in the Command text field in the GUI:</p>


<h3>Help</h3>
<p>
<code>help [command]</code><br/>
<br/>
Get help on BaseX commands.
If <code>[command]</code> is specified, information on the specific
command is printed; otherwise, all commands are listed.
If 'all' is specified, hidden commands are included.
</p>

<h3>Create</h3>
<p>
<code>create [xml|fs|index] [...]</code><br/>
<br/>
Create a database from XML or the filesystem, or create an index:
<ul>
<li><code>xml [file]</code>:<br/>
Create a new database for the the XML document specified with <code>[file]</code>.<br/>
The database will be named after the document name, excluding the suffix.
</li>
<li><code>fs [database] [path]</code>:<br/>
Create a database for the specified file <code>[path]</code>.<br/>
The Database will be named <code>[database]</code>.</li>
<li><code>index [text|attribute|word|fulltext]</code>:<br/>
Create the specified index for the currently opened database.</li>
</ul>
</p>

<h3>Open</h3>
<p>
<code>open [database]</code><br/>
<p>
Open the specified [database].
</p>

<h3>Info</h3>
<p>
<code>info [database|index|table]?</code><br/>
<br/>
Show information on the currently opened database:
<ul>
<li>no argument: show global information</li>
<li><code>database</code>: show database information</li>
<li><code>index</code>: show information on the existing indexes.</li>
<li><code>table [start end | query]</code>: show the internal XML table representation for
the specified numeric range <code>[start-end]</code>, or for the results of the
specified <code>[query]</code></li>
</ul>
</p>

<h3>Close</h3>
<p>
<code>close</code> <br/>
<br/>
Close the current database.
</p>

<h3>List</h3>
<p>
<code>list</code><br/>
<br/>
List all available databases.
</p>

<h3>Drop</h3>
<p>
<code>drop [database|index] [...]</code><br/>
<br/>
Drop a database or an index:
<ul>
<li><code>database [name]</code>:<br/>
Drop the database specified with <code>[name]</code>.</li>
<li><code>index [text|attribute|word|fulltext]</code>:<br/>
Drop the specified index in the currently opened database.</li>
</ul>
</p>

<h3>Export</h3>
<p>
<code>export [file]</code><br/>
<br/>
Export the current database or node set to an XML document, named <code>[file]</code>.
</p>

<h3>XPath</h3>
<p>
<code>xpath [query]</code><br/>
<br/>
Perform the specified XPath 1.0 <code>[query]</code> and print its result.
</p>

<h3>XQuery</h3>
<p>
<code>xquery [query]</code><br/>
<br/>
Perform the specified XQuery 1.0 <code>[query]</code> and print its result.
The complete input is treated as XQuery, so no other commands are allowed
in the same line. If no query is specified after the command, the query is
created from the following inputs, finished by an empty.
</p>

<h3>Find</h3>
<p>
<code>find [query]</code><br/>
<br/>
Evaluate a simple keyword <code>[query]</code> and print its results.
This command is used in the simple search mode in the GUI. The following
prefixes are supported:
<ul>
<li>(none): find any tags and substrings</li>
<li><code>=  </code>find exact text nodes</li>
<li><code>@  </code>find attributes and attribute values</li>
<li><code>@= </code>find exact attribute values</li>
</ul>
</p>

<h3>CD</h3>
<p>
<code>cd [query]</code><br/>
<br/>
Evaluate the <code>[query]</code> as XPath 1.0 and set the result as new 
context set.
</p>

<h3>Copy</h3>
<p>
<code>copy [pos] ["source"] ["target"]</code><br/>
<br/>
Copy database nodes.
Evaluate the XPath 1.0 <code>[source]</code> query and insert the
resulting nodes as child nodes into the <code>[target]</code> query.
<code>[pos]</code> specifies the child position; if <code>0</code>
is specified, the nodes are inserted as last child.
The queries should be enclosed by brackets.
</p>

<h3>Delete</h3>
<p>
<code>delete ["target"]</code>
<br/>
Delete database nodes resulting from the specified <code>[target]</code> query.
The query should be enclosed by brackets.
</p>

<h3>Insert</h3>
<p>
<code>insert [fragment|element|attribute|text|comment|pi] [...]</code><br/>
<br/>
Insert database nodes.
Insert a fragment or a specific node at the specified
child <code>[pos]</code> of the specified <code>[target]</code> query:
<ul>
<li><code>element [name] [pos] [target]</code>:<br/>
Insert an element with the specified tag <code>[name]</code>.</li>
<li><code>text [text] [pos] [target]</code>:<br/>
Insert a text node with the specified <code>[text]</code>.</li>
<li><code>attribute [name] [value] [target]</code>:<br/>
Insert an attribute with the specified <code>[name]</code> and <code>[value]</code></li>
<li><code>comment [text] [pos] [target]</code>:<br/>
Insert a comment with the specified <code>[text]</code>.</li>
<li><code>pi [name] [value] [pos] [target]</code>:<br/>
Insert a processing instruction with the specified
<code>[name]</code> and <code>[value]</code>.</li>
<li><code>fragment [frag] [pos] [target]</code>:<br/>
Insert an XML <code>[frag]</code>.</li>
</ul>
</p>

<h3>Update</h3>
<p>
<code>update [element|attribute|text|comment|pi] [...]</code><br/>
<br/>
Update database nodes satisfying the specified <code>[target]</code> query.
<ul>
<li><code>element [name] [target]</code>:<br/>
Update the resulting elements with the specified tag <code>[name]</code>.</li>
<li><code>text [text] [target]</code>:<br/>
Update the resulting text nodes with the specified <code>[text]</code>.</li>
<li><code>attribute [name] [value] [target]</code>:<br/>
Update the resulting attributes with the specified <code>[name]</code>
and </code>[value]</code>.</li>
<li><code>comment [text] [target]</code>:<br/>
Update the resulting comments with the specified <code>[text]</code>.</li>
<li><code>pi [name] [value] [target]</code>:<br/>
Update the resulting processing instructions with the specified
<code>[name]</code> and <code>[value]</code>.</li>
</ul>
</p>

<h3>Set</h3>
<p>
<code>set [option] [val]?</code><br/>
<br/>
Sets global options. The currently set values can be shown with the
<code>info</code> command. The following <code>[option]</code>s are
available and can be turned [on] or [off]:
<ul>
<li><code>debug</code>: Show internal debug info.</li>
<li><code>mainmem</code>: Use main-memory mode.</li>
</ul>
The following commands apply to the creation of new databases:<br/>&nbsp;
<ul>
<li><code>chop</code>: Chop all XML whitespace nodes.</li>
<li><code>entity</code>: Parse XML entities.</li>
<li><code>textindex</code>: : Index text nodes.</li>
<li><code>attrindex</code>: : Index attribute values.</li>
<li><code>wordindex</code>: Index all words (simplified full-text)</li>
<li><code>ftindex</code>: Index full-text</li>
<li><code>dbpath [path]</code>: Set a new database <code>[path]</code></li>
</ul>

The following commands apply to querying:<br/>&nbsp;
<ul>
<li><code>info [all]</code>: Show (all) info on query evaluation.</li>
<li><code>serialize</code>: Turn serialization of query results on/off.</li>
<li><code>xmloutput</code>: Serialize query results as XML.</li>
<li><code>runs [nr]</code>: Set the number of query runs.</li>
</ul>
Have a look into the <a href='faq.php'>FAQ</a> to find
more information on the available options.
<br/>&nbsp;
</p>

<h3>Exit</h3>
<p>
<code>exit/quit </code><br/>
<br/>
Leave the console mode of BaseX.
</p>

<!-- ===== ... ===== -->
                
<? include("inc/footer.inc"); ?> 

