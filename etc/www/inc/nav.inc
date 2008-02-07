<?php
$notMainPage = false;
$pages = array( "index.php" => "none", 
				"documentation.php" => "none",
				"downloads.php" => "none",
				"xml.php" => "none",
				"fs.php" => "none",
				"faq.php" => "none",												
				"frontend.php" => "none",
				"publications.php" => "none",	
				"contact.php" => "none");					

$pages[$webpage] = "selected";

if(isset($subpageOf))
{
	$pages[$subpageOf] = "selected";
	$notMainPage = true;
}

$nav .= '
	<div id="links">
		<p>
			<span class="'.$pages["index.php"].'"><a href="index.php">Home</a>
			</span><br/>What\'s that BaseX thing?<br/><br/>
    	<span class="'.$pages["download.php"].'"><a href="download.php">Download</a>
			</span><br/>Get it on your own system!<br/><br/>
    	<span class="'.$pages["documentation.php"].'"><a href="documentation.php">Documentation</a>
			</span><br/>How can I use it?<br/><br/>
      <span class="'.$pages["frontend.php"].'"><a href="frontend.php">Visual BaseX</a>
			</span><br/>Have a look.<br/><br/>
      <span class="'.$pages["publications.php"].'"><a href="publications.php">Publications</a>
			</span><br/>Reports and presentations about BaseX<br/><br/>
	    <span class="'.$pages["contact.php"].'"><a href="contact.php">Contact</a>
			</span><br/>Guys behind the project 
    	</p>
    </div>
';

echo <<< EOT

	$nav

EOT;
?>
