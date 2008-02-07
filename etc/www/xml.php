<? echo '<?xml version="1.0" encoding="ISO-8859-1" ?>';
$subpageOf ="frontend.php";
$webpage= basename($_SERVER['SCRIPT_NAME']);
include("inc/header.inc");
include("inc/nav.inc"); 
?>

<!-- ===== ... ===== -->

<div id="main">
<h1>How to create a new XML database instance</h1>
<p>
<h3>Use BaseX with an existing XML file</h3>
<ol>
<li>The easiest way for users to start experimenting with BaseX is to use an existing XML file. Just click on "File -> New...". 
<p><img src="gfx/Doc_New.gif"/></p></li>

<li>Choose one of your XML documents on disk. In this example we used the 111MB.xml file which you can find in our <a href="download.php">download section</a>.
<p><img src="gfx/Doc_FS.png"/></p></li>
<li>After you choose the XML file you can set some Options. You can use indexes to speed up your queries. If you got some parsing problems try it again but disable the option "Parsing entities".
<p><img src="gfx/Doc_Option.png"/></p></li>
<li>Now you should see this screen. If you need more information about how to use BaseX just read the <a href="overview.php">Overview section</a> or use the help in the lower left of the BaseX window.<p><img src="gfx/Doc_Finish.png"/></p></li>
</ol>
</p>

</div>
                
<!-- ===== ... ===== -->
                

<? include("inc/footer.inc"); ?>
