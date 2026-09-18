(:~
 : DBA configuration.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace config = 'dba/lib/config';

(:~ Session key. :)
declare variable $config:SESSION-KEY := 'dba';

(:~ DBA directory; the file panel starts here if the client has no directory of its own. :)
declare variable $config:DBA-DIR := (
  let $dir := db:option('dbpath') || '/.dba'
  return (
    if (not(file:exists($dir))) { file:create-dir($dir) },
    file:path-to-native($dir)
  )
);

(:~ Permission values. :)
declare variable $config:PERMISSIONS := ('none', 'read', 'write', 'create', 'admin');

(:~ Views, in the order in which the navigation offers them. :)
(: a view is named by the path of its page, which is also the label of its entry :)
declare variable $config:VIEWS := (
  'workspace', 'databases', 'stores', 'users', 'logs', 'activity', 'settings'
);

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
(:~ Delay between two requests of a live view, in seconds. :)
declare variable $config:INTERVAL := 'interval';

(:~ Options file. :)
declare %private variable $config:OPTIONS-FILE := $config:DBA-DIR || '.dba.xml';

(:~ Default options. :)
declare %private variable $config:DEFAULTS := {
  $config:MAXCHARS   : 1_000_000,
  $config:MAXROWS    : 100,
  $config:TIMEOUT    : 60,
  $config:MEMORY     : 8_000,
  $config:PERMISSION : 'admin',
  $config:INTERVAL   : 1
};

(:~ Currently assigned options. :)
declare %basex:lazy %private variable $config:OPTIONS := (
  if (file:exists($config:OPTIONS-FILE)) then (
    try {
      (: merge defaults with saved options :)
      let $options := fetch:doc($config:OPTIONS-FILE)/options
      return {
        for key $key value $value in $config:DEFAULTS
        let $option := $options/*[name() = $key]
        return { $key: if (not($option)) then $value
          else if ($value instance of xs:numeric) then xs:integer($option)
          else string($option) }
      }
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
  $config:OPTIONS?$name
};

(:~
 : Saves options.
 : @param  $options  keys/values that have been changed
 :)
declare function config:save(
  $options  as map(*)
) as empty-sequence() {
  file:write($config:OPTIONS-FILE, element options {
    for key $key value $value in $config:DEFAULTS
    return element { $key } { $options?$key otherwise $value }
  })
};

(:~
 : Resolves the directory of the file panel.
 : @param  $dir  directory supplied by the client (empty: use the default)
 : @return existing directory, in native notation
 :)
declare function config:files-dir(
  $dir  as xs:string?
) as xs:string {
  (: the client remembers the directory and supplies it with every request; a relative step
     ('sub', '..') is appended to the path it sends :)
  let $path := file:path-to-native(file:resolve-path(($dir[.] otherwise $config:DBA-DIR) || '/'))
  (: ensure that the directory can be accessed :)
  return (void(file:list($path)), $path)
};

