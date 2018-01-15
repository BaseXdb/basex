(:~
 : Change directory.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/files';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';

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
  cons:check(),
  let $path := if(contains($dir, file:dir-separator())) then (
    $dir
  ) else (
    file:path-to-native(cons:current-dir() || $dir || '/')    
  )
  return cons:save(map { $cons:K-DIRECTORY: $path, $cons:K-QUERY: '' }),
  web:redirect($dba:CAT)
};
