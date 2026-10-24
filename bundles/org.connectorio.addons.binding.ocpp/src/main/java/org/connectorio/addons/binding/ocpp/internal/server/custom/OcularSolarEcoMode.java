package org.connectorio.addons.binding.ocpp.internal.server.custom;

import org.connectorio.addons.binding.ocpp.internal.OcppSender;
import org.connectorio.addons.binding.ocpp.internal.server.ChargerReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.chargetime.ocpp.model.core.ChangeConfigurationRequest;

public class OcularSolarEcoMode {
	private static final String KEY_ECO_MODE = "EcoMode";
	
	private final Logger logger = LoggerFactory.getLogger(OcularSolarEcoMode.class);
	private final String initialOcularEcoMode;
	private OcppSender ocppSender;

  public OcularSolarEcoMode(String initialOcularEcoMode) {
    this.initialOcularEcoMode = initialOcularEcoMode;
  }
  
  public void setOcppSender(OcppSender ocppSender) {
  	this.ocppSender = ocppSender;
  }

  public enum EcoMode {
  	FAST("0"),
  	SOLAR_ASSIST("1"),
  	SOLAR_ONLY("2");
  	
  	String value;
  	EcoMode(String value) {
  		this.value = value;
  	}
  }

  public void applyOcularEcoMode(ChargerReference reference) {
  	if (ocppSender == null) {
      logger.warn("OcppSender or charger serial not set. Cannot send charging profile.");
  		return;
  	}
  	
  	if (initialOcularEcoMode == null) {
  		return;
  	}  	
  	EcoMode ecoMode;
    try {
       ecoMode = EcoMode.valueOf(initialOcularEcoMode);
    } catch (IllegalArgumentException e) {
    	logger.warn("Cannot set ecoMode as it is the unknown value %s.", initialOcularEcoMode);
    	return;
    }
  	
    try {
      ChangeConfigurationRequest request = new ChangeConfigurationRequest(KEY_ECO_MODE, ecoMode.value);
      ocppSender.send(reference, request).whenComplete((confirmation, ex) -> {
        if (ex != null) {
          logger.warn("ChangeConfiguration failed", ex);
        } else {
          logger.info("ChangeConfiguration result: {}", confirmation);
        }
      });
    } catch (Exception e) {
      logger.error("Error sending ChangeConfiguration", e);
    }
  }
}
