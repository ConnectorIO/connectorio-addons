/*
 * Copyright (C) 2019-2020 ConnectorIO Sp. z o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.connectorio.binding.plc4x.shared.osgi.internal;

import java.util.List;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.PlcDriver;
import org.apache.plc4x.java.api.authentication.PlcAuthentication;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.connectorio.binding.plc4x.shared.osgi.PlcDriverManager;

/**
 * Implementation of osgi aware driver manager service.
 */
public class OsgiDriverManager implements PlcDriverManager {

  private final org.apache.plc4x.java.PlcDriverManager mgr;
  private final CompoundClassLoader classLoader;

  public OsgiDriverManager(List<ClassLoader> wiring) {
    classLoader = new CompoundClassLoader(wiring);
    this.mgr = new org.apache.plc4x.java.PlcDriverManager(classLoader);
  }

  @Override
  public PlcConnection getConnection(String url) throws PlcConnectionException {
    return ClassLoaderAware.call(classLoader, () -> mgr.getConnection(url));
  }

  @Override
  public PlcConnection getConnection(String url, PlcAuthentication authentication) throws PlcConnectionException {
    return ClassLoaderAware.call(classLoader, () -> mgr.getConnection(url, authentication));
  }

  @Override
  public PlcDriver getDriver(String url) throws PlcConnectionException {
    return ClassLoaderAware.call(classLoader, () -> mgr.getDriver(url));
  }

  void close() {
  }

}