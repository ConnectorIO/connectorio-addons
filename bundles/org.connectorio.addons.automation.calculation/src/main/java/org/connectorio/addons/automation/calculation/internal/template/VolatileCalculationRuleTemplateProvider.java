package org.connectorio.addons.automation.calculation.internal.template;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import org.openhab.core.automation.template.RuleTemplate;
import org.openhab.core.automation.template.RuleTemplateProvider;
import org.openhab.core.common.registry.ProviderChangeListener;
import org.osgi.service.component.annotations.Component;

@Component
public class VolatileCalculationRuleTemplateProvider implements RuleTemplateProvider {

  public static final Set<RuleTemplate> TEMPLATES = Collections.singleton(new VolatileCalculationRuleTemplate());

  @Override
  public RuleTemplate getTemplate(String uid, Locale locale) {
    return VolatileCalculationRuleTemplate.UID.equals(uid) ? TEMPLATES.iterator().next() : null;
  }

  @Override
  public Collection<RuleTemplate> getTemplates(Locale locale) {
    return TEMPLATES;
  }

  @Override
  public Collection<RuleTemplate> getAll() {
    return TEMPLATES;
  }

  @Override
  public void removeProviderChangeListener(ProviderChangeListener<RuleTemplate> listener) {
    // does nothing because this provider does not change
  }

  @Override
  public void addProviderChangeListener(ProviderChangeListener<RuleTemplate> listener) {
    // does nothing because this provider does not change
  }

}
