# In The Name Of God
# ========================================
# [] File Name : elahe.py
#
# [] Creation Date : 10-10-2015
#
# [] Created By : Elahe Jalalpour (el.jalalpour@gmail.com)
# =======================================
__author__ = 'Elahe Jalalpour'

from ryu.base import app_manager
from ryu.controller import ofp_event
from ryu.controller import dpset
from ryu.controller.handler import CONFIG_DISPATCHER, MAIN_DISPATCHER
from ryu.controller.handler import set_ev_cls
from ryu.ofproto import ofproto_v1_3
from ryu.lib.packet import packet
from ryu.lib.packet import ethernet
from ryu.lib.packet import ether_types


class ELTest(app_manager.RyuApp):

    def __init__(self, *args, **kwargs):
        super(ELTest, self).__init__(*args, **kwargs)
        self.mac_table = {}

    @set_ev_cls(dpset.EventDP, dpset.DPSET_EV_DISPATCHER)
    def handler_datapath(self, ev):
        ofproto = ev.dp.ofproto
        parser = ev.dp.ofproto_parser

        # install table-miss flow entry
        #
        # We specify NO BUFFER to max_len of the output action due to
        # OVS bug. At this moment, if we specify a lesser number, e.g.,
        # 128, OVS will send Packet-In with invalid buffer_id and
        # truncated packet data. In that case, we cannot output packets
        # correctly.  The bug has been fixed in OVS v2.1.0.
        if ev.enter:
            match = parser.OFPMatch()
            actions = [parser.OFPActionOutput(ofproto.OFPP_CONTROLLER, ofproto.OFPCML_NO_BUFFER)]
            self.add_flow(ev.dp, 0, match, actions)

    @set_ev_cls(ofp_event.EventOFPPacketIn, MAIN_DISPATCHER)
    def _packet_in_handler(self, ev):
        msg = ev.msg
        data = msg.data
        datapath = msg.datapath
        pkt = packet.Packet(data)
        eth = pkt.get_protocols(ethernet.ethernet)[0]

        in_port = msg.match['in_port']
        dpid = datapath.id
        self.mac_table.setdefault(dpid, {})
        (self.mac_table[dpid])[in_port] = eth.src


        print(self.mac_table)

    def add_flow(self, datapath, priority, match, actions):
        ofproto = datapath.ofproto
        # an object represents the openflow protocol
        parser = datapath.ofproto_parser
        # an object represents the openflow protocol parser
        inst = [parser.OFPInstructionActions(ofproto.OFPIT_APPLY_ACTIONS, actions)]

        mod = parser.OFPFlowMod(datapath=datapath, priority=priority,
                                match=match, instructions=inst)
        datapath.send_msg(mod)
