package linoleum.application.event;

import java.util.EventListener;

public interface ClassPathListener extends EventListener {
	void classPathChanged(ClassPathChangeEvent e);
}
