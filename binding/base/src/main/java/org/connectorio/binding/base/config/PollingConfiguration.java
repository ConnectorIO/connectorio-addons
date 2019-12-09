package org.connectorio.binding.base.config;

/**
 * Supertype for polling configuration.
 */
public class PollingConfiguration implements Configuration {

  /**
   * Refresh interval for refreshing/polling items.
   */
  public Long refreshInterval = 1000L;

}
