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
package org.connectorio.addons.norule.internal.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.connectorio.addons.norule.Action;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingHandler;

@ExtendWith(MockitoExtension.class)
class DefaultThingActionsRegistryTest {

  private static final ThingUID TEST_UID = new ThingUID("mail", "smtp", "instanceX");
  @Mock
  ThingHandler handler;
  @Mock
  Thing thing;

  @BeforeEach
  void setup() {
    when(handler.getThing()).thenReturn(thing);
    when(thing.getUID()).thenReturn(TEST_UID);
  }

  @Test
  void checkResolver() {
    DefaultThingActionsRegistry registry = new DefaultThingActionsRegistry();
    TestActions actions = new TestActions();
    actions.setThingHandler(handler);
    registry.addThingActions(actions);

    Optional<Action<Integer>> action = registry.lookupAction("", TEST_UID, getClass().getClassLoader());

    assertThat(action).isNotEmpty();

    Action<Integer> action1 = action.get();
    action1.setInput("recipient", "foo");
    action1.setInput("subject", "bar");
    action1.setInput("attach", Arrays.asList("baz"));

    int result = action1.invoke("test");
    assertThat(result).isEqualTo(7);
  }

  static class TestActions implements ThingActions {

    private ThingHandler handler;

    @RuleAction(label = "Test action", description = "Some extra description")
    public @ActionOutput(name = "success", type = "java.lang.Integer") int test(
      @ActionInput(name = "recipient") String recipient,
      @ActionInput(name = "subject") String subject,
      @ActionInput(name = "attach") List<String> objects) {

      return recipient.length() + subject.length() + objects.size();
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
      this.handler = handler;
    }

    @Override
    public ThingHandler getThingHandler() {
      return handler;
    }
  }

}