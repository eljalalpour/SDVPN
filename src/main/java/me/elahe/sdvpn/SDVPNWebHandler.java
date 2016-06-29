package me.elahe.sdvpn;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;
import org.onlab.packet.MacAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.Host;
import org.onosproject.net.flow.*;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onosproject.ui.RequestHandler;
import org.onosproject.ui.UiMessageHandler;

import java.util.Collection;

/**
 * message handler for the requests from the applications web ui
 */
public class SDVPNWebHandler extends UiMessageHandler implements HostListener {
	private FlowRuleService flowRuleService;
	private HostService hostService;
	private ApplicationId appId;

	SDVPNWebHandler(ApplicationId appId, FlowRuleService flowRuleService, HostService hostService) {
		this.flowRuleService = flowRuleService;
		this.hostService = hostService;
		this.appId = appId;
	}

	@Override
	protected Collection<RequestHandler> createRequestHandlers() {
		return ImmutableSet.of(
			new DropRuleRequestHandler()
		);
	}

	/**
	 *
	 * @param hostEvent Host event that we use to detect adding new hosts to the network
     */
	@Override
	public void event(HostEvent hostEvent) {
		if (hostEvent.type() == HostEvent.Type.HOST_ADDED) {
			Host host = hostEvent.subject();
			ObjectNode hostMessage = objectNode();
			hostMessage.put("mac", host.mac().toString());
			hostMessage.put("vlan", host.vlan().toShort());
			hostMessage.put("ip", host.ipAddresses().toString());
			connection().sendMessage("hostEvent", 0, hostMessage);
		}
	}

	// handler for drop request

	/**
	 *
	 */
	private final class DropRuleRequestHandler extends RequestHandler {

		private DropRuleRequestHandler() {
			super("dropRule");
		}

		/**
		 *
		 * @param sid
		 * @param payload
         */
		@Override
		public void process(long sid, ObjectNode payload) {
			String h1 = payload.get("h1").asText();
			String h2 = payload.get("h2").asText();

			Host host1 = (Host) hostService.getHostsByMac(MacAddress.valueOf(h1)).toArray()[0];
			Host host2 = (Host) hostService.getHostsByMac(MacAddress.valueOf(h2)).toArray()[0];

			TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
			TrafficSelector selector = selectorBuilder.matchEthDst(host2.mac()).build();
			TrafficTreatment.Builder treatmentBuilder = DefaultTrafficTreatment.builder();
			TrafficTreatment treatment = treatmentBuilder.drop().build();
			FlowRule.Builder flowBuilder = new DefaultFlowRule.Builder();
			flowBuilder.forDevice(host1.location().deviceId()).withSelector(selector).withTreatment(treatment);
			flowBuilder.fromApp(appId);
			flowBuilder.makePermanent();
			flowBuilder.withPriority(30);
			FlowRule flowRule = flowBuilder.build();
			flowRuleService.applyFlowRules(flowRule);
		}
	}
}
