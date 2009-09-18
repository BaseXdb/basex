<?
$top ="contact";
include("inc/header.inc");
?>
<?PHP

function schreiben($datei,$mode) {
   global $REMOTE_ADDR;
   $fp = @fopen($datei,$mode);
   flock($fp,2);
   fwrite($fp,$REMOTE_ADDR."|".time()."\n");
   flock($fp,3);
   fclose($fp);
}

function ip_sperre() {
   global $REMOTE_ADDR;
   $reloadlogdatei = "ips.txt";
   $anzahleintraege = 50;
   $zeitlimit = 3600;
   if(!file_exists($reloadlogdatei)) {
      $datei = fopen($reloadlogdatei,"w+");
      fclose($datei);
   }
   $fp = file($reloadlogdatei);
   $size = sizeof($fp);
   if($size >= $anzahleintraege){
      schreiben($reloadlogdatei,"w");
   }
   else {
      $ausgabe=false;
      for ($i=0;$i<$size;$i++) {
         $zeile = explode("|",$fp[$i]);
         if ($zeile[0] == $REMOTE_ADDR AND $zeile[1] > (time()-$zeitlimit)){
            // ip wird gefunden
            $ausgabe = true;
         }
      }
      schreiben($reloadlogdatei,"a+");
   }
   return $ausgabe;
}
$random = mt_rand(1,2);
$filename = "poll".$random.".txt";
$handle = fopen($filename, "r");
while (!feof($handle)) {
    $buffer = fgets($handle);
}
fclose($handle);
$buffer = trim($buffer);
$items = explode(";", $buffer);
$frage = $items[0];
$anzahl = (count($items)-1);
$i=0;
for ($i; $i<$anzahl; $i++) {
    $option[$i] = $items[($i+1)];
    $farbe[$i] = dechex(rand(0,10000000));
}
$datei = "poll".$random.".result";

if (file_exists($datei)) {
    $votes=file($datei);
}
else {
    $handle = fopen($datei, "w+");
    $i=0;
    for ($i; $i<$anzahl; $i++) {
        fputs($handle,"0\n");
    }
    fclose($handle);
    $votes=file($datei);
}

if (($submitvote) && (ip_sperre()==false)) {
    settype($votes[$radio],"integer");
    $votes[$radio]++;
    $handle = fopen($datei, "w+");
    $i=0;
    for ($i; $i<$anzahl; $i++) {
        settype($votes[$i],"integer");
        fputs($handle,"$votes[$i]\n");
    }
    fclose($handle);
}

echo "<H1>".$frage."</H1>\n\n";

echo "<FORM ACTION=\"".$PHP_SELF."\" METHOD=\"Post\">\n";
$i=0;
for ($i; $i<$anzahl; $i++) {
    echo "<INPUT TYPE=\"radio\" NAME=\"radio\" VALUE=\"".$i."\">".$option[$i]."<BR>\n";
}
echo "<br/>";
echo "<INPUT TYPE=\"Submit\" VALUE=\"Vote\" NAME=\"submitvote\"></FORM>";
echo "<br/><H1>Results:</H1>";
$i=0;
for ($i; $i<$anzahl; $i++) {
    echo $option[$i].": ".$votes[$i]." votes";
    $width = ($votes[$i]*2.5);
    echo '<hr align="left" style="color:'.$farbe[$i].'; background: '.$farbe[$i].'; background-color: '.$farbe[$i].'; height:15px; width: '.$width.'">';
}
?>
<? include("inc/footer.inc"); ?>