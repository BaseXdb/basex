(:~
 : DBA configuration.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace config = 'dba/config';

(:~ Session key. :)
declare variable $config:SESSION-KEY := 'dba';
(:~ Current directory in the file panel. :)
declare %private variable $config:FILES-DIR := 'dba-files-dir';
(:~ Name of currently edited file. :)
declare %private variable $config:EDITED-FILE := 'dba-edited-file';

(:~ DBA directory. :)
declare variable $config:DBA-DIR := (
  for $dir in db:option('dbpath') || '/.dba'
  return (
    if(file:exists($dir)) then () else file:create-dir($dir),
    file:path-to-native($dir)
  )
);

(:~ Permission values. :)
declare variable $config:PERMISSIONS := ('none', 'read', 'write', 'create', 'admin');
(:~ Indentation values. :)
declare variable $config:INDENTS := ('no', 'yes');

(:~ Maximum length of XML characters. :)
declare variable $config:MAXCHARS := 'maxchars';
(:~ Maximum number of table entries. :)
declare variable $config:MAXROWS := 'maxrows';
(:~ Query timeout. :)
declare variable $config:TIMEOUT := 'timeout';
(:~ Maximal memory consumption. :)
declare variable $config:MEMORY := 'memory';
(:~ Permission when running queries. :)
declare variable $config:PERMISSION := 'permission';
(:~ Show DBA log entries. :)
declare variable $config:IGNORE-LOGS := 'ignore-logs';
(:~ Indent results. :)
declare variable $config:INDENT := 'indent';

(:~ Options file. :)
declare %private variable $config:OPTIONS-FILE := $config:DBA-DIR || '.dba.xml';

(:~ Default options. :)
declare %basex:lazy %private variable $config:DEFAULTS := {
  $config:MAXCHARS   : 1000000,
  $config:MAXROWS    : 1000,
  $config:TIMEOUT    : 30,
  $config:MEMORY     : 1000,
  $config:PERMISSION : 'admin',
  $config:IGNORE-LOGS: '',
  $config:INDENT     : 'no'
};

(:~ Currently assigned options. :)
declare %basex:lazy %private variable $config:OPTIONS := (
  if(file:exists($config:OPTIONS-FILE)) then (
    try {
      (: merge defaults with saved options :)
      let $options := fetch:doc($config:OPTIONS-FILE)/options
      return map:merge(
        map:for-each($config:DEFAULTS, fn($key, $value) {
          map:entry($key,
            let $option := $options/*[name() = $key]
            return if($option) then (
              typeswitch($value) {
                case xs:numeric  return xs:integer($option)
                case xs:boolean  return xs:boolean($option)
                default          return xs:string($option)
              }
            ) else (
              $value
            )
          )
        })
      )
    } catch * {
      (: use defaults if an error occurs while parsing the options :)
      $config:DEFAULTS
    }
  ) else (
    $config:DEFAULTS
  )
);

(:~
 : Returns the value of an option.
 : @param  $name  name of option
 : @return value
 :)
declare function config:get(
  $name  as xs:string
) as xs:anyAtomicType {
  $config:OPTIONS($name)
};

(:~
 : Saves options.
 : @param  $options  keys/values that have been changed
 :)
declare function config:save(
  $options  as map(*)
) as empty-sequence() {
  file:write($config:OPTIONS-FILE, element options {
    map:for-each($config:DEFAULTS, fn($key, $value) {
      element { $key } { $options($key) otherwise $value }
    })
  })
};

(:~
 : Returns the current files directory.
 : @return directory
 :)
declare function config:files-dir() as xs:string {
  session:get($config:FILES-DIR) otherwise $config:DBA-DIR
};

(:~
 : Assigns the current files directory.
 : @param  $dir  directory
 :)
declare function config:set-files-dir(
  $dir  as xs:string
) as empty-sequence() {
  session:set($config:FILES-DIR, $dir)
};

(:~
 : Returns the current editor directory.
 : @return directory
 :)
declare function config:editor-dir() as xs:string {
  (config:edited-file()[.] ! file:parent(.)) otherwise config:files-dir()
};

(:~
 : Returns the currently edited file.
 : @return file path
 :)
declare function config:edited-file() as xs:string? {
  session:get($config:EDITED-FILE)
};

(:~
 : Assigns the currently edited file.
 : @param  $path  file path
 :)
declare function config:set-edited-file(
  $path  as xs:string
) as empty-sequence() {
  session:set($config:EDITED-FILE, $path)
};

(:~
 : Closes the currently edited file.
 :)
declare function config:close-edited-file() as empty-sequence() {
  session:delete($config:EDITED-FILE)
};

(:~
 : Returns the names of all files that can be opened in the editor.
 : @return list of files
 :)
declare function config:editor-files() as xs:string* {
  let $limit := config:get($config:MAXCHARS)
  for $file in file:children(config:editor-dir())
  where file:is-file($file) and file:size($file) <= $limit
  return file:name($file)
};
