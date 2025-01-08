(:~
 : Put resources.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/databases';

import module namespace html = 'dba/html' at '../../lib/html.xqm';
import module namespace utils = 'dba/utils' at '../../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';
(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : Puts resources.
 : @param  $name    name of resource
 : @param  $opts    chosen parsing options
 : @param  $path    database path
 : @param  $file    uploaded file
 : @param  $binary  store as binary
 : @param  $do      perform update
 : @return form or redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/db-put')
  %rest:form-param('name',   '{$name}')
  %rest:form-param('opts',   '{$opts}')
  %rest:form-param('path',   '{$path}')
  %rest:form-param('file',   '{$file}')
  %rest:form-param('binary', '{$binary}')
  %rest:form-param('do',      '{$do}')
  %output:method('html')
  %output:html-version('5')
function dba:db-put(
  $name    as xs:string,
  $opts    as xs:string*,
  $path    as xs:string?,
  $file    as item()?,
  $binary  as xs:string?,
  $do      as xs:string?
) {
  html:update($do, { 'header': ($dba:CAT, $name) }, fn() {
    <tr>
      <td>
        <form method='post' enctype='multipart/form-data' autocomplete='off'>
          <input type='hidden' name='do' value='do'/>
          <input type='hidden' name='name' value='{ $name }'/>
          <h2>{
            html:link('Databases', $dba:CAT), ' » ',
            html:link($name, $dba:SUB, { 'name': $name }), ' » ',
            html:button('db-put', 'Put')
          }</h2>
          <table>
            <tr>
              <td>Input:</td>
              <td>{
                <input type='file' name='file' id='file'/>
              }</td>
            </tr>
            <tr>
              <td>Database Path:</td>
              <td>
                <input type='text' name='path' value='{ $path }'/>
              </td>
            </tr>
            <tr>
              <td>Binary Storage:</td>
              <td>{ html:checkbox('binary', 'true', $binary = 'true', '') }</td>
            </tr>
            <tr>
              <td colspan='2'>{
                <h3>Parsing Options</h3>,
                let $opts := if ($do) then $opts else ''
                return (
                  html:option('intparse', 'Use internal XML parser', $opts),
                  html:option('dtd', 'Parse DTDs and entities', $opts),
                  html:option('stripns', 'Strip namespaces', $opts),
                  html:option('stripws', 'Strip whitespace', $opts),
                  html:option('xinclude', 'Use XInclude', $opts)
                )
              }</td>
            </tr>
          </table>
        </form>
      </td>
    </tr>
  }, fn() {
    let $key := $file[. instance of map(*)] ! map:keys(.)
    let $path := if (not($path) or ends-with($path, '/')) { $path || $key } else { $path }
    return if (not($key)) {
      error((), 'No input specified.')
    } else {
      let $input := $file($key)
      return if ($binary) {
        db:put-binary($name, $input, $path)
      } else {
        db:put($name, fetch:binary-doc($input), $path, map:merge(
          ('intparse', 'dtd', 'stripns', 'stripws', 'xinclude') ! map:entry(., $opts = .))
        )
      },
      utils:redirect($dba:SUB,
        { 'name': $name, 'path': $path, 'info': 'Resource was added or updated.' }
      )
    }
  })
};
