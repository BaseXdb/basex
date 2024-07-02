(:~
 : Change directory.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
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
  try {
    let $sep := file:dir-separator()
    let $path := file:path-to-native(if(contains($dir, $sep)) then (
      $dir
    ) else (
      config:files-dir() || $dir || $sep)
    )
    return (
      (: ensure that the directory can be accessed :)
      void(file:list($path)),
      config:set-files-dir($path)
    ),
    web:redirect($dba:CAT)
  } catch file:io-error {
    web:redirect($dba:CAT, { 'error': $err:description })
  }
};
