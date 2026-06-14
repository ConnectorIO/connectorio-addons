package org.connectorio.addons.binding.ocpp.internal.handler;

import eu.chargetime.ocpp.model.core.ChargingProfile;
import eu.chargetime.ocpp.model.core.ChargingProfileKindType;
import eu.chargetime.ocpp.model.core.ChargingProfilePurposeType;
import eu.chargetime.ocpp.model.core.ChargingRateUnitType;
import eu.chargetime.ocpp.model.core.ChargingSchedule;
import eu.chargetime.ocpp.model.core.ChargingSchedulePeriod;
import eu.chargetime.ocpp.model.smartcharging.SetChargingProfileRequest;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import org.connectorio.addons.binding.ocpp.internal.server.ChargerReference;
import org.connectorio.addons.binding.ocpp.internal.server.SetChargingProfileCoalescer;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChargeLimitCommandHandler {
    private final Logger logger = LoggerFactory.getLogger(ChargeLimitCommandHandler.class);

    // Last non-zero limit requested, restored on resume (pause = limit 0).
    private double lastRequestedLimit = -1;

    // One coalescer per handler instance — handlers are created per connector.
    private SetChargingProfileCoalescer coalescer;

    public void handle(Command command, ConnectorCommandContext context) {
        double limit;
        if (command instanceof DecimalType) {
            limit = ((DecimalType) command).doubleValue();
        } else if (command instanceof QuantityType) {
            limit = ((QuantityType<?>) command).doubleValue();
        } else {
            logger.warn("Unsupported command type for chargeLimit: {}", command.getClass());
            return;
        }
        if (!canSend(context)) {
            return;
        }
        if (limit > 0) {
            lastRequestedLimit = limit;
        }
        coalescer(context).submit((int) Math.round(limit));
    }

    /** Pause charging by applying a 0 A limit (OCPP has no dedicated pause command). */
    public void pause(ConnectorCommandContext context) {
        if (!canSend(context)) {
            return;
        }
        coalescer(context).submit(0);
    }

    /** Resume charging by restoring the last non-zero limit. */
    public void resume(ConnectorCommandContext context) {
        if (lastRequestedLimit <= 0) {
            logger.info("No prior charge limit to resume to; awaiting next chargeLimit command.");
            return;
        }
        if (!canSend(context)) {
            return;
        }
        coalescer(context).submit((int) Math.round(lastRequestedLimit));
    }

    private boolean canSend(ConnectorCommandContext context) {
        if (context.getOcppSender() == null || context.getChargerSerialNumber() == null
                || context.getConnectorId() == null) {
            logger.warn("OcppSender, charger serial or connector id not set. Cannot send charging profile.");
            return false;
        }
        return true;
    }

    private synchronized SetChargingProfileCoalescer coalescer(ConnectorCommandContext context) {
        if (coalescer == null) {
            coalescer = new SetChargingProfileCoalescer(
                context.getProfileMinIntervalMs(),
                System::currentTimeMillis,
                wire -> sendChargingProfile(wire, context),
                (task, delayMs) -> context.getScheduler().schedule(task, delayMs, TimeUnit.MILLISECONDS));
        }
        return coalescer;
    }

    private CompletionStage<?> sendChargingProfile(int limit, ConnectorCommandContext context) {
        ChargerReference chargerRef = new ChargerReference(context.getChargerSerialNumber());

        ChargingSchedulePeriod period = new ChargingSchedulePeriod(0, (double) limit);
        ChargingSchedule schedule = new ChargingSchedule(
            ChargingRateUnitType.A,
            new ChargingSchedulePeriod[]{ period }
        );

        ChargingProfile profile = new ChargingProfile();
        profile.setChargingProfileId(1);
        profile.setStackLevel(0);
        profile.setChargingProfileKind(ChargingProfileKindType.Relative);
        profile.setChargingSchedule(schedule);

        // A limit applied while a transaction is running must target that transaction with a
        // TxProfile carrying its transactionId. Per OCPP 1.6 Smart Charging a TxDefaultProfile
        // sets the default for *future* transactions, so spec-strict charge points may defer a
        // mid-session TxDefaultProfile to the next transaction instead of acting on the running
        // one. Fall back to TxDefaultProfile only when no transaction is active, which seeds the
        // limit for the next session.
        Integer transactionId = context.getCurrentTransactionId();
        if (transactionId != null) {
            profile.setChargingProfilePurpose(ChargingProfilePurposeType.TxProfile);
            profile.setTransactionId(transactionId);
        } else {
            profile.setChargingProfilePurpose(ChargingProfilePurposeType.TxDefaultProfile);
        }

        SetChargingProfileRequest setProfileRequest = new SetChargingProfileRequest(context.getConnectorId(), profile);

        return context.getOcppSender().send(chargerRef, setProfileRequest).whenComplete((confirmation, throwable) -> {
            if (throwable != null) {
                logger.warn("Failed to send SetChargingProfile with limit {}", limit, throwable);
            } else {
                logger.info("SetChargingProfile with limit {} sent successfully: {}", limit, confirmation);
            }
        });
    }
}
