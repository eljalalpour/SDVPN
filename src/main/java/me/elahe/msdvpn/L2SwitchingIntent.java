package me.elahe.msdvpn;

import org.onlab.packet.VlanId;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.Host;
import org.onosproject.net.HostId;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.intent.HostToHostIntent;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.Key;

import java.util.HashMap;
import java.util.*;

public class L2SwitchingIntent implements HostListener {
	private IntentService intentService;
	private ApplicationId appId;

	private Map<VlanId, List<Host>> vLanIdMap;

	public L2SwitchingIntent(ApplicationId appId, IntentService intentService) {
		this.appId = appId;

		this.intentService = intentService;

		this.vLanIdMap = new HashMap<>();
	}


	public void event(HostEvent event) {
		if (event.type() == HostEvent.Type.HOST_ADDED) {
			Host host = event.subject();

			/* When we see new VLan */
			if (!vLanIdMap.containsKey(host.vlan())) {
				vLanIdMap.put(host.vlan(), new ArrayList<>());
			}

			/* And we do this for all our new hosts :) */

			/*
			 * Create path between our new host and all old hosts
			 * that has the same VLanID
			 */
			for (Host h : vLanIdMap.get(host.vlan())) {
				intentService.submit(tunnelBuilder(host, h));
			}

			/* Add new host into VLan based host list */
			vLanIdMap.get(host.vlan()).add(host);
		}
	}

	private HostToHostIntent tunnelBuilder(Host src, Host dst) {
		HostId srcId = src.id();
		HostId dstId = dst.id();

		String strKey = "uni-" + src.id() + "-->" + dst.id();
		Key key = Key.of(strKey, appId);

		TrafficSelector selector = DefaultTrafficSelector.emptySelector();

		TrafficTreatment treatment = DefaultTrafficTreatment.emptyTreatment();

		return HostToHostIntent.builder().appId(appId).key(key).one(srcId).two(dstId).selector(selector)
			.treatment(treatment).priority(Intent.MAX_PRIORITY).build();
	}

}
