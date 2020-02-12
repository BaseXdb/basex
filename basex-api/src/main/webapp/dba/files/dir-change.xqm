(:~
 : Change directory.
 :
 : @author Christian Grün, BaseX Team 2005-20, BSD License
 :)
module namespace dba = 'dba/files';

import module namespace config = 'dba/config' at '../lib/config.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'files';

(:~
 : Changes the directory.
 : @param  $dir  directory
 : @return redirection
 :)
declare
  %rest:path('/dba/dir-change')
  %rest:query-param('dir', '{$dir}')
function dba:dir-change(
  $dir  as xs:string
) as element(rest:response) {
  config:directory(
    if(contains($dir, file:dir-separator())) then (
      $dir
    ) else (
      file:path-to-native(config:directory() || $dir || '/')
    )
  ),
  config:query(''),
  web:redirect($dba:CAT)
};
