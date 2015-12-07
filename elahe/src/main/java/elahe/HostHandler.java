package elahe;

import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostHandler implements HostListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

	
	@Override
	public void event(HostEvent ev) {
		if (ev.type() == HostEvent.Type.HOST_ADDED) {
			log.info(ev.subject().toString());
		}
	}

}
