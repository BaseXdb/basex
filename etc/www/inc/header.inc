<?php

$pages = array( "index.php" => "Processing and Visualizing XML Data", 
				"documentation.php" => "Documentation",
				"commands.php" => "Commands",
				"xml.php" => "Create a Database",
				"fs.php" => "Import your Filesystem",
				"faq.php" => "FAQ",
				"frontend.php" => "Frontend",
				"xpath.php" => "XPath Example",
				"xquery.php" => "XQuery Example",
				"download.php" => "Downloads",
				"publications.php" => "Publications",	
				"contact.php" => "Contact");

$title = $pages[$webpage];

$header .= '
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/2001/REC-xhtml11-20010531/DTD/xhtml11-flat.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<!--
    
    Pristine 1.0
    
    Design copyright Matt Dibb 2006
    www.mdibb.net

    Please feel free to use and modify this template for use on your site.  I
dont mind if you use it for your personal site or a commercial site, but I do
insist that it is not sold or given away in some "50,000 Templates!" package
or something like that.

-->

    <head>
				'.$meta.'
		    <meta http-equiv="Content-Language" content="en-us"/>
        <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
		    <meta name="author" content="BaseX Team"/>
		    <meta name="description" content="BaseX - Processing and Visualizing XML Data"/>
		    <meta name="keywords" content="BaseX,XML,XQuery,Database,Fulltext,Visualization"/>
		    <meta name="robots" content="all"/>
        <link rel="stylesheet" type="text/css" href="style.css" />
        <link rel="SHORTCUT ICON"
				     href="http://www.inf.uni-konstanz.de/dbis/basex/gfx/BaseX.ico"
						 type="image/ico" />
				<title>BaseX - '.$title.'</title>
    </head>
    
    <body>
    
        <div id="content"> <!-- is closed in footer.php -->
<!--
            <div id="search">
                <form action="..." method="get">
                    <p>search:&nbsp;<input type="text" />&nbsp;
                        <input type="image" src="gfx/zoom.jpg" value="Submit" alt="Search" title="Search" />
                    </p>
                </form>
            </div>
-->
            <div id="header">
	        <a href="index.php"><img src="gfx/BaseX.png" alt="BaseX Logo"/></a>
                <p>Processing and Visualizing XML Data<br/>with a native XML Database</p>
            </div>

            <div id="body">
';
            
            
echo <<< EOT

	$header

EOT;
?>            
