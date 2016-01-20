package linoleum.application.event;

import java.util.EventObject;

public class ClassPathChangeEvent extends EventObject {

	public ClassPathChangeEvent(final Object source) {
		super(source);
	}
}
