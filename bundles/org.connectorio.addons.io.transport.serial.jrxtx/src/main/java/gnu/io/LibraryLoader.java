package gnu.io;

public class LibraryLoader {

  private static boolean loaded = false;

  public synchronized static void loadRxtxNative() {
    new NativeResource().load("libNRJavaSerial");
  }

}
