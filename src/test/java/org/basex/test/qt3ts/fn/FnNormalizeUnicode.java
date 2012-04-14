package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the normalize-unicode() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnNormalizeUnicode extends QT3TestSet {

  /**
   *  A test whose essence is: `normalize-unicode()`. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc1() {
    final XQuery query = new XQuery(
      "normalize-unicode()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `normalize-unicode("foo", "") eq "foo"`. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc10() {
    final XQuery query = new XQuery(
      "normalize-unicode(\"foo\", \"\") eq \"foo\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `normalize-unicode("f oo") eq "f oo"`. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc11() {
    final XQuery query = new XQuery(
      "normalize-unicode(\"f oo\") eq \"f oo\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `normalize-unicode("f oo", "NFC") eq "f oo"`. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc12() {
    final XQuery query = new XQuery(
      "normalize-unicode(\"f oo\", \"NFC\") eq \"f oo\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Static typing implementations may raise XPTY0004. See http://www.w3.org/Bugs/Public/show_bug.cgi?id=4551 for details. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc13() {
    final XQuery query = new XQuery(
      "normalize-unicode((\"a string\", error()), \"NFC\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("FOER0000")
      ||
        error("XPTY0004")
      )
    );
  }

  /**
   *  A test whose essence is: `normalize-unicode("a string", "NFC", "wrong param")`. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc2() {
    final XQuery query = new XQuery(
      "normalize-unicode(\"a string\", \"NFC\", \"wrong param\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `normalize-unicode("a string", "example.com/notSupported/")`. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc3() {
    final XQuery query = new XQuery(
      "normalize-unicode(\"a string\", \"example.com/notSupported/\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOCH0003")
    );
  }

  /**
   *  A test whose essence is: `normalize-unicode("foo") eq "foo"`. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc4() {
    final XQuery query = new XQuery(
      "normalize-unicode(\"foo\") eq \"foo\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `normalize-unicode("foo", "NFC") eq "foo"`. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc5() {
    final XQuery query = new XQuery(
      "normalize-unicode(\"foo\", \"NFC\") eq \"foo\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `normalize-unicode("foo", "NFD") eq "foo"`. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc6() {
    final XQuery query = new XQuery(
      "normalize-unicode(\"foo\", \"NFD\") eq \"foo\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `normalize-unicode("foo", "NFKD") eq "foo"`. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc7() {
    final XQuery query = new XQuery(
      "normalize-unicode(\"foo\", \"NFKD\") eq \"foo\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `normalize-unicode("foo", "NFKC") eq "foo"`. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc8() {
    final XQuery query = new XQuery(
      "normalize-unicode(\"foo\", \"NFKC\") eq \"foo\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `normalize-unicode("f oo", "") eq "f oo"`. .
   */
  @org.junit.Test
  public void kNormalizeUnicodeFunc9() {
    final XQuery query = new XQuery(
      "normalize-unicode(\"f oo\", \"\") eq \"f oo\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Evaluation of fn:normalize-unicode to ensure that " NFC " is the same as "NFC". .
   */
  @org.junit.Test
  public void fnNormalizeUnicode1() {
    final XQuery query = new XQuery(
      "fn:concat(fn:normalize-unicode(\"Å\",\" NFC \"),fn:normalize-unicode(\"Å\",\"NFC\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ÅÅ")
    );
  }

  /**
   *  Evaluation of fn:normalize-unicode with noncharacters in argument .
   */
  @org.junit.Test
  public void fnNormalizeUnicode10() {
    final XQuery query = new XQuery(
      "string-to-codepoints(fn:normalize-unicode('e﷐̂'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("101, 64976, 770")
    );
  }

  /**
   *  Bug 7935: normalize-unicode() applied to unassigned codepoints 
   *         Result should be empty, indicating that normalization is idempotent on these 
   *         characters derived from the Unicode 5.2 database - essentially a list of characters 
   *         that are legal in XML but undefined in Unicode 5.2 .
   */
  @org.junit.Test
  public void fnNormalizeUnicode11() {
    final XQuery query = new XQuery(
      "(: Name: fn-normalize-unicode-11 :)\n" +
      "(: Description: Bug 7935: normalize-unicode() applied to unassigned codepoints :)\n" +
      "(: Result should be empty, indicating that normalization is idempotent on these characters :)\n" +
      "\n" +
      "declare variable $Cn := \n" +
      "   (: derived from the Unicode 5.2 database - essentially a list of characters that are legal in XML but undefined in Unicode 5.2 :)\n" +
      "   <Cn>\n" +
      "      <range from=\"888\" to=\"889\"/>\n" +
      "      <range from=\"895\" to=\"899\"/>\n" +
      "      <range from=\"907\" to=\"907\"/>\n" +
      "      <range from=\"909\" to=\"909\"/>\n" +
      "      <range from=\"930\" to=\"930\"/>\n" +
      "      <range from=\"1318\" to=\"1328\"/>\n" +
      "      <range from=\"1367\" to=\"1368\"/>\n" +
      "      <range from=\"1376\" to=\"1376\"/>\n" +
      "      <range from=\"1416\" to=\"1416\"/>\n" +
      "      <range from=\"1419\" to=\"1424\"/>\n" +
      "      <range from=\"1480\" to=\"1487\"/>\n" +
      "      <range from=\"1515\" to=\"1519\"/>\n" +
      "      <range from=\"1525\" to=\"1535\"/>\n" +
      "      <range from=\"1540\" to=\"1541\"/>\n" +
      "      <range from=\"1564\" to=\"1565\"/>\n" +
      "      <range from=\"1568\" to=\"1568\"/>\n" +
      "      <range from=\"1631\" to=\"1631\"/>\n" +
      "      <range from=\"1806\" to=\"1806\"/>\n" +
      "      <range from=\"1867\" to=\"1868\"/>\n" +
      "      <range from=\"1970\" to=\"1983\"/>\n" +
      "      <range from=\"2043\" to=\"2047\"/>\n" +
      "      <range from=\"2094\" to=\"2095\"/>\n" +
      "      <range from=\"2111\" to=\"2303\"/>\n" +
      "      <range from=\"2362\" to=\"2363\"/>\n" +
      "      <range from=\"2383\" to=\"2383\"/>\n" +
      "      <range from=\"2390\" to=\"2391\"/>\n" +
      "      <range from=\"2419\" to=\"2424\"/>\n" +
      "      <range from=\"2432\" to=\"2432\"/>\n" +
      "      <range from=\"2436\" to=\"2436\"/>\n" +
      "      <range from=\"2445\" to=\"2446\"/>\n" +
      "      <range from=\"2449\" to=\"2450\"/>\n" +
      "      <range from=\"2473\" to=\"2473\"/>\n" +
      "      <range from=\"2481\" to=\"2481\"/>\n" +
      "      <range from=\"2483\" to=\"2485\"/>\n" +
      "      <range from=\"2490\" to=\"2491\"/>\n" +
      "      <range from=\"2501\" to=\"2502\"/>\n" +
      "      <range from=\"2505\" to=\"2506\"/>\n" +
      "      <range from=\"2511\" to=\"2518\"/>\n" +
      "      <range from=\"2520\" to=\"2523\"/>\n" +
      "      <range from=\"2526\" to=\"2526\"/>\n" +
      "      <range from=\"2532\" to=\"2533\"/>\n" +
      "      <range from=\"2556\" to=\"2560\"/>\n" +
      "      <range from=\"2564\" to=\"2564\"/>\n" +
      "      <range from=\"2571\" to=\"2574\"/>\n" +
      "      <range from=\"2577\" to=\"2578\"/>\n" +
      "      <range from=\"2601\" to=\"2601\"/>\n" +
      "      <range from=\"2609\" to=\"2609\"/>\n" +
      "      <range from=\"2612\" to=\"2612\"/>\n" +
      "      <range from=\"2615\" to=\"2615\"/>\n" +
      "      <range from=\"2618\" to=\"2619\"/>\n" +
      "      <range from=\"2621\" to=\"2621\"/>\n" +
      "      <range from=\"2627\" to=\"2630\"/>\n" +
      "      <range from=\"2633\" to=\"2634\"/>\n" +
      "      <range from=\"2638\" to=\"2640\"/>\n" +
      "      <range from=\"2642\" to=\"2648\"/>\n" +
      "      <range from=\"2653\" to=\"2653\"/>\n" +
      "      <range from=\"2655\" to=\"2661\"/>\n" +
      "      <range from=\"2678\" to=\"2688\"/>\n" +
      "      <range from=\"2692\" to=\"2692\"/>\n" +
      "      <range from=\"2702\" to=\"2702\"/>\n" +
      "      <range from=\"2706\" to=\"2706\"/>\n" +
      "      <range from=\"2729\" to=\"2729\"/>\n" +
      "      <range from=\"2737\" to=\"2737\"/>\n" +
      "      <range from=\"2740\" to=\"2740\"/>\n" +
      "      <range from=\"2746\" to=\"2747\"/>\n" +
      "      <range from=\"2758\" to=\"2758\"/>\n" +
      "      <range from=\"2762\" to=\"2762\"/>\n" +
      "      <range from=\"2766\" to=\"2767\"/>\n" +
      "      <range from=\"2769\" to=\"2783\"/>\n" +
      "      <range from=\"2788\" to=\"2789\"/>\n" +
      "      <range from=\"2800\" to=\"2800\"/>\n" +
      "      <range from=\"2802\" to=\"2816\"/>\n" +
      "      <range from=\"2820\" to=\"2820\"/>\n" +
      "      <range from=\"2829\" to=\"2830\"/>\n" +
      "      <range from=\"2833\" to=\"2834\"/>\n" +
      "      <range from=\"2857\" to=\"2857\"/>\n" +
      "      <range from=\"2865\" to=\"2865\"/>\n" +
      "      <range from=\"2868\" to=\"2868\"/>\n" +
      "      <range from=\"2874\" to=\"2875\"/>\n" +
      "      <range from=\"2885\" to=\"2886\"/>\n" +
      "      <range from=\"2889\" to=\"2890\"/>\n" +
      "      <range from=\"2894\" to=\"2901\"/>\n" +
      "      <range from=\"2904\" to=\"2907\"/>\n" +
      "      <range from=\"2910\" to=\"2910\"/>\n" +
      "      <range from=\"2916\" to=\"2917\"/>\n" +
      "      <range from=\"2930\" to=\"2945\"/>\n" +
      "      <range from=\"2948\" to=\"2948\"/>\n" +
      "      <range from=\"2955\" to=\"2957\"/>\n" +
      "      <range from=\"2961\" to=\"2961\"/>\n" +
      "      <range from=\"2966\" to=\"2968\"/>\n" +
      "      <range from=\"2971\" to=\"2971\"/>\n" +
      "      <range from=\"2973\" to=\"2973\"/>\n" +
      "      <range from=\"2976\" to=\"2978\"/>\n" +
      "      <range from=\"2981\" to=\"2983\"/>\n" +
      "      <range from=\"2987\" to=\"2989\"/>\n" +
      "      <range from=\"3002\" to=\"3005\"/>\n" +
      "      <range from=\"3011\" to=\"3013\"/>\n" +
      "      <range from=\"3017\" to=\"3017\"/>\n" +
      "      <range from=\"3022\" to=\"3023\"/>\n" +
      "      <range from=\"3025\" to=\"3030\"/>\n" +
      "      <range from=\"3032\" to=\"3045\"/>\n" +
      "      <range from=\"3067\" to=\"3072\"/>\n" +
      "      <range from=\"3076\" to=\"3076\"/>\n" +
      "      <range from=\"3085\" to=\"3085\"/>\n" +
      "      <range from=\"3089\" to=\"3089\"/>\n" +
      "      <range from=\"3113\" to=\"3113\"/>\n" +
      "      <range from=\"3124\" to=\"3124\"/>\n" +
      "      <range from=\"3130\" to=\"3132\"/>\n" +
      "      <range from=\"3141\" to=\"3141\"/>\n" +
      "      <range from=\"3145\" to=\"3145\"/>\n" +
      "      <range from=\"3150\" to=\"3156\"/>\n" +
      "      <range from=\"3159\" to=\"3159\"/>\n" +
      "      <range from=\"3162\" to=\"3167\"/>\n" +
      "      <range from=\"3172\" to=\"3173\"/>\n" +
      "      <range from=\"3184\" to=\"3191\"/>\n" +
      "      <range from=\"3200\" to=\"3201\"/>\n" +
      "      <range from=\"3204\" to=\"3204\"/>\n" +
      "      <range from=\"3213\" to=\"3213\"/>\n" +
      "      <range from=\"3217\" to=\"3217\"/>\n" +
      "      <range from=\"3241\" to=\"3241\"/>\n" +
      "      <range from=\"3252\" to=\"3252\"/>\n" +
      "      <range from=\"3258\" to=\"3259\"/>\n" +
      "      <range from=\"3269\" to=\"3269\"/>\n" +
      "      <range from=\"3273\" to=\"3273\"/>\n" +
      "      <range from=\"3278\" to=\"3284\"/>\n" +
      "      <range from=\"3287\" to=\"3293\"/>\n" +
      "      <range from=\"3295\" to=\"3295\"/>\n" +
      "      <range from=\"3300\" to=\"3301\"/>\n" +
      "      <range from=\"3312\" to=\"3312\"/>\n" +
      "      <range from=\"3315\" to=\"3329\"/>\n" +
      "      <range from=\"3332\" to=\"3332\"/>\n" +
      "      <range from=\"3341\" to=\"3341\"/>\n" +
      "      <range from=\"3345\" to=\"3345\"/>\n" +
      "      <range from=\"3369\" to=\"3369\"/>\n" +
      "      <range from=\"3386\" to=\"3388\"/>\n" +
      "      <range from=\"3397\" to=\"3397\"/>\n" +
      "      <range from=\"3401\" to=\"3401\"/>\n" +
      "      <range from=\"3406\" to=\"3414\"/>\n" +
      "      <range from=\"3416\" to=\"3423\"/>\n" +
      "      <range from=\"3428\" to=\"3429\"/>\n" +
      "      <range from=\"3446\" to=\"3448\"/>\n" +
      "      <range from=\"3456\" to=\"3457\"/>\n" +
      "      <range from=\"3460\" to=\"3460\"/>\n" +
      "      <range from=\"3479\" to=\"3481\"/>\n" +
      "      <range from=\"3506\" to=\"3506\"/>\n" +
      "      <range from=\"3516\" to=\"3516\"/>\n" +
      "      <range from=\"3518\" to=\"3519\"/>\n" +
      "      <range from=\"3527\" to=\"3529\"/>\n" +
      "      <range from=\"3531\" to=\"3534\"/>\n" +
      "      <range from=\"3541\" to=\"3541\"/>\n" +
      "      <range from=\"3543\" to=\"3543\"/>\n" +
      "      <range from=\"3552\" to=\"3569\"/>\n" +
      "      <range from=\"3573\" to=\"3584\"/>\n" +
      "      <range from=\"3643\" to=\"3646\"/>\n" +
      "      <range from=\"3676\" to=\"3712\"/>\n" +
      "      <range from=\"3715\" to=\"3715\"/>\n" +
      "      <range from=\"3717\" to=\"3718\"/>\n" +
      "      <range from=\"3721\" to=\"3721\"/>\n" +
      "      <range from=\"3723\" to=\"3724\"/>\n" +
      "      <range from=\"3726\" to=\"3731\"/>\n" +
      "      <range from=\"3736\" to=\"3736\"/>\n" +
      "      <range from=\"3744\" to=\"3744\"/>\n" +
      "      <range from=\"3748\" to=\"3748\"/>\n" +
      "      <range from=\"3750\" to=\"3750\"/>\n" +
      "      <range from=\"3752\" to=\"3753\"/>\n" +
      "      <range from=\"3756\" to=\"3756\"/>\n" +
      "      <range from=\"3770\" to=\"3770\"/>\n" +
      "      <range from=\"3774\" to=\"3775\"/>\n" +
      "      <range from=\"3781\" to=\"3781\"/>\n" +
      "      <range from=\"3783\" to=\"3783\"/>\n" +
      "      <range from=\"3790\" to=\"3791\"/>\n" +
      "      <range from=\"3802\" to=\"3803\"/>\n" +
      "      <range from=\"3806\" to=\"3839\"/>\n" +
      "      <range from=\"3912\" to=\"3912\"/>\n" +
      "      <range from=\"3949\" to=\"3952\"/>\n" +
      "      <range from=\"3980\" to=\"3983\"/>\n" +
      "      <range from=\"3992\" to=\"3992\"/>\n" +
      "      <range from=\"4029\" to=\"4029\"/>\n" +
      "      <range from=\"4045\" to=\"4045\"/>\n" +
      "      <range from=\"4057\" to=\"4095\"/>\n" +
      "      <range from=\"4294\" to=\"4303\"/>\n" +
      "      <range from=\"4349\" to=\"4351\"/>\n" +
      "      <range from=\"4681\" to=\"4681\"/>\n" +
      "      <range from=\"4686\" to=\"4687\"/>\n" +
      "      <range from=\"4695\" to=\"4695\"/>\n" +
      "      <range from=\"4697\" to=\"4697\"/>\n" +
      "      <range from=\"4702\" to=\"4703\"/>\n" +
      "      <range from=\"4745\" to=\"4745\"/>\n" +
      "      <range from=\"4750\" to=\"4751\"/>\n" +
      "      <range from=\"4785\" to=\"4785\"/>\n" +
      "      <range from=\"4790\" to=\"4791\"/>\n" +
      "      <range from=\"4799\" to=\"4799\"/>\n" +
      "      <range from=\"4801\" to=\"4801\"/>\n" +
      "      <range from=\"4806\" to=\"4807\"/>\n" +
      "      <range from=\"4823\" to=\"4823\"/>\n" +
      "      <range from=\"4881\" to=\"4881\"/>\n" +
      "      <range from=\"4886\" to=\"4887\"/>\n" +
      "      <range from=\"4955\" to=\"4958\"/>\n" +
      "      <range from=\"4989\" to=\"4991\"/>\n" +
      "      <range from=\"5018\" to=\"5023\"/>\n" +
      "      <range from=\"5109\" to=\"5119\"/>\n" +
      "      <range from=\"5789\" to=\"5791\"/>\n" +
      "      <range from=\"5873\" to=\"5887\"/>\n" +
      "      <range from=\"5901\" to=\"5901\"/>\n" +
      "      <range from=\"5909\" to=\"5919\"/>\n" +
      "      <range from=\"5943\" to=\"5951\"/>\n" +
      "      <range from=\"5972\" to=\"5983\"/>\n" +
      "      <range from=\"5997\" to=\"5997\"/>\n" +
      "      <range from=\"6001\" to=\"6001\"/>\n" +
      "      <range from=\"6004\" to=\"6015\"/>\n" +
      "      <range from=\"6110\" to=\"6111\"/>\n" +
      "      <range from=\"6122\" to=\"6127\"/>\n" +
      "      <range from=\"6138\" to=\"6143\"/>\n" +
      "      <range from=\"6159\" to=\"6159\"/>\n" +
      "      <range from=\"6170\" to=\"6175\"/>\n" +
      "      <range from=\"6264\" to=\"6271\"/>\n" +
      "      <range from=\"6315\" to=\"6319\"/>\n" +
      "      <range from=\"6390\" to=\"6399\"/>\n" +
      "      <range from=\"6429\" to=\"6431\"/>\n" +
      "      <range from=\"6444\" to=\"6447\"/>\n" +
      "      <range from=\"6460\" to=\"6463\"/>\n" +
      "      <range from=\"6465\" to=\"6467\"/>\n" +
      "      <range from=\"6510\" to=\"6511\"/>\n" +
      "      <range from=\"6517\" to=\"6527\"/>\n" +
      "      <range from=\"6572\" to=\"6575\"/>\n" +
      "      <range from=\"6602\" to=\"6607\"/>\n" +
      "      <range from=\"6619\" to=\"6621\"/>\n" +
      "      <range from=\"6684\" to=\"6685\"/>\n" +
      "      <range from=\"6751\" to=\"6751\"/>\n" +
      "      <range from=\"6781\" to=\"6782\"/>\n" +
      "      <range from=\"6794\" to=\"6799\"/>\n" +
      "      <range from=\"6810\" to=\"6815\"/>\n" +
      "      <range from=\"6830\" to=\"6911\"/>\n" +
      "      <range from=\"6988\" to=\"6991\"/>\n" +
      "      <range from=\"7037\" to=\"7039\"/>\n" +
      "      <range from=\"7083\" to=\"7085\"/>\n" +
      "      <range from=\"7098\" to=\"7167\"/>\n" +
      "      <range from=\"7224\" to=\"7226\"/>\n" +
      "      <range from=\"7242\" to=\"7244\"/>\n" +
      "      <range from=\"7296\" to=\"7375\"/>\n" +
      "      <range from=\"7411\" to=\"7423\"/>\n" +
      "      <range from=\"7655\" to=\"7676\"/>\n" +
      "      <range from=\"7958\" to=\"7959\"/>\n" +
      "      <range from=\"7966\" to=\"7967\"/>\n" +
      "      <range from=\"8006\" to=\"8007\"/>\n" +
      "      <range from=\"8014\" to=\"8015\"/>\n" +
      "      <range from=\"8024\" to=\"8024\"/>\n" +
      "      <range from=\"8026\" to=\"8026\"/>\n" +
      "      <range from=\"8028\" to=\"8028\"/>\n" +
      "      <range from=\"8030\" to=\"8030\"/>\n" +
      "      <range from=\"8062\" to=\"8063\"/>\n" +
      "      <range from=\"8117\" to=\"8117\"/>\n" +
      "      <range from=\"8133\" to=\"8133\"/>\n" +
      "      <range from=\"8148\" to=\"8149\"/>\n" +
      "      <range from=\"8156\" to=\"8156\"/>\n" +
      "      <range from=\"8176\" to=\"8177\"/>\n" +
      "      <range from=\"8181\" to=\"8181\"/>\n" +
      "      <range from=\"8191\" to=\"8191\"/>\n" +
      "      <range from=\"8293\" to=\"8297\"/>\n" +
      "      <range from=\"8306\" to=\"8307\"/>\n" +
      "      <range from=\"8335\" to=\"8335\"/>\n" +
      "      <range from=\"8341\" to=\"8351\"/>\n" +
      "      <range from=\"8377\" to=\"8399\"/>\n" +
      "      <range from=\"8433\" to=\"8447\"/>\n" +
      "      <range from=\"8586\" to=\"8591\"/>\n" +
      "      <range from=\"9193\" to=\"9215\"/>\n" +
      "      <range from=\"9255\" to=\"9279\"/>\n" +
      "      <range from=\"9291\" to=\"9311\"/>\n" +
      "      <range from=\"9934\" to=\"9934\"/>\n" +
      "      <range from=\"9954\" to=\"9954\"/>\n" +
      "      <range from=\"9956\" to=\"9959\"/>\n" +
      "      <range from=\"9984\" to=\"9984\"/>\n" +
      "      <range from=\"9989\" to=\"9989\"/>\n" +
      "      <range from=\"9994\" to=\"9995\"/>\n" +
      "      <range from=\"10024\" to=\"10024\"/>\n" +
      "      <range from=\"10060\" to=\"10060\"/>\n" +
      "      <range from=\"10062\" to=\"10062\"/>\n" +
      "      <range from=\"10067\" to=\"10069\"/>\n" +
      "      <range from=\"10079\" to=\"10080\"/>\n" +
      "      <range from=\"10133\" to=\"10135\"/>\n" +
      "      <range from=\"10160\" to=\"10160\"/>\n" +
      "      <range from=\"10175\" to=\"10175\"/>\n" +
      "      <range from=\"10187\" to=\"10187\"/>\n" +
      "      <range from=\"10189\" to=\"10191\"/>\n" +
      "      <range from=\"11085\" to=\"11087\"/>\n" +
      "      <range from=\"11098\" to=\"11263\"/>\n" +
      "      <range from=\"11311\" to=\"11311\"/>\n" +
      "      <range from=\"11359\" to=\"11359\"/>\n" +
      "      <range from=\"11506\" to=\"11512\"/>\n" +
      "      <range from=\"11558\" to=\"11567\"/>\n" +
      "      <range from=\"11622\" to=\"11630\"/>\n" +
      "      <range from=\"11632\" to=\"11647\"/>\n" +
      "      <range from=\"11671\" to=\"11679\"/>\n" +
      "      <range from=\"11687\" to=\"11687\"/>\n" +
      "      <range from=\"11695\" to=\"11695\"/>\n" +
      "      <range from=\"11703\" to=\"11703\"/>\n" +
      "      <range from=\"11711\" to=\"11711\"/>\n" +
      "      <range from=\"11719\" to=\"11719\"/>\n" +
      "      <range from=\"11727\" to=\"11727\"/>\n" +
      "      <range from=\"11735\" to=\"11735\"/>\n" +
      "      <range from=\"11743\" to=\"11743\"/>\n" +
      "      <range from=\"11826\" to=\"11903\"/>\n" +
      "      <range from=\"11930\" to=\"11930\"/>\n" +
      "      <range from=\"12020\" to=\"12031\"/>\n" +
      "      <range from=\"12246\" to=\"12271\"/>\n" +
      "      <range from=\"12284\" to=\"12287\"/>\n" +
      "      <range from=\"12352\" to=\"12352\"/>\n" +
      "      <range from=\"12439\" to=\"12440\"/>\n" +
      "      <range from=\"12544\" to=\"12548\"/>\n" +
      "      <range from=\"12590\" to=\"12592\"/>\n" +
      "      <range from=\"12687\" to=\"12687\"/>\n" +
      "      <range from=\"12728\" to=\"12735\"/>\n" +
      "      <range from=\"12772\" to=\"12783\"/>\n" +
      "      <range from=\"12831\" to=\"12831\"/>\n" +
      "      <range from=\"13055\" to=\"13055\"/>\n" +
      "      <range from=\"19894\" to=\"19903\"/>\n" +
      "      <range from=\"40908\" to=\"40959\"/>\n" +
      "      <range from=\"42125\" to=\"42127\"/>\n" +
      "      <range from=\"42183\" to=\"42191\"/>\n" +
      "      <range from=\"42540\" to=\"42559\"/>\n" +
      "      <range from=\"42592\" to=\"42593\"/>\n" +
      "      <range from=\"42612\" to=\"42619\"/>\n" +
      "      <range from=\"42648\" to=\"42655\"/>\n" +
      "      <range from=\"42744\" to=\"42751\"/>\n" +
      "      <range from=\"42893\" to=\"43002\"/>\n" +
      "      <range from=\"43052\" to=\"43055\"/>\n" +
      "      <range from=\"43066\" to=\"43071\"/>\n" +
      "      <range from=\"43128\" to=\"43135\"/>\n" +
      "      <range from=\"43205\" to=\"43213\"/>\n" +
      "      <range from=\"43226\" to=\"43231\"/>\n" +
      "      <range from=\"43260\" to=\"43263\"/>\n" +
      "      <range from=\"43348\" to=\"43358\"/>\n" +
      "      <range from=\"43389\" to=\"43391\"/>\n" +
      "      <range from=\"43470\" to=\"43470\"/>\n" +
      "      <range from=\"43482\" to=\"43485\"/>\n" +
      "      <range from=\"43488\" to=\"43519\"/>\n" +
      "      <range from=\"43575\" to=\"43583\"/>\n" +
      "      <range from=\"43598\" to=\"43599\"/>\n" +
      "      <range from=\"43610\" to=\"43611\"/>\n" +
      "      <range from=\"43644\" to=\"43647\"/>\n" +
      "      <range from=\"43715\" to=\"43738\"/>\n" +
      "      <range from=\"43744\" to=\"43967\"/>\n" +
      "      <range from=\"44014\" to=\"44015\"/>\n" +
      "      <range from=\"44026\" to=\"44031\"/>\n" +
      "      <range from=\"55204\" to=\"55215\"/>\n" +
      "      <range from=\"55239\" to=\"55242\"/>\n" +
      "      <range from=\"55292\" to=\"55295\"/>\n" +
      "      <range from=\"64046\" to=\"64047\"/>\n" +
      "      <range from=\"64110\" to=\"64111\"/>\n" +
      "      <range from=\"64218\" to=\"64255\"/>\n" +
      "      <range from=\"64263\" to=\"64274\"/>\n" +
      "      <range from=\"64280\" to=\"64284\"/>\n" +
      "      <range from=\"64311\" to=\"64311\"/>\n" +
      "      <range from=\"64317\" to=\"64317\"/>\n" +
      "      <range from=\"64319\" to=\"64319\"/>\n" +
      "      <range from=\"64322\" to=\"64322\"/>\n" +
      "      <range from=\"64325\" to=\"64325\"/>\n" +
      "      <range from=\"64434\" to=\"64466\"/>\n" +
      "      <range from=\"64832\" to=\"64847\"/>\n" +
      "      <range from=\"64912\" to=\"64913\"/>\n" +
      "      <range from=\"64968\" to=\"65007\"/>\n" +
      "      <range from=\"65022\" to=\"65023\"/>\n" +
      "      <range from=\"65050\" to=\"65055\"/>\n" +
      "      <range from=\"65063\" to=\"65071\"/>\n" +
      "      <range from=\"65107\" to=\"65107\"/>\n" +
      "      <range from=\"65127\" to=\"65127\"/>\n" +
      "      <range from=\"65132\" to=\"65135\"/>\n" +
      "      <range from=\"65141\" to=\"65141\"/>\n" +
      "      <range from=\"65277\" to=\"65278\"/>\n" +
      "      <range from=\"65280\" to=\"65280\"/>\n" +
      "      <range from=\"65471\" to=\"65473\"/>\n" +
      "      <range from=\"65480\" to=\"65481\"/>\n" +
      "      <range from=\"65488\" to=\"65489\"/>\n" +
      "      <range from=\"65496\" to=\"65497\"/>\n" +
      "      <range from=\"65501\" to=\"65503\"/>\n" +
      "      <range from=\"65511\" to=\"65511\"/>\n" +
      "      <range from=\"65519\" to=\"65528\"/>\n" +
      "      <range from=\"65548\" to=\"65548\"/>\n" +
      "      <range from=\"65575\" to=\"65575\"/>\n" +
      "      <range from=\"65595\" to=\"65595\"/>\n" +
      "      <range from=\"65598\" to=\"65598\"/>\n" +
      "      <range from=\"65614\" to=\"65615\"/>\n" +
      "      <range from=\"65630\" to=\"65663\"/>\n" +
      "      <range from=\"65787\" to=\"65791\"/>\n" +
      "      <range from=\"65795\" to=\"65798\"/>\n" +
      "      <range from=\"65844\" to=\"65846\"/>\n" +
      "      <range from=\"65931\" to=\"65935\"/>\n" +
      "      <range from=\"65948\" to=\"65999\"/>\n" +
      "      <range from=\"66046\" to=\"66175\"/>\n" +
      "      <range from=\"66205\" to=\"66207\"/>\n" +
      "      <range from=\"66257\" to=\"66303\"/>\n" +
      "      <range from=\"66335\" to=\"66335\"/>\n" +
      "      <range from=\"66340\" to=\"66351\"/>\n" +
      "      <range from=\"66379\" to=\"66431\"/>\n" +
      "      <range from=\"66462\" to=\"66462\"/>\n" +
      "      <range from=\"66500\" to=\"66503\"/>\n" +
      "      <range from=\"66518\" to=\"66559\"/>\n" +
      "      <range from=\"66718\" to=\"66719\"/>\n" +
      "      <range from=\"66730\" to=\"67583\"/>\n" +
      "      <range from=\"67590\" to=\"67591\"/>\n" +
      "      <range from=\"67593\" to=\"67593\"/>\n" +
      "      <range from=\"67638\" to=\"67638\"/>\n" +
      "      <range from=\"67641\" to=\"67643\"/>\n" +
      "      <range from=\"67645\" to=\"67646\"/>\n" +
      "      <range from=\"67670\" to=\"67670\"/>\n" +
      "      <range from=\"67680\" to=\"67839\"/>\n" +
      "      <range from=\"67868\" to=\"67870\"/>\n" +
      "      <range from=\"67898\" to=\"67902\"/>\n" +
      "      <range from=\"67904\" to=\"68095\"/>\n" +
      "      <range from=\"68100\" to=\"68100\"/>\n" +
      "      <range from=\"68103\" to=\"68107\"/>\n" +
      "      <range from=\"68116\" to=\"68116\"/>\n" +
      "      <range from=\"68120\" to=\"68120\"/>\n" +
      "      <range from=\"68148\" to=\"68151\"/>\n" +
      "      <range from=\"68155\" to=\"68158\"/>\n" +
      "      <range from=\"68168\" to=\"68175\"/>\n" +
      "      <range from=\"68185\" to=\"68191\"/>\n" +
      "      <range from=\"68224\" to=\"68351\"/>\n" +
      "      <range from=\"68406\" to=\"68408\"/>\n" +
      "      <range from=\"68438\" to=\"68439\"/>\n" +
      "      <range from=\"68467\" to=\"68471\"/>\n" +
      "      <range from=\"68480\" to=\"68607\"/>\n" +
      "      <range from=\"68681\" to=\"69215\"/>\n" +
      "      <range from=\"69247\" to=\"69759\"/>\n" +
      "      <range from=\"69826\" to=\"73727\"/>\n" +
      "      <range from=\"74607\" to=\"74751\"/>\n" +
      "      <range from=\"74851\" to=\"74863\"/>\n" +
      "      <range from=\"74868\" to=\"77823\"/>\n" +
      "      <range from=\"78895\" to=\"118783\"/>\n" +
      "      <range from=\"119030\" to=\"119039\"/>\n" +
      "      <range from=\"119079\" to=\"119080\"/>\n" +
      "      <range from=\"119262\" to=\"119295\"/>\n" +
      "      <range from=\"119366\" to=\"119551\"/>\n" +
      "      <range from=\"119639\" to=\"119647\"/>\n" +
      "      <range from=\"119666\" to=\"119807\"/>\n" +
      "      <range from=\"119893\" to=\"119893\"/>\n" +
      "      <range from=\"119965\" to=\"119965\"/>\n" +
      "      <range from=\"119968\" to=\"119969\"/>\n" +
      "      <range from=\"119971\" to=\"119972\"/>\n" +
      "      <range from=\"119975\" to=\"119976\"/>\n" +
      "      <range from=\"119981\" to=\"119981\"/>\n" +
      "      <range from=\"119994\" to=\"119994\"/>\n" +
      "      <range from=\"119996\" to=\"119996\"/>\n" +
      "      <range from=\"120004\" to=\"120004\"/>\n" +
      "      <range from=\"120070\" to=\"120070\"/>\n" +
      "      <range from=\"120075\" to=\"120076\"/>\n" +
      "      <range from=\"120085\" to=\"120085\"/>\n" +
      "      <range from=\"120093\" to=\"120093\"/>\n" +
      "      <range from=\"120122\" to=\"120122\"/>\n" +
      "      <range from=\"120127\" to=\"120127\"/>\n" +
      "      <range from=\"120133\" to=\"120133\"/>\n" +
      "      <range from=\"120135\" to=\"120137\"/>\n" +
      "      <range from=\"120145\" to=\"120145\"/>\n" +
      "      <range from=\"120486\" to=\"120487\"/>\n" +
      "      <range from=\"120780\" to=\"120781\"/>\n" +
      "      <range from=\"120832\" to=\"126975\"/>\n" +
      "      <range from=\"127020\" to=\"127023\"/>\n" +
      "      <range from=\"127124\" to=\"127231\"/>\n" +
      "      <range from=\"127243\" to=\"127247\"/>\n" +
      "      <range from=\"127279\" to=\"127280\"/>\n" +
      "      <range from=\"127282\" to=\"127292\"/>\n" +
      "      <range from=\"127294\" to=\"127294\"/>\n" +
      "      <range from=\"127296\" to=\"127297\"/>\n" +
      "      <range from=\"127299\" to=\"127301\"/>\n" +
      "      <range from=\"127303\" to=\"127305\"/>\n" +
      "      <range from=\"127311\" to=\"127318\"/>\n" +
      "      <range from=\"127320\" to=\"127326\"/>\n" +
      "      <range from=\"127328\" to=\"127352\"/>\n" +
      "      <range from=\"127354\" to=\"127354\"/>\n" +
      "      <range from=\"127357\" to=\"127358\"/>\n" +
      "      <range from=\"127360\" to=\"127369\"/>\n" +
      "      <range from=\"127374\" to=\"127375\"/>\n" +
      "      <range from=\"127377\" to=\"127487\"/>\n" +
      "      <range from=\"127489\" to=\"127503\"/>\n" +
      "      <range from=\"127538\" to=\"127551\"/>\n" +
      "      <range from=\"127561\" to=\"131071\"/>\n" +
      "      <range from=\"173783\" to=\"173823\"/>\n" +
      "      <range from=\"177973\" to=\"194559\"/>\n" +
      "      <range from=\"195102\" to=\"917504\"/>\n" +
      "      <range from=\"917506\" to=\"917535\"/>\n" +
      "      <range from=\"917632\" to=\"917759\"/>\n" +
      "      <range from=\"918000\" to=\"983039\"/>\n" +
      "      <range from=\"1048574\" to=\"1048575\"/>\n" +
      "   </Cn>;\n" +
      "\n" +
      "<out>{   \n" +
      "   let $codes := for $r in $Cn//range return (xs:integer($r/@from) to xs:integer($r/@to))\n" +
      "   for $code in $codes\n" +
      "   for $char in codepoints-to-string($code)\n" +
      "   where not(codepoint-equal(normalize-unicode($char, 'NFC'), $char))\n" +
      "         or not(codepoint-equal(normalize-unicode($char, 'NFD'), $char))\n" +
      "   return \n" +
      "   <norm char=\"{string-to-codepoints($char)}\"\n" +
      "         nfc=\"{string-to-codepoints(normalize-unicode($char, 'NFC'))}\"\n" +
      "         nfd=\"{string-to-codepoints(normalize-unicode($char, 'NFD'))}\"/>\n" +
      "}</out>\n" +
      "",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<out/>", false)
    );
  }

  /**
   *  Evaluation of fn:normalize-unicode with a normalization form that attempts to raise a non-implemented form .
   */
  @org.junit.Test
  public void fnNormalizeUnicode2() {
    final XQuery query = new XQuery(
      "fn:normalize-unicode(\"è\",\"chancesareyoudonotsupportthis123ifyoudowaoo\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FOCH0003")
    );
  }

  /**
   *  Evaluation of fn:normalize-unicode with nothing to normalize and used as argument to fn:upper-case .
   */
  @org.junit.Test
  public void fnNormalizeUnicode3() {
    final XQuery query = new XQuery(
      "fn:upper-case(fn:normalize-unicode(\"normalizedstring\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NORMALIZEDSTRING")
    );
  }

  /**
   *  Evaluation of fn:normalize-unicode with nothing to normalize and used as argument to fn:lower-case .
   */
  @org.junit.Test
  public void fnNormalizeUnicode4() {
    final XQuery query = new XQuery(
      "fn:lower-case(fn:normalize-unicode(\"NORMALIZEDSTRING\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "normalizedstring")
    );
  }

  /**
   *  Evaluation of fn:normalize-unicode with nothing to normalize and used as argument to fn:string-length .
   */
  @org.junit.Test
  public void fnNormalizeUnicode5() {
    final XQuery query = new XQuery(
      "fn:string-length(fn:normalize-unicode(\"NORMALIZEDSTRING\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "16")
    );
  }

  /**
   *  Evaluation of fn:normalize-unicode with nothing to normalize and used as argument to fn:concat .
   */
  @org.junit.Test
  public void fnNormalizeUnicode6() {
    final XQuery query = new XQuery(
      "fn:concat(fn:normalize-unicode(\"NORMALIZEDSTRING\"),\"another string\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "NORMALIZEDSTRINGanother string")
    );
  }

  /**
   *  Evaluation of fn:normalize-unicode with nothing to normalize and used as argument to fn:substring .
   */
  @org.junit.Test
  public void fnNormalizeUnicode7() {
    final XQuery query = new XQuery(
      "fn:substring(fn:normalize-unicode(\"NORMALIZEDSTRING\"),5)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ALIZEDSTRING")
    );
  }

  /**
   *  Evaluation of fn:normalize-unicode with noncharacter argument .
   */
  @org.junit.Test
  public void fnNormalizeUnicode8() {
    final XQuery query = new XQuery(
      "string-to-codepoints(normalize-unicode('﷐'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "64976")
    );
  }

  /**
   *  Evaluation of fn:normalize-unicode with noncharacters in argument .
   */
  @org.junit.Test
  public void fnNormalizeUnicode9() {
    final XQuery query = new XQuery(
      "string-to-codepoints(fn:normalize-unicode('ê﷐ê﷐ê'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertDeepEq("234, 64976, 234, 64976, 234")
    );
  }

  /**
   * Test normalize-unicode with simple text input .
   */
  @org.junit.Test
  public void fnNormalizeUnicode1args1() {
    final XQuery query = new XQuery(
      "normalize-unicode('Nothing to normalize.')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Nothing to normalize.")
    );
  }

  /**
   * Test normalize-unicode with empty sequence argument .
   */
  @org.junit.Test
  public void fnNormalizeUnicode1args2() {
    final XQuery query = new XQuery(
      "normalize-unicode(())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "")
    );
  }

  /**
   * Test fn:normalize-unicode on combining characters for LATIN CAPITAL LETTER A WITH RING (w/ ACUTE) and ANGSTROM SIGN .
   */
  @org.junit.Test
  public void fnNormalizeUnicode1args3() {
    final XQuery query = new XQuery(
      "matches('Ǻ', normalize-unicode('Ǻ'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Test fn:normalize-unicode on combining characters for LATIN CAPITAL LETTER A WITH RING (w/ ACUTE) and ANGSTROM SIGN .
   */
  @org.junit.Test
  public void fnNormalizeUnicode1args4() {
    final XQuery query = new XQuery(
      "matches('Å', normalize-unicode('Å'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Test fn:normalize-unicode on combining characters for LATIN CAPITAL LETTER A WITH RING (w/ ACUTE) and ANGSTROM SIGN .
   */
  @org.junit.Test
  public void fnNormalizeUnicode1args5() {
    final XQuery query = new XQuery(
      "matches('Å', normalize-unicode('Å'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * Test fn:normalize-unicode on combining characters for LATIN CAPITAL LETTER A WITH RING (w/ ACUTE) and ANGSTROM SIGN .
   */
  @org.junit.Test
  public void fnNormalizeUnicode1args6() {
    final XQuery query = new XQuery(
      "(normalize-unicode('Å') eq normalize-unicode('Å'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Test normalize-unicode with invalid argument types .
   */
  @org.junit.Test
  public void fnNormalizeUnicode1args7() {
    final XQuery query = new XQuery(
      "normalize-unicode(12)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Test normalize-unicode with incorrect arity .
   */
  @org.junit.Test
  public void fnNormalizeUnicode1args8() {
    final XQuery query = new XQuery(
      "normalize-unicode('','','')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   * Test normalize-unicode with simple text input and NFC .
   */
  @org.junit.Test
  public void fnNormalizeUnicode2args1() {
    final XQuery query = new XQuery(
      "normalize-unicode('Nothing to normalize.', 'NFC')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Nothing to normalize.")
    );
  }

  /**
   * Test normalize-unicode with simple text input and NFC spelled differently .
   */
  @org.junit.Test
  public void fnNormalizeUnicode2args2() {
    final XQuery query = new XQuery(
      "normalize-unicode('Nothing to normalize.', 'nFc')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "Nothing to normalize.")
    );
  }

  /**
   * Test normalize-unicode with empty sequence argument and NFC .
   */
  @org.junit.Test
  public void fnNormalizeUnicode2args3() {
    final XQuery query = new XQuery(
      "normalize-unicode((), 'NFC')",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "")
    );
  }

  /**
   * Test fn:normalize-unicode where the second argument is the zero-length string, no normalization is performed .
   */
  @org.junit.Test
  public void fnNormalizeUnicode2args4() {
    final XQuery query = new XQuery(
      "(normalize-unicode('Å', '') eq normalize-unicode('Å', ''))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * Test normalize-unicode with invalid argument types .
   */
  @org.junit.Test
  public void fnNormalizeUnicode2args5() {
    final XQuery query = new XQuery(
      "normalize-unicode('',())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Test normalize-unicode with invalid argument types .
   */
  @org.junit.Test
  public void fnNormalizeUnicode2args6() {
    final XQuery query = new XQuery(
      "normalize-unicode('',12)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }
}
