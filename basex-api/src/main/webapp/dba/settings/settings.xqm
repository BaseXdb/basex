(:~
 : Settings page.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/settings';

import module namespace Request = 'http://exquery.org/ns/request';
import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../modules/tmpl.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'settings';

(:~
 : Settings page.
 :)
declare
  %rest:GET
  %rest:path("/dba/settings")
  %output:method("html")
function dba:settings(
) as element() {
  cons:check(),

  let $system := html:properties(db:system())
  return tmpl:wrap(map { 'top': $dba:CAT },
    <tr>
      <td width='32%'>
        <form action="settings" method="post">
          <h2>Settings » <button>Save</button></h2>
          <h3>Querying</h3>
          <table>
            {
              dba:input($cons:K-TIMEOUT, 'Timeout, in seconds (0 = disabled)'),
              dba:input($cons:K-MEMORY, 'Memory limit, in MB (0 = disabled)'),
              dba:input($cons:K-MAXCHARS, 'Maximum output size')
            }
            <tr>
              <td colspan='2'>Permission:</td>
            </tr>
            <tr>
              <td>
                <select name="permission">{
                  let $pm := $cons:OPTION($cons:K-PERMISSION)
                  for $p in $cons:PERMISSIONS
                  return element option { attribute selected { }[$p = $pm], $p }
                }</select>
              </td>
            </tr>
          </table>
          <h3>Tables</h3>
          <table>
            { dba:input($cons:K-MAXROWS,  'Displayed table rows') }
          </table>
        </form>
      </td>
      <td class='vertical'/>
      <td width='32%'>
        <h2>Global Options</h2>
        <table>{
          $system/tr[th][3]/preceding-sibling::tr[not(th)]
        }</table>
      </td>
      <td class='vertical'/>
      <td width='32%'>
        <h2>Local Options</h2>
        <table>{
          $system/tr[th][3]/following-sibling::tr
        }</table>
      </td>
    </tr>
  )
};

(:~
 : Returns a text input component.
 : @param  $key    key
 : @param  $label  label
 :)
declare %private function dba:input(
  $key    as xs:string,
  $value  as xs:string
) as element(tr)* {
  <tr>
    <td>
      { $value }:<br/>
      <input name="{ $key }" type="number" value="{ $cons:OPTION($key) }"/>
    </td>
  </tr>
};

(:~
 : Saves the settings.
 :)
declare
  %rest:POST
  %rest:path("/dba/settings")
  %output:method("html")
function dba:settings-save(
) as element(rest:response) {
  cons:check(),

  let $config := element config {
    for $key in Request:parameter-names()
    return element { $key } { Request:parameter($key) }
  }
  return (
    file:create-dir($cons:DBA-DIR),
    file:write($cons:DBA-SETTINGS-FILE, $config),
    web:redirect("settings")
  )
};
