package org.connectorio.addons.binding.fatek.internal;

import static org.simplify4u.jfatek.registers.DisReg.*;
import static org.simplify4u.jfatek.registers.DataReg.*;

import org.connectorio.addons.binding.fatek.config.channel.RegisterConfig;
import org.simplify4u.jfatek.registers.DataReg;
import org.simplify4u.jfatek.registers.DisReg;

public class RegisterParser {

  public static DisReg parseDiscrete(RegisterConfig config) {
    switch (config.getRegister()) {
      case X:
        return X(config.getIndex());
      case Y:
        return Y(config.getIndex());
      case M:
        return M(config.getIndex());
      case S:
        return S(config.getIndex());
      case T:
        return T(config.getIndex());
      case C:
        return C(config.getIndex());
      default:
        throw new IllegalArgumentException("Unsupported register kind " + config.getRegister());
    }
  }

  public static DataReg parseData16(RegisterConfig config) {
    switch (config.getRegister()) {
      case R:
        return R(config.getIndex());
      case D:
        return D(config.getIndex());
      case F:
        return F(config.getIndex());
      case RT:
        return RT(config.getIndex());
      case RC:
        return RC(config.getIndex());
      case WX:
        return WX(config.getIndex());
      case WY:
        return WY(config.getIndex());
      case WM:
        return WM(config.getIndex());
      case WS:
        return WS(config.getIndex());
      case WT:
        return WT(config.getIndex());
      case WC:
        return WC(config.getIndex());
      default:
        throw new IllegalArgumentException("Unsupported register kind " + config.getRegister());
    }
  }

  public static DataReg parseData32(RegisterConfig config) {
    switch (config.getRegister()) {
      case DR:
        return DR(config.getIndex());
      case DD:
        return DD(config.getIndex());
      case DF:
        return DF(config.getIndex());
      case DRT:
        return DRT(config.getIndex());
      case DRC:
        return DRC(config.getIndex());
      case DWX:
        return DWX(config.getIndex());
      case DWY:
        return DWY(config.getIndex());
      case DWM:
        return DWM(config.getIndex());
      case DWS:
        return DWS(config.getIndex());
      case DWT:
        return DWT(config.getIndex());
      case DWC:
        return DWC(config.getIndex());
      default:
        throw new IllegalArgumentException("Unsupported register kind " + config.getRegister());
    }
  }

}
