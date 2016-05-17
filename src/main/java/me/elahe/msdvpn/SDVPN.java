package me.elahe.msdvpn;

import org.apache.felix.scr.annotations.*;
import org.onosproject.app.ApplicationService;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.group.GroupService;
import org.onosproject.net.host.HostService;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.onosproject.net.topology.TopologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Skeletal ONOS application component.
 */
@Component(immediate = true)
public class SDVPN {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	private ApplicationService applicationService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	private HostService hostService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	private FlowRuleService flowRuleService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	private GroupService groupService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	private PacketService packetService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	private DeviceService deviceService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	private TopologyService topologyService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	private IntentService intentService;

	@Activate
	protected void activate() {
		ApplicationId appId = applicationService.getId("me.elahe.msdvpn");
		L2SwitchingVLAN l2SwitchingVLAN = new L2SwitchingVLAN(appId, flowRuleService, groupService, deviceService,
			topologyService);
		L2SwitchingMPLS l2SwitchingMPLS = new L2SwitchingMPLS(appId, flowRuleService, groupService, deviceService,
			topologyService);
		L2SwitchingIntent l2SwitchingIntent = new L2SwitchingIntent(appId, intentService);
		ARPHandler arpHandler = new ARPHandler();
		//hostService.addListener(l2SwitchingVLAN);
		hostService.addListener(l2SwitchingMPLS);
		//hostService.addListener(l2SwitchingIntent);
		packetService.addProcessor(arpHandler, PacketProcessor.director(2));

		log.info("Started");
	}

	@Deactivate
	protected void deactivate() {
		log.info("Stopped");
	}

}
