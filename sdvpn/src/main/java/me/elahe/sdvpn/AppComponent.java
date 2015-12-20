/*
 * Copyright 2014 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.elahe.sdvpn;

import org.apache.felix.scr.annotations.*;
import org.onlab.packet.Ethernet;
import org.onosproject.app.ApplicationService;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.host.HostService;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.packet.PacketPriority;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SDVPN application component.
 */
@Component(immediate = true)
public class AppComponent {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	private HostService hostService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	protected IntentService intentService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	protected ApplicationService applicationService;
	
	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	protected PacketService packetService;

	@Activate
	protected void activate() {
		ApplicationId appId = applicationService.getId("me.elahe.sdvpn");
		HostHandler handler = new HostHandler(intentService, appId, hostService, packetService);
		hostService.addListener(handler);
		packetService.addProcessor(handler, PacketProcessor.director(2));
		
		TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
		selector.matchEthType(Ethernet.TYPE_IPV4);
		packetService.requestPackets(selector.build(), PacketPriority.REACTIVE, appId);
		
		log.info("Started");
	}

	@Deactivate
	protected void deactivate() {
		log.info("Stopped");
	}
}
