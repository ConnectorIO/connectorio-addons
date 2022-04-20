package org.connectorio.addons.profile.counter.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import org.connectorio.addons.profile.counter.internal.state.LinkedItemStateRetriever;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;

@ExtendWith(MockitoExtension.class)
class PulseCounterProfileTest {

  @Mock
  ProfileCallback callback;
  @Mock
  ProfileContext context;
  @Mock
  LinkedItemStateRetriever itemStateRetriever;

  @Test
  void checkDecimalValueFromItem() {
    HashMap<String, Object> cfgMap = new HashMap<>();
    cfgMap.put("tick", "0.1");
    Configuration config = new Configuration(cfgMap);

    when(context.getConfiguration()).thenReturn(config);

    PulseCounterProfile profile = new PulseCounterProfile(callback, context, itemStateRetriever);

    // update from handler before item state is set -> no interactions
    profile.onStateUpdateFromHandler(new DecimalType(13.0));
    Mockito.verifyNoInteractions(callback);

    // item initializes last state, then handler can send its updates
    profile.onStateUpdateFromItem(new DecimalType(10.0));
    Mockito.verify(callback).handleCommand(new DecimalType(10.0));
    profile.onStateUpdateFromHandler(OnOffType.ON);
    profile.onStateUpdateFromHandler(OnOffType.OFF);
    Mockito.verify(callback).sendUpdate(new DecimalType(10.1));

    profile.onStateUpdateFromHandler(OnOffType.ON);
    profile.onStateUpdateFromHandler(OnOffType.OFF);
    Mockito.verify(callback).sendUpdate(new DecimalType(10.2));
  }

}