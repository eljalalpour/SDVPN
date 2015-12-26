package me.elahe.sdvpn;

import org.onlab.packet.Ethernet;
import org.onlab.packet.MacAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.Host;
import org.onosproject.net.HostId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onosproject.net.intent.HostToHostIntent;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.Key;
import org.onosproject.net.packet.DefaultOutboundPacket;
import org.onosproject.net.packet.InboundPacket;
import org.onosproject.net.packet.OutboundPacket;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostHandler implements HostListener, PacketProcessor {
	private final Logger log = LoggerFactory.getLogger(getClass());
	private IntentService intentService;
	private HostService hostService;
	private ApplicationId appId;
	private PacketService packetService;

	public HostHandler(IntentService intentService, ApplicationId appId, HostService hostService, PacketService packetService) {
		this.intentService = intentService;
		this.appId = appId;
		this.hostService = hostService;
		this.packetService = packetService;
	}

	@Override
	public void process(PacketContext context) {
		// Stop processing if the packet has been handled, since we
		// can't do any more to it.
		if (context.isHandled()) {
			return;
		}
		InboundPacket pkt = context.inPacket();
		Ethernet ethPkt = pkt.parsed();

		if (ethPkt == null) {
			return;
		}

		if (ethPkt.getDestinationMAC().equals(MacAddress.BROADCAST)) {
			flood(context);
			return;
		}

		HostId dstId = HostId.hostId(ethPkt.getDestinationMAC());

		// Do we know who this is for? If not, flood and bail.
		Host dst = hostService.getHost(dstId);
		if (dst == null) {
			flood(context);
			return;
		}

		//packetOut(context, dst.location().port());
		if (dst.vlan().toShort() == ethPkt.getVlanID()) {
			forwardPacketToDst(context, dst);
		}
	}

	private void forwardPacketToDst(PacketContext context, Host dst) {
		TrafficTreatment treatment = DefaultTrafficTreatment.builder().setOutput(dst.location().port()).build();
		OutboundPacket packet = new DefaultOutboundPacket(dst.location().deviceId(),
			treatment, context.inPacket().unparsed());
		packetService.emit(packet);
		log.info("sending packet: {}", packet);
	}


	// Floods the specified packet if permissible.
	private void flood(PacketContext context) {
		packetOut(context, PortNumber.FLOOD);
	}

	// Sends a packet out the specified port.
	private void packetOut(PacketContext context, PortNumber portNumber) {
		context.treatmentBuilder().setOutput(portNumber);
		context.send();
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
