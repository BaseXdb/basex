(:~
 : Download log file.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/logs-download';

import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'logs';

(:~
 : Downloads database logs.
 : @param  $names  names (dates) of log files
 : @return single or zipped file, or redirection
 :)
declare
  %rest:POST
  %rest:path('/dba/logs-download')
  %rest:form-param('name', '{$names}')
function dba:logs-download(
  $names  as xs:string*
) as item()+ {
  let $dir := db:option('dbpath') || '/.logs/'
  let $paths :=
    for $name in $names
    let $path := utils:safe-path($dir, $name || '.log')
    where file:is-file($path)
    return $path
  return if (empty($paths)) then (
    utils:outcome($dba:CAT, {}, { 'error': 'No log was selected.' })
  ) else (
    (: an archive of several logs is named after the first and the last date :)
    utils:download($paths, string-join(sort($names)[position() = (1, last())], '_'))
  )
};
