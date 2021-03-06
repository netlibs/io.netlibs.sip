package com.jive.sip.message.api.headers;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.jive.sip.base.api.Token;
import com.jive.sip.message.api.NameAddr;
import com.jive.sip.parameters.api.RawParameter;
import com.jive.sip.parameters.api.SipParameterDefinition;
import com.jive.sip.parameters.api.TokenParameterValue;
import com.jive.sip.parameters.impl.DefaultParameters;
import com.jive.sip.parameters.tools.ParameterUtils;
import com.jive.sip.uri.api.Uri;

import lombok.Value;


/**
 * First stab at a History-Info header.
 *
 * @author theo
 *
 */

public class HistoryInfo
{

  public static final SipParameterDefinition<Token> P_RC = ParameterUtils.createFlagParameterDefinition(Token.from("c"));
  public static final SipParameterDefinition<Token> P_MP = ParameterUtils.createFlagParameterDefinition(Token.from("mp"));
  public static final SipParameterDefinition<Token> P_NP = ParameterUtils.createFlagParameterDefinition(Token.from("np"));


  public static enum ChangeType
  {
    // o "rc": The Request-URI has changed while the target user associated
    // with the original Request-URI prior to retargeting has been
    // retained.
    RC,
    // o "mp": The target was determined based on a mapping to a user other
    // than the target user associated with the Request-URI being
    // retargeted.
    MP,
    // o "np": The target hasn't changed, and the associated Request-URI
    // remained the same.
    NP,
    // unknown (none specified?
    Unknown

  }

  @Value
  public static class Entry
  {

    private Uri uri;
    private int[] index;
    private ChangeType type;
    private int[] prev;

    public NameAddr toNameAddr()
    {
      final List<RawParameter> raw = Lists.newLinkedList();
      switch (this.type)
      {
        case MP:
          raw.add(new RawParameter("mp", new TokenParameterValue(Token.from(buildIndex(prev)))));
          break;
        case NP:
          raw.add(new RawParameter("np", new TokenParameterValue(Token.from(buildIndex(prev)))));
          break;
        case RC:
          raw.add(new RawParameter("rc", new TokenParameterValue(Token.from(buildIndex(prev)))));
          break;
        default:
          break;
      }

      if ((this.index != null) && (this.index.length > 0))
      {
        raw.add(new RawParameter("index", new TokenParameterValue(Token.from(buildIndex(this.index)))));
      }

      return new NameAddr(this.uri, DefaultParameters.from(raw));

    }

    public ChangeType getChangeType()
    {
      return this.type;
    }

  }

  public static final HistoryInfo EMPTY = new HistoryInfo(Lists.<Entry> newLinkedList());
  private static final int[] INITIAL_INDEX =
  { 1 };
  private final List<Entry> entries;


  private static String buildIndex(int[] index)
  {
    StringBuilder sb = new StringBuilder();
    int i = 0;
    for (int id : index)
    {
      if (i++ > 0)
      {
        sb.append('.');
      }
      sb.append(id);
    }
    return sb.toString();
  }

  public HistoryInfo(final List<Entry> entries)
  {
    this.entries = entries;
  }

  public List<Entry> entries()
  {
    // TODO: immutable?
    return this.entries;
  }

  public Optional<Entry> last()
  {
    if (this.entries.isEmpty())
    {
      return Optional.empty();
    }
    return Optional.of(this.entries.get(this.entries.size() - 1));
  }

  public static HistoryInfo build(final List<NameAddr> nas)
  {
    final List<Entry> entries = Lists.newLinkedList();
    for (final NameAddr na : nas)
    {
      int[] prev = new int[]
      { 1 };
      entries.add(new Entry(na.getAddress(), extractIndex(na), extractType(na), prev));
    }
    return new HistoryInfo(entries);
  }

  private static int[] extractIndex(final NameAddr na)
  {
    return new int[]
    { 0 };
  }

  private static ChangeType extractType(final NameAddr na)
  {
    if (na.getParameter(P_RC).isPresent())
    {
      return ChangeType.RC;
    }
    else if (na.getParameter(P_MP).isPresent())
    {
      return ChangeType.MP;
    }
    else if (na.getParameter(P_NP).isPresent())
    {
      return ChangeType.NP;
    }
    return ChangeType.Unknown;
  }

  public HistoryInfo withAppended(final Uri target)
  {
    final List<Entry> entries = Lists.newLinkedList(this.entries);
    entries.add(new Entry(target, INITIAL_INDEX, ChangeType.MP, new int[]
    { 1 }));
    return new HistoryInfo(entries);
  }


  public HistoryInfo withRecursion(final Uri target)
  {
    final List<Entry> entries = Lists.newLinkedList(this.entries);
    entries.add(new Entry(target, INITIAL_INDEX, ChangeType.RC, new int[]
    { 1 }));
    return new HistoryInfo(entries);
  }


  public HistoryInfo withRetarget(final Uri target)
  {
    final List<Entry> entries = Lists.newLinkedList(this.entries);
    entries.add(new Entry(target, INITIAL_INDEX, ChangeType.MP, new int[]
    { 1 }));
    return new HistoryInfo(entries);
  }

  public HistoryInfo withNoChange(final Uri target)
  {
    final List<Entry> entries = Lists.newLinkedList(this.entries);
    entries.add(new Entry(target, INITIAL_INDEX, ChangeType.NP, new int[]
    { 1 }));
    return new HistoryInfo(entries);
  }

  public static HistoryInfo fromUnknownRequest(Uri target)
  {
    final List<Entry> entries = Lists.newLinkedList();
    entries.add(new Entry(target, INITIAL_INDEX, ChangeType.Unknown, null));
    return new HistoryInfo(entries);
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    int i = 0;
    for (Entry e : entries)
    {
      if (i++ > 0)
      {
        sb.append(", ");
      }
      sb.append(e.toNameAddr());
    }
    return sb.toString();
  }

  public boolean isEmpty()
  {
    return this.entries.isEmpty();
  }

}
