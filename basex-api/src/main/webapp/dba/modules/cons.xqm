(:~
 : Global constants and functions.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace cons = 'dba/cons';

import module namespace Request = 'http://exquery.org/ns/request';
import module namespace Session = 'http://basex.org/modules/session';

(:~ Session key. :)
declare variable $cons:SESSION-KEY := 'dba';
(:~ Current session. :)
declare variable $cons:SESSION-VALUE := Session:get($cons:SESSION-KEY);

(:~ Directory for DBA files. :)
declare variable $cons:DBA-DIR := (
  for $dir in file:temp-dir() || 'dba'
  return (
    if(file:exists($dir)) then () else file:create-dir($dir),
    file:path-to-native($dir)
  )
);
(:~ Configuration file. :)
declare %private variable $cons:DBA-SETTINGS-FILE := $cons:DBA-DIR || 'dba-settings.xml';

(:~ Permissions. :)
declare variable $cons:PERMISSIONS := ('none', 'read', 'write', 'create', 'admin');

(:~ Maximum length of XML characters. :)
declare variable $cons:K-MAXCHARS := 'maxchars';
(:~ Maximum number of table entries. :)
declare variable $cons:K-MAXROWS := 'maxrows';
(:~ Query timeout. :)
declare variable $cons:K-TIMEOUT := 'timeout';
(:~ Maximal memory consumption. :)
declare variable $cons:K-MEMORY := 'memory';
(:~ Permission when running queries. :)
declare variable $cons:K-PERMISSION := 'permission';
(:~ Current directory. :)
declare variable $cons:K-DIRECTORY := 'directory';
(:~ Current query file. :)
declare variable $cons:K-QUERY := 'query';

(:~ Settings with default values. :)
declare %private variable $cons:K-DEFAULTS := map {
  $cons:K-DIRECTORY : $cons:DBA-DIR,
  $cons:K-MAXCHARS  : 200000,
  $cons:K-MAXROWS   : 200,
  $cons:K-TIMEOUT   : 30,
  $cons:K-MEMORY    : 500,
  $cons:K-PERMISSION: 'admin',
  $cons:K-QUERY     : ''
};

(:~ Current configuration. :)
declare variable $cons:OPTIONS := (
  if(file:exists($cons:DBA-SETTINGS-FILE)) then (
    try {
      (: merge defaults with options from settings file :)
      let $configs := fetch:xml($cons:DBA-SETTINGS-FILE)/config
      return map:merge(
        map:for-each($cons:K-DEFAULTS, function($key, $value) {
          map:entry($key,
            let $config := $configs/*[name() = $key]
            return if($config) then (
              if($value instance of xs:numeric) then xs:integer($config) else xs:string($config)
            ) else (
              $value
            )
          )
        })
      )
    } catch * {
      (: use defaults if an error occurs while parsing the configuration file :)
      $cons:K-DEFAULTS
    }
  ) else (
    $cons:K-DEFAULTS
  )
);

(:~
 : Returns the current DBA directory.
 : @return directory
 :)
declare function cons:current-dir(
) as xs:string {
  let $dir := $cons:OPTIONS($cons:K-DIRECTORY)
  return if(file:exists($dir)) then $dir else $cons:DBA-DIR
};

(:~
 : Saves settings.
 : @param  $settings  keys/values to be written
 :)
declare function cons:save(
  $settings  as map(*)
) as empty-sequence() {
  file:write($cons:DBA-SETTINGS-FILE, element config {
    map:for-each($cons:OPTIONS, function($key, $value) {
      element { $key } { ($settings($key), $value)[1] }
    })
  })
};

(:~
 : Checks if the current client is logged in. If not, raises an error.
 :)
declare function cons:check(
) as empty-sequence() {
  if($cons:SESSION-VALUE) then () else
    error(xs:QName('basex:login'), 'Please log in again', Request:path())
};

(:~
 : Convenience function for redirecting to another page from update operations.
 : @param  $url     URL
 : @param  $params  query parameters
 :)
declare %updating function cons:redirect(
  $url     as xs:string,
  $params  as map(*)
) as empty-sequence() {
  update:output(web:redirect($url, $params))
};

(:~
 : Returns the names of all files.
 : @return list of files
 :)
declare function cons:query-files() as xs:string* {
  let $dir := cons:current-dir()
  where file:exists($dir)
  return file:list($dir)[matches(., '\.xqm?$')]
};
