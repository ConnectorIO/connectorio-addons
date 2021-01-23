package org.connectorio.addons.automation.calculation.internal.template;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.connectorio.addons.automation.calculation.internal.handler.VolatileCalculationActionHandler;
import org.connectorio.automation.period.PeriodTriggerConstants;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.Visibility;
import org.openhab.core.automation.template.RuleTemplate;
import org.openhab.core.automation.util.ModuleBuilder;

public class VolatileCalculationRuleTemplate extends RuleTemplate {

  public static final String UID = "VolatileCalculation";

  public VolatileCalculationRuleTemplate() {
    super(UID, "Volatile calculation", "Template for a volatile calculation for tracking change of an item",
      Collections.singleton("Volatile calculation"),
      createTriggers(UUID.randomUUID().toString()),
      Collections.emptyList(),
      createActions(Collections.emptyMap()),
      Collections.emptyList(),
      Visibility.VISIBLE
    );
  }

  private static List<Action> createActions(Map<String, String> actionInputs) {
    return Arrays.asList(ModuleBuilder.createAction().withId(UUID.randomUUID().toString())
      .withTypeUID(VolatileCalculationActionHandler.MODULE_TYPE_ID).withLabel("Calculate difference")
      .withInputs(actionInputs).build());
  }

  private static List<Trigger> createTriggers(String triggerId) {
    final List<Trigger> triggers = Arrays.asList(ModuleBuilder.createTrigger().withId(triggerId)
      .withTypeUID(PeriodTriggerConstants.MODULE_TYPE_ID).withLabel("Periodic Trigger").build());
    return triggers;
  }
}
