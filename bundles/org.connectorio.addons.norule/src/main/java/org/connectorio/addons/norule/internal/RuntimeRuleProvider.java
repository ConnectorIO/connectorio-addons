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
package org.connectorio.addons.norule.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.connectorio.addons.norule.Rule;
import org.connectorio.addons.norule.RuleProvider;
import org.connectorio.addons.norule.RuleUID;
import org.openhab.core.common.registry.ManagedProvider;
import org.openhab.core.common.registry.ProviderChangeListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = {RuleProvider.class, ManagedProvider.class})
public class RuntimeRuleProvider implements RuleProvider, ManagedProvider<Rule, RuleUID> {

  private final Logger logger = LoggerFactory.getLogger(RuntimeRuleProvider.class);
  private final List<ProviderChangeListener<Rule>> listeners = new CopyOnWriteArrayList<>();
  private final Map<RuleUID, Rule> rules;

  @Activate
  public RuntimeRuleProvider() {
    this(new LinkedHashMap<>());
  }

  public RuntimeRuleProvider(Rule ... rules) {
    this(Arrays.stream(rules).collect(Collectors.toMap(Rule::getUID, Function.identity(), (left, right) -> left)));
  }

  public RuntimeRuleProvider(Map<RuleUID, Rule> rules) {
    this.rules = rules;
  }

  @Override
  public Collection<Rule> getAll() {
    return rules.values();
  }

  @Override
  public void addProviderChangeListener(ProviderChangeListener<Rule> listener) {
    listeners.add(listener);
  }

  @Override
  public void removeProviderChangeListener(ProviderChangeListener<Rule> listener) {
    listeners.remove(listener);
  }

  @Override
  public void add(Rule element) {
    rules.put(element.getUID(), element);
    listeners.forEach(listener -> listener.added(this, element));
  }

  @Override
  public Rule remove(RuleUID key) {
    Rule rule = rules.remove(key);
    if (rule != null) {
      listeners.forEach(listener -> listener.removed(this, rule));
    }
    return rule;
  }

  @Override
  public Rule update(Rule element) {
    Rule old = rules.put(element.getUID(), element);
    if (old != null) {
      listeners.forEach(listener -> listener.updated(this, old, element));
    }
    return element;
  }

  @Override
  public Rule get(RuleUID key) {
    return rules.get(key);
  }

  @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
  public void addRule(Rule rule) {
    logger.info("Registration of rule {} with triggers {}", rule, rule.getTriggers());
    add(rule);
  }

  public void removeRule(Rule rule) {
    logger.info("Rule {} removed, it will not be triggered.", rule);
    remove(rule.getUID());
  }
}
