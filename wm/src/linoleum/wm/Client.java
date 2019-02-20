package linoleum.wm;

import gnu.x11.Display;
import gnu.x11.Window;

public class Client extends Window {
  // system attributes
  public Window.AttributesReply attributes;
  public WMClassHint class_hint;
  public String name;
  public WMSizeHints size_hints;

  // internal states
  public boolean early_unmapped, early_destroyed;
  public int register = -1;     // invalid register index
  public int saved_width, saved_height;
  public int state;

  /** Intern. */
  public Client (Display display, int id) {
    super (display, id);
  }

  public static Client intern (Window window) {
    return intern (window.display, window.id);
  }

  public static Client intern (Display display, int id) {
    Object value = display.resources.get (new Integer (id));
    if (value != null && value instanceof Client) return (Client) value;
    return new Client (display, id);
  }
  
  public static final String [] STATE_STRINGS = {
    "unmanaged", "normal", "hidden", "no-focus", "early-unmapped",
    "early-destroyed"
  };

  public String toString () {
    String name0 = name == null ? "" : "\"" + name + "\" ";
    String hint0 = class_hint == null ? "(" : class_hint.toString () + " (";
    return "#Client " + name0 + hint0 + STATE_STRINGS [state] + ") "
      + super.toString ();
  }
}
