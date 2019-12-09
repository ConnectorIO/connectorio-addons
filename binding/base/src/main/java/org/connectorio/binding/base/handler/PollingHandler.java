package org.connectorio.binding.base.handler;

/**
 * Generic supertype for pollable handlers of different kinds.
 */
public interface PollingHandler {

  /**
   * Returns configured or default reloading cycle for handler.
   *
   * @return Refresh interval between read attempts.
   */
  Long getRefreshInterval();

}
