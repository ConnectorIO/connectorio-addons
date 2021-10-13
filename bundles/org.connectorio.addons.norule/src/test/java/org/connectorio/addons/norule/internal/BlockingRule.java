package org.connectorio.addons.norule.internal;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import org.connectorio.addons.norule.Rule;
import org.connectorio.addons.norule.RuleContext;
import org.connectorio.addons.norule.RuleUID;
import org.connectorio.addons.norule.Trigger;

public class BlockingRule implements Rule {

  private final CountDownLatch latch = new CountDownLatch(1);
  private final Rule rule;

  public BlockingRule(Rule rule) {
    this.rule = rule;
  }

  @Override
  public Set<Trigger> getTriggers() {
    return rule.getTriggers();
  }

  @Override
  public void handle(RuleContext context) {
    rule.handle(context);
    latch.countDown();
  }

  @Override
  public RuleUID getUID() {
    return rule.getUID();
  }

  public CountDownLatch getLatch() {
    return latch;
  }

}
