<? 
$top ="frontend";
include("inc/header.inc");
$desc = $_GET[desc];
$link = $_GET[name];
$w = 800;
$h = 620;

print "<h2>$desc</h2>\n";
print "<embed src='media/mediaplayer.swf' width='$w' height='$h' ";
print "allowscriptaccess='always' allowfullscreen='true' flashvars=";
print "'width=$w&height=$h&autostart=true&repeat=false&file=$link.mp4'/>";

include("inc/footer.inc"); 
?>
