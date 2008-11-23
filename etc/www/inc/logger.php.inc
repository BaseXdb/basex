<?
$addr = $HTTP_SERVER_VARS['REMOTE_ADDR'];
$host = gethostbyaddr($addr);

if(!(preg_match("/googlebot/", $host) ||
     preg_match("/search.live/", $host) ||
	   preg_match("/crawl.yahoo/", $host) ||
     preg_match("/inktomisearch/", $host) ||
		 preg_match("/phobos09/", $host))) 
{
    $fp = fopen("/www_io_mnt/gruen/bx/basex.log", "a");
    fwrite($fp, date('j.m.y, G:i')." Uhr [$addr, $host] $webpage\n");
    fclose($fp);
}
?>

