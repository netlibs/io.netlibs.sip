package com.jive.sip.parameters.api;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FlagParameterValue extends ParameterValue<Void>
{

  private static final FlagParameterValue INSTANCE = new FlagParameterValue();

  public static FlagParameterValue getInstance()
  {
    return INSTANCE;
  }

  @Override
  public <T> T apply(final ParameterValueVisitor<T> visitor)
  {
    return visitor.visit(this);
  }

  @Override
  public String toString()
  {
    return "";
  }

  @Override
  public Void getValue()
  {
    return null;
  }

  @Override
  public int hashCode()
  {
    return 0;
  }

  @Override
  public boolean equals(final Object obj)
  {
    return obj instanceof FlagParameterValue;
  }

}
