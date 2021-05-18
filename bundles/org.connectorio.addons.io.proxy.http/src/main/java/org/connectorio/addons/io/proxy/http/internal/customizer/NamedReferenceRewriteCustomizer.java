/*
 * Copyright (C) 2019-2021 ConnectorIO Sp. z o.o.
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
 * SPDX-License-Identifier: Apache-2.0
 */
package org.connectorio.addons.io.proxy.http.internal.customizer;

import java.util.Collection;
import java.util.function.Supplier;
import javax.servlet.http.HttpServletRequest;
import org.connectorio.addons.io.proxy.http.RewriteCustomizer;
import org.eclipse.jetty.client.api.Request;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class NamedReferenceRewriteCustomizer implements RewriteCustomizer {

  private final BundleContext context;
  private final String name;

  public NamedReferenceRewriteCustomizer(BundleContext context, String name) {
    this.context = context;
    this.name = name;
  }

  @Override
  public void copyHeaders(Supplier<Void> defaultHeaders, Supplier<Void> proxyHeaders, HttpServletRequest clientRequest, Request proxyRequest) {
    try (DynamicRewriteCustomizer customizer = resolve()) {
      if (customizer == null) {
        throw new IllegalStateException("Customizer '" + name + "' not found");
      }
      customizer.copyHeaders(defaultHeaders, proxyHeaders, clientRequest, proxyRequest);
    } catch (Exception e) {
      throw new IllegalStateException("Could not handle operation", e);
    }
  }

  private DynamicRewriteCustomizer resolve() {
    try {
      Collection<ServiceReference<RewriteCustomizer>> references = context.getServiceReferences(RewriteCustomizer.class, "(name=" + name + ")");
      for (ServiceReference<RewriteCustomizer> reference : references) {
        // first one wins!
        return new DynamicRewriteCustomizer(context, reference, context.getService(reference));
      }
    } catch (InvalidSyntaxException e) {
      throw new RuntimeException();
    }
    return null;
  }

  static class DynamicRewriteCustomizer implements RewriteCustomizer, AutoCloseable {

    private final BundleContext context;
    private final ServiceReference<?> reference;
    private final RewriteCustomizer delegate;

    DynamicRewriteCustomizer(BundleContext context, ServiceReference<?> reference, RewriteCustomizer delegate) {
      this.context = context;
      this.reference = reference;
      this.delegate = delegate;
    }

    @Override
    public void copyHeaders(Supplier<Void> defaultHeaders, Supplier<Void> proxyHeaders, HttpServletRequest clientRequest, Request proxyRequest) {
      delegate.copyHeaders(defaultHeaders, proxyHeaders, clientRequest, proxyRequest);
    }

    @Override
    public void close() throws Exception {
      context.ungetService(reference);
    }
  }

}
