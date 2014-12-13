(:~
 : Global constants.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace G = 'dba/global';

import module namespace Session = 'http://basex.org/modules/session';

(:~ Login error code. :)
declare variable $G:LOGIN-ERROR := xs:QName("basex:login");
(:~ An error occured while retrieving data. :)
declare variable $G:DATA-ERROR := 'Could not retrieve data';

(:~ Session key. :)
declare variable $G:SESSION-KEY := "session";
(:~ Current session. :)
declare variable $G:SESSION := Session:get($G:SESSION-KEY);

(:~ Configuration file. :)
declare variable $G:CONFIG-XML := file:base-dir() || '../files/config.xml';
(:~ Configuration. :)
declare %private variable $G:CONFIG := map:merge(
  doc($G:CONFIG-XML)/config/* ! map { name(): string() }
);

(:~ Language. :)
declare variable $G:LANGUAGE := G:string('language');

(:~ Maximum length of XML characters (currently: 1mb). :)
declare variable $G:MAX-CHARS := G:integer('maxchars');
(:~ Maximum number of table entries (currently: 1000 rows). :)
declare variable $G:MAX-ROWS := G:integer('maxrows');
(:~ Query timeout. :)
declare variable $G:TIMEOUT := G:integer('timeout');
(:~ Maximal memory consumption. :)
declare variable $G:MEMORY := G:integer('memory');
(:~ Permission when running queries. :)
declare variable $G:PERMISSION := G:string('permission');

(:~ Permissions. :)
declare variable $G:PERMISSIONS := ('none', 'read', 'write', 'create', 'admin');

(:~ Year one. :)
declare variable $G:YEAR-ONE := xs:dateTime('0001-01-01T01:01:01');
(:~ Time one. :)
declare variable $G:TIME-ZERO := xs:time('00:00:00');

(:~
 : Returns a configuration string for the specified key.
 : @param  $key  key
 : @return text
 :)
declare %private function G:string($key as xs:string) as xs:string {
  let $text := $G:CONFIG($key)
  return if($text) then $text else error((), 'Missing in config.xml: "' || $text || '"')
};

(:~
 : Returns a configuration number for the specified key.
 : @param  $key  key
 : @return text
 :)
declare %private function G:integer($key as xs:string) as xs:integer {
  xs:integer(G:string($key))
};
