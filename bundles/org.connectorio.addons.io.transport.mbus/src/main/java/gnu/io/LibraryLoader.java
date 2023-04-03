package gnu.io;

public class LibraryLoader {

  private static boolean loaded = false;

  public synchronized static void loadRxtxNative() {
    try {
      if (!loaded) {
        loaded = true;
        new NativeResource().load("libNRJavaSerial");
      }

    } catch (NativeResourceException e) {
      System.err.println("Could not load native part of serial port library.");
    }
  }

}
