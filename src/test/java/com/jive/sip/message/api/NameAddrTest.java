/**
 *
 */
package com.jive.sip.message.api;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.net.HostAndPort;
import com.jive.sip.base.api.Token;
import com.jive.sip.parameters.impl.TokenParameterDefinition;
import com.jive.sip.uri.api.SipUri;

/**
 * @author Jeff Hutchins {@code <jhutchins@getjive.com>}
 *
 */
public class NameAddrTest
{

  @Test
  public void parameterTest()
  {
    final NameAddr name = new NameAddr(new SipUri(HostAndPort.fromString("10.199.3.1:5061")))
        .withParameter(Token.from("ftag"), Token.from("a29dd1ac97e3b91e"))
        .withParameter(Token.from("lr"), Token.from("on"));

    Assert.assertTrue(name.getParameters().isPresent());

    Assert.assertEquals(Token.from("a29dd1ac97e3b91e"), name.getParameter(new TokenParameterDefinition("ftag")).get());

    Assert.assertEquals(Token.from("on"), name.getParameter(new TokenParameterDefinition("lr")).get());

  }

  @Test
  public void testWithUri()
  {


    final NameAddr name = new NameAddr(new SipUri(HostAndPort.fromString("10.199.3.1:5061")))
        .withParameter(Token.from("expires"), Token.from("0"));

    System.err.println(name.withAddress(name.getAddress()));


  }

}
