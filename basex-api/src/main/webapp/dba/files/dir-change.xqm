(:~
 : Change directory.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/files';

import module namespace session = 'dba/session' at '../modules/session.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'files';

(:~
 : Changes the directory.
 : @param  $dir  directory
 : @return redirection
 :)
declare
  %rest:path("/dba/dir-change")
  %rest:query-param("dir", "{$dir}")
function dba:dir-change(
  $dir  as xs:string
) as element(rest:response) {
  session:set($session:DIRECTORY,
    if(contains($dir, file:dir-separator())) then (
      $dir
    ) else (
      file:path-to-native(session:directory() || $dir || '/')    
    )
  ),
  session:set($session:QUERY, ''),
  web:redirect($dba:CAT)
};
