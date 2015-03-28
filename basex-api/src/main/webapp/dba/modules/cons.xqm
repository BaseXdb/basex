(:~
 : Global constants and functions.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace cons = 'dba/cons';

import module namespace Session = 'http://basex.org/modules/session';

(:~ An error occured while retrieving data. :)
declare variable $cons:DATA-ERROR := 'Could not retrieve data';

(:~ Session key. :)
declare variable $cons:SESSION-KEY := "session";
(:~ Current session. :)
declare variable $cons:SESSION := Session:get($cons:SESSION-KEY);

(:~ Configuration file. :)
declare variable $cons:CONFIG-XML := file:base-dir() || '../files/config.xml';
(:~ Configuration. :)
declare %private variable $cons:CONFIG := map:merge(
  doc($cons:CONFIG-XML)/config/* ! map { name(): string() }
);

(:~ Language. :)
declare variable $cons:LANGUAGE := cons:string('language');

(:~ Maximum length of XML characters (currently: 1mb). :)
declare variable $cons:MAX-CHARS := cons:integer('maxchars');
(:~ Maximum number of table entries (currently: 1000 rows). :)
declare variable $cons:MAX-ROWS := cons:integer('maxrows');
(:~ Query timeout. :)
declare variable $cons:TIMEOUT := cons:integer('timeout');
(:~ Maximal memory consumption. :)
declare variable $cons:MEMORY := cons:integer('memory');
(:~ Permission when running queries. :)
declare variable $cons:PERMISSION := cons:string('permission');

(:~ Permissions. :)
declare variable $cons:PERMISSIONS := ('none', 'read', 'write', 'create', 'admin');

(:~
 : Returns a configuration string for the specified key.
 : @param  $key  key
 : @return text
 :)
declare %private function cons:string($key as xs:string) as xs:string {
  let $text := $cons:CONFIG($key)
  return if($text) then $text else error((), 'Missing in config.xml: "' || $text || '"')
};

(:~
 : Returns a configuration number for the specified key.
 : @param  $key  key
 : @return text
 :)
declare %private function cons:integer($key as xs:string) as xs:integer {
  xs:integer(cons:string($key))
};

(:~
 : Checks if the current client is logged in. If not, raises an error.
 :)
declare function cons:check(
) as empty-sequence() {
  if($cons:SESSION) then () else error(xs:QName("basex:login"), 'Please log in again.')
};
