package com.events.core.commands.common.path;

import java.util.Map;
import java.util.Optional;

public class PlaceholderValueMapProvider implements PlaceholderValueProvider {

  private final Map<String, String> params;

  public PlaceholderValueMapProvider(Map<String, String> params) {
    if (params == null) {
      throw new IllegalArgumentException("params cannot be null");
    }

    this.params = params;
  }

  @Override
  public Optional<String> get(String name) {
    return Optional.ofNullable(params.get(name));
  }

  @Override
  public Map<String, String> getParams() {
    return params;
  }
}
