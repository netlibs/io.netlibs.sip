package com.jive.sip.processor.uri.parsers;

import static com.jive.sip.parsers.core.ParserUtils.ALPHANUM_CHARS;
import static com.jive.sip.parsers.core.ParserUtils.EQUALS;
import static com.jive.sip.parsers.core.ParserUtils.ch;
import static com.jive.sip.parsers.core.ParserUtils.chars;
import static com.jive.sip.parsers.core.ParserUtils.read;

import java.net.URLDecoder;
import java.util.Collection;

import com.google.common.collect.Lists;
import com.google.common.net.HostAndPort;
import com.jive.sip.base.api.RawHeader;
import com.jive.sip.parameters.api.RawParameter;
import com.jive.sip.parameters.impl.DefaultParameters;
import com.jive.sip.parsers.api.Parser;
import com.jive.sip.parsers.api.ParserContext;
import com.jive.sip.parsers.api.ValueListener;
import com.jive.sip.parsers.core.ByteParserInput;
import com.jive.sip.parsers.core.HostAndPortParser;
import com.jive.sip.parsers.core.ParameterParser;
import com.jive.sip.parsers.core.ParserUtils;
import com.jive.sip.uri.api.SipUri;
import com.jive.sip.uri.api.UserInfo;

public class SipUriParser implements UriSchemeParser<SipUri>
{
  private final String scheme;

  private SipUriParser(final String scheme)
  {
    this.scheme = scheme;
  }

  public static final SipUriParser SIP = new SipUriParser(SipUri.SIP);
  public static final SipUriParser SIPS = new SipUriParser(SipUri.SIPS);

  private static final Parser<CharSequence> USER = chars(
      ALPHANUM_CHARS.concat("-_.!~*'()%&=+$,;?/").concat("#[]"));
  private static final Parser<CharSequence> PASSWORD = chars(ALPHANUM_CHARS.concat("-_.!~*'()%&=+$,"));
  private static final Parser<CharSequence> COLON = ch(':');
  private static final Parser<CharSequence> AT = ch('@');
  public static final Parser<UserInfo> USERINFO = new Parser<UserInfo>()
  {
    @Override
    public boolean find(final ParserContext ctx, final ValueListener<UserInfo> value)
    {


      final int pos = ctx.position();

      final CharSequence user = read(ctx, USER);

      if (user == null)
      {
        return false;
      }


      UserInfo info;

      if (ctx.skip(COLON))
      {
        final CharSequence password = read(ctx, PASSWORD);
        if (password == null)
        {
          ctx.position(pos);
          return false;
        }
        info = new UserInfo(user.toString(), password.toString());
      }
      else
      {
        info = new UserInfo(user.toString());
      }

      if (!ctx.skip(AT))
      {
        ctx.position(pos);
        return false;
      }

      if (value != null)
      {
        value.set(info);
      }
      return true;
    }

    @Override
    public String toString()
    {
      return "userinfo";
    }
  };
  private static final Parser<CharSequence> HEADER_CHARS = chars(ALPHANUM_CHARS.concat("-_.!~*'()[]/?:+$%"));
  private static final Parser<RawHeader> HEADER = new Parser<RawHeader>()
  {

    @Override
    public boolean find(final ParserContext ctx, final ValueListener<RawHeader> value)
    {
      final int pos = ctx.position();

      final CharSequence hname = read(ctx, HEADER_CHARS);
      if (hname == null)
      {
        return false;
      }
      if (!ctx.skip(EQUALS))
      {
        ctx.position(pos);
        return false;
      }
      final CharSequence hvalue = read(ctx, HEADER_CHARS);
      if (value != null)
      {
        value.set(new RawHeader(hname.toString(), hvalue != null ? URLDecoder.decode(hvalue.toString()) : ""));
      }
      return true;
    }

    @Override
    public String toString()
    {
      return "header";
    }
  };
  private static final Parser<CharSequence> QUESTION = ch('?');
  private static final Parser<CharSequence> AMP = ch('&');
  public static final Parser<Collection<RawHeader>> HEADERS = new Parser<Collection<RawHeader>>()
  {

    @Override
    public boolean find(final ParserContext ctx, final ValueListener<Collection<RawHeader>> value)
    {
      final int pos = ctx.position();

      final Collection<RawHeader> result = Lists.newLinkedList();
      if (!ctx.skip(QUESTION))
      {
        return false;
      }

      do
      {
        final RawHeader header = read(ctx, HEADER);
        if (header == null)
        {
          ctx.position(pos);
          return false;
        }
        result.add(header);
      }
      while (ctx.skip(AMP));

      if (value != null)
      {
        value.set(result);
      }

      return true;
    }

    @Override
    public String toString()
    {
      return "headers";
    }
  };

  @Override
  public boolean find(final ParserContext ctx, final ValueListener<SipUri> value)
  {
    final int pos = ctx.position();

    final UserInfo info = read(ctx, USERINFO);
    final HostAndPort host = read(ctx, HostAndPortParser.INSTANCE);
    if (host == null)
    {
      ctx.position(pos);
      return false;
    }
    final Collection<RawParameter> rawParams = read(ctx, ParameterParser.getInstance());
    final Collection<RawHeader> headers = read(ctx, HEADERS);

    if (value != null)
    {
      value.set(new SipUri(this.scheme, info, host, DefaultParameters.from(rawParams), headers));
    }
    return true;
  }

  @Override
  public String toString()
  {
    return "sipuri";
  }

  public static SipUri parse(final String input)
  {
    final ByteParserInput is = ByteParserInput.fromString(input.substring(4));
    final SipUri value = ParserUtils.read(is, SIP);
    if (is.remaining() > 0)
    {
      throw new RuntimeException("Trailing Garbage in SIP URI");
    }
    return value;
  }

}
