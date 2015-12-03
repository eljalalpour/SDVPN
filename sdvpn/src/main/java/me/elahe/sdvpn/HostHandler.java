package me.elahe.sdvpn;

import org.onosproject.core.ApplicationId;
import org.onosproject.net.Host;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onosproject.net.intent.HostToHostIntent;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostHandler implements HostListener {
	private final Logger log = LoggerFactory.getLogger(getClass());

	private IntentService intentService;
	private HostService hostService;
	private ApplicationId appId;

	private static final int PRIORITY_OFFSET = 1000;


	public HostHandler(IntentService intentService, ApplicationId appId, HostService hostService) {
		this.intentService = intentService;
		this.appId = appId;
		this.hostService = hostService;
	}

	@Override
	public void event(HostEvent ev) {
		if (ev.type() == HostEvent.Type.HOST_ADDED) {

			for (Host host : hostService.getHosts()) {
				if (host.id().equals(ev.subject().id()))
					continue;

				log.info("************************");
				log.info(host.toString());
				log.info(ev.subject().toString());
				log.info("************************");

				HostToHostIntent h = tunnelBuilder(host, ev.subject());

				intentService.submit(h);
			}
		}
	}

	private HostToHostIntent tunnelBuilder(Host src, Host dst) {
		String keyString = "uni-" +
			src.id() +
			"-->" +
			dst.id();
		Key key = Key.of(keyString, appId);

		TrafficSelector selector = DefaultTrafficSelector.builder()
			.matchEthSrc(src.mac())
			.matchEthDst(dst.mac())
			.build();

		TrafficTreatment treatment = DefaultTrafficTreatment.emptyTreatment();

		return HostToHostIntent.builder()
			.one(src.id())
			.two(dst.id())
			.priority(PRIORITY_OFFSET)
			.selector(selector)
			.treatment(treatment)
			.appId(appId)
			.key(key)
			.build();
	}

}
