package org.connectorio.addons.profile.counter.internal.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.Collections;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.internal.profiles.ProfileCallbackImpl;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.types.State;

@ExtendWith(MockitoExtension.class)
class PersistenceItemStateRetrieverTest {

  private static final String TEST_ITEM = "Item_Test_123";
  private static final ChannelUID CHANNEL_UID = new ChannelUID("test:test:test:test");

  @Mock
  private PersistenceServiceRegistry persistenceServiceRegistry;

  public void testStateRetrieval() {
    ItemChannelLink link = new ItemChannelLink(TEST_ITEM, CHANNEL_UID);
    CallbackImpl stub = new CallbackImpl(link);
    final DecimalType initialState = new DecimalType(10.0);

    QueryablePersistenceService persistenceService = mock(QueryablePersistenceService.class);
    when(persistenceService.query(any(FilterCriteria.class))).thenReturn(Collections.singletonList(
        new HistoricItem() {
          @Override
          public ZonedDateTime getTimestamp() {
            return ZonedDateTime.now();
          }

          @Override
          public State getState() {
            return initialState;
          }

          @Override
          public String getName() {
            return TEST_ITEM;
          }
        }
    ));

    when(persistenceServiceRegistry.getDefault()).thenReturn(persistenceService);

    PersistenceItemStateRetriever registry = new PersistenceItemStateRetriever(persistenceServiceRegistry);
    State state = registry.retrieve(stub);
    assertThat(state).isEqualTo(initialState);
  }

  static class CallbackImpl extends ProfileCallbackImpl {
    public CallbackImpl(ItemChannelLink link) {
      super(null, null, null, link, null, null);
    }
  }
}