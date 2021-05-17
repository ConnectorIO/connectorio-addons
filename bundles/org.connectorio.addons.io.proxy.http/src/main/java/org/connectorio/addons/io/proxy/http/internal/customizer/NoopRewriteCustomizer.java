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
 * SPDX-License-Identifier: Apache-2.0
 */
package org.connectorio.addons.io.proxy.http.internal.customizer;

import java.util.function.Supplier;
import javax.servlet.http.HttpServletRequest;
import org.connectorio.addons.io.proxy.http.RewriteCustomizer;
import org.eclipse.jetty.client.api.Request;
import org.osgi.service.component.annotations.Component;

// # tag::default-rewriter[]
@Component(immediate = true, property = {"name=default"}, service = RewriteCustomizer.class)
public class NoopRewriteCustomizer implements RewriteCustomizer {

  public static final String DEFAULT_CUSTOMIZER_NAME = "default";

  @Override
  public void copyHeaders(Supplier<Void> defaultHeaders, Supplier<Void> proxyHeaders, HttpServletRequest clientRequest, Request proxyRequest) {
    defaultHeaders.get();
    proxyHeaders.get();
  }
}
// # end::default-rewriter