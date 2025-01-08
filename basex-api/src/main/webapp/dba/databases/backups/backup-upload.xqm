(:~
 : Upload backups.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/files';

import module namespace utils = 'dba/utils' at '../../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';

(:~
 : Uploads backups.
 : @param  $files  map with uploaded files
 : @return form or redirection
 :)
declare
  %rest:POST
  %rest:path('/dba/backup-upload')
  %rest:form-param('files', '{$files}')
function dba:file-upload(
  $files  as item()?
) as element(rest:response) {
  let $dir := db:option('dbpath') || '/'
  let $files := $files[. instance of map(*)] otherwise {}
  return try {
    (: reject backups with invalid content :)
    map:for-each($files, fn($file, $content) {
      let $name := replace($file, $utils:BACKUP-ZIP-REGEX, '$1')
      let $entries := archive:entries($content) ! data()
      where not(if ($name) {
        every $entry in $entries satisfies starts-with($entry, $name || '/') and
        $entries = $name || '/inf.basex'
      } else {
        every $entry in $entries satisfies matches($entry, '\.(xml|basex)')
      })
      return error((), 'Invalid backup file: ' || $file)
    }),
    map:for-each($files, fn($file, $content) {
      file:write-binary($dir || $file, $content)
    }),
    web:redirect($dba:CAT, { 'info': utils:info(map:keys($files), 'backup', 'uploaded') })
  } catch * {
    web:redirect($dba:CAT, { 'error': $err:description })
  }
};
