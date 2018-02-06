(:~
 : Session values.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace session = 'dba/session';

import module namespace options = 'dba/options' at 'options.xqm';
import module namespace Session = 'http://basex.org/modules/session';

(:~ Session key. :)
declare variable $session:ID := 'dba';
(:~ Current session. :)
declare variable $session:VALUE := Session:get($session:ID);

(:~ Current directory. :)
declare variable $session:DIRECTORY := $session:ID || '-directory';
(:~ Current query. :)
declare variable $session:QUERY := $session:ID || '-query';

(:~
 : Closes the session.
 :)
declare function session:close() as empty-sequence() {
  Session:delete($session:ID)
};

(:~
 : Returns a session value.
 : @return session value
 :)
declare function session:get(
  $name  as xs:string
) as xs:string? {
  Session:get($name)
};

(:~
 : Assigns session values.
 : @param  $name   name
 : @param  $value  value
 :)
declare function session:set(
  $name   as xs:string,
  $value  as xs:string
) as empty-sequence() {
  if($value) then Session:set($name, $value)
  else Session:delete($name)
};

(:~
 : Returns the current query directory.
 : @return directory
 :)
declare function session:directory() as xs:string {
  let $dir := Session:get($session:DIRECTORY)
  return if(exists($dir) and file:exists($dir)) then $dir else $options:DBA-DIRECTORY
};

(:~
 : Returns the names of all files.
 : @return list of files
 :)
declare function session:query-files() as xs:string* {
  let $dir := session:directory()
  where file:exists($dir)
  return file:list($dir)[matches(., '\.xqm?$')]
};
