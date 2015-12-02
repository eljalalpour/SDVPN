package me.elahe.sdvpn;

import org.onosproject.core.ApplicationId;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Host;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.intent.PointToPointIntent;
import org.onosproject.sdnip.IntentSynchronizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Objects;

public class HostHandler implements HostListener {
	private final Logger log = LoggerFactory.getLogger(getClass());

	private IntentSynchronizer intentSynchronizer;
	private ApplicationId appId;

	private ArrayList<Host> hosts = new ArrayList<>();

	public HostHandler(IntentSynchronizer intentSynchronizer, ApplicationId appId) {
		this.intentSynchronizer = intentSynchronizer;
		this.appId = appId;
	}

	@Override
	public void event(HostEvent ev) {
		if (ev.type() == HostEvent.Type.HOST_ADDED) {
			log.info(ev.subject().toString());
			hosts.stream().filter(host -> !Objects.equals(host.id().toString(), ev.subject().id().toString())).forEach(host -> {
				PointToPointIntent p1 = PointToPointIntent.builder()
					.egressPoint(ConnectPoint.hostConnectPoint(
						host.id().toString() + "/" +
							host.location().port().toString()
					))
					.ingressPoint(ConnectPoint.hostConnectPoint(
						ev.subject().id().toString() + "/" +
							ev.subject().location().port().toString()
					))
					.appId(appId)
					.build();
				PointToPointIntent p2 = PointToPointIntent.builder()
					.ingressPoint(ConnectPoint.hostConnectPoint(
						host.id().toString() + "/" +
							host.location().port().toString()
					))
					.egressPoint(ConnectPoint.hostConnectPoint(
						ev.subject().id().toString() + "/" +
							ev.subject().location().port().toString()
					))
					.appId(appId)
					.build();
				intentSynchronizer.submit(p1);
				intentSynchronizer.submit(p2);
			});
			if (!hosts.contains(ev.subject())) {
				hosts.add(ev.subject());
			}
		}
	}

}
