(:~
 : Upload backups.
 :
 : @author Christian Grün, BaseX Team 2005-23, BSD License
 :)
module namespace dba = 'dba/files';

import module namespace config = 'dba/config' at '../../lib/config.xqm';
import module namespace util = 'dba/util' at '../../lib/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';

(:~
 : Uploads backups.
 : @param  $files  map with uploaded files
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path('/dba/backup-upload')
  %rest:form-param('files', '{$files}')
function dba:file-upload(
  $files  as map(xs:string, xs:base64Binary)
) as element(rest:response) {
  (: save files :)
  let $dir := db:option('dbpath') || '/'
  return try {
    (: reject backups with invalid content :)
    map:for-each($files, function($file, $content) {
      let $name := replace($file, $util:BACKUP-ZIP-REGEX, '$1')
      let $entries := archive:entries($content) ! data()
      where not(if($name) then (
        every $entry in $entries satisfies starts-with($entry, $name || '/') and
        $entries = $name || '/inf.basex'
      ) else (
        every $entry in $entries satisfies matches($entry, '\.(xml|basex)')
      ))
      return error((), 'Invalid backup file: ' || $file)
    }),
    map:for-each($files, function($file, $content) {
      file:write-binary($dir || $file, $content)
    }),
    web:redirect($dba:CAT, map { 'info': util:info(map:keys($files), 'backup', 'uploaded') })
  } catch * {
    web:redirect($dba:CAT, map { 'error': $err:description })
  }
};
