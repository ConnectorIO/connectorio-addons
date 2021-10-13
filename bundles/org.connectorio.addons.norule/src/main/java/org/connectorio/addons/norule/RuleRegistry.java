package org.connectorio.addons.norule;

import org.openhab.core.common.registry.Registry;

public interface RuleRegistry extends Registry<Rule, RuleUID> {

  void run(RuleUID ruleUID);

}
