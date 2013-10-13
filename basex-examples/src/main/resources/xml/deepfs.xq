module namespace deepfs = "http://www.deepfs.org/";

declare function deepfs:pe2pn($f as element()+) as xs:string* {
  for $e in $f
  return fn:string-join(
  for $v in $e/ancestor-or-self::*
  return if ($v/@root) then '' else fn:data($v/@name), '/')
};
