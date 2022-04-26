/*
 * Copyright (C) 2022-2022 ConnectorIO Sp. z o.o.
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
package org.connectorio.openapi;

import io.swagger.v3.oas.models.media.Schema;
import java.util.EnumSet;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;
import org.openapitools.codegen.SupportingFile;
import org.openapitools.codegen.languages.JavaClientCodegen;
import org.openapitools.codegen.meta.features.ClientModificationFeature;
import org.openapitools.codegen.meta.features.DocumentationFeature;
import org.openapitools.codegen.meta.features.GlobalFeature;
import org.openapitools.codegen.meta.features.SchemaSupportFeature;
import org.openapitools.codegen.meta.features.SecurityFeature;
import org.openapitools.codegen.meta.features.WireFormatFeature;

public class JaxrsClientBuilderGenerator extends JavaClientCodegen {

  public JaxrsClientBuilderGenerator() {
    modifyFeatureSet(features -> features
      .excludeDocumentationFeatures(DocumentationFeature.Api, DocumentationFeature.Model, DocumentationFeature.Readme)
      .wireFormatFeatures(EnumSet.of(WireFormatFeature.JSON, WireFormatFeature.XML))
      .securityFeatures(EnumSet.noneOf(
        SecurityFeature.class
      ))
      .excludeGlobalFeatures(
        GlobalFeature.ExternalDocumentation, GlobalFeature.Examples
      )
      .excludeSchemaSupportFeatures(
        SchemaSupportFeature.Polymorphism
      )
      .includeClientModificationFeatures(
        ClientModificationFeature.BasePath
      )
    );
    supportedLibraries().put("client-builder", "Minimal client stub based on JAX-RS client builder API.");
    forceSerializationLibrary(SERIALIZATION_LIBRARY_JACKSON);
    setLibrary("client-builder");
    setDateLibrary("java8");
    setHideGenerationTimestamp(true);
  }

  @Override
  public void processOpts() {
    super.processOpts();

    supportingFiles.clear();

    final String invokerFolder = (sourceFolder + '/' + invokerPackage).replace(".", "/");
    supportingFiles.add(new SupportingFile("ApiClient.mustache", invokerFolder, "ApiClient.java"));
    supportingFiles.add(new SupportingFile("apiException.mustache", invokerFolder, "ApiException.java"));
  }

  public CodegenModel fromModel(String name, Schema model) {
    CodegenModel codegenModel = super.fromModel(name, model);
    codegenModel.imports.remove("ApiModel");
    codegenModel.imports.remove("ApiModelProperty");
    return codegenModel;
  }

  @Override
  public String getName() {
    return "jaxrs-client-builder";
  }

}
