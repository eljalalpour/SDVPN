package me.elahe.msdvpn;

import java.util.Scanner;

import org.apache.felix.scr.annotations.*;
import org.onosproject.app.ApplicationService;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.Host;
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
	protected ApplicationService applicationService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	private HostService hostService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	protected FlowRuleService flowRuleService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	protected GroupService groupService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	protected PacketService packetService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	protected DeviceService deviceService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	protected TopologyService topologyService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	protected IntentService intentService;
	
	L2SwitchingMPLS l2SwitchingMPLS;

	@Activate
	protected void activate() {
		ApplicationId appId = applicationService.getId("me.elahe.msdvpn");
		L2SwitchingMPLS l2SwitchingMPLS = new L2SwitchingMPLS(appId, flowRuleService, groupService, deviceService,
				topologyService);
		L2SwitchingIntent l2SwitchingIntent = new L2SwitchingIntent(appId, intentService);
		ARPHandler arpHandler = new ARPHandler();
		hostService.addListener(l2SwitchingMPLS);
		//hostService.addListener(l2SwitchingIntent);
		packetService.addProcessor(arpHandler, PacketProcessor.director(2));

		log.info("Started");
	}

	public void dropRule(){
		Scanner sc;
		sc = new Scanner(System.in);
		System.out.println("print the name of two hosts");
		
		Host host1= null,host2 = null;
		String h1 = sc.nextLine();
		String h2 = sc.nextLine();
		
		for(Host host : hostService.getHosts()){
			if (host.toString().equals(h1)){
				host1 = host;
				
			}else if(host.toString().equals(h2)){
				host2 = host;
			}
		}
		l2SwitchingMPLS.dropRule(host1, host2);
		
		System.out.println("The path between" + h1 + "and" + h2 + "has been blocked");
		
	}
	@Deactivate
	protected void deactivate() {
		log.info("Stopped");
	}

}
