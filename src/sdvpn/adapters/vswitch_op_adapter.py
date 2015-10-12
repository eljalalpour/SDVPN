# In The Name Of God
# ========================================
# [] File Name : vswitch_op_adapter.py
#
# [] Creation Date : 04-10-2015
#
# [] Created By : Elahe Jalalpour (el.jalalpour@gmail.com)
# =======================================
__author__ = 'Elahe Jalalpour'

import logging
from ryu.exception import OFPUnknownVersion
from ryu.lib import ofctl_v1_3, ofctl_v1_2, ofctl_v1_0
from ryu.lib import dpid as dpid_lib
from ryu.ofproto import ofproto_v1_3, ofproto_v1_2, ofproto_v1_0
from ryu.controller import dpset
from ryu.controller.handler import set_ev_cls
from ryu.controller.controller import Datapath
from ryu.base import app_manager
from ryu.app.wsgi import ControllerBase, WSGIApplication, route


class RESTVswitchAdapter(app_manager.RyuApp):
    _CONTEXTS = {
        'wsgi': WSGIApplication,
    }

    @set_ev_cls(dpset.EventDP, dpset.DPSET_EV_DISPATCHER)
    def handler_datapath(self, ev):
        """
        dpset.EventDP: An event class to notify connect/disconnect of a switch.
        :param ev: has following attribute
        dp: A ryu.controller.controller.Datapath instance of the switch.
        enter: True when the switch connected to our controller. False for disconnect.
        :return:
         """
        if ev.enter:
            PEController.register_ofs(ev.dp)
        else:
            PEController.un_register_ofs(ev.dp)


class PE:
    """
    :type dp: Datapath
    :param: dp: A ryu.controller.controller.Datapath instance of the switch.

    :type ofctl: ofctl_v1_3
    """
    _OFCTL = {ofproto_v1_0.OFP_VERSION: ofctl_v1_0,
              ofproto_v1_2.OFP_VERSION: ofctl_v1_2,
              ofproto_v1_3.OFP_VERSION: ofctl_v1_3,
              }

    def __init__(self, dp):
        self.dp = dp

        version = dp.ofproto.OFP_VERSION
        if version not in self._OFCTL:
            raise OFPUnknownVersion(version=version)
        self.ofctl = self._OFCTL[version]


class PEController(ControllerBase):
    _OFS_LIST = {}
    _LOGGER = None

    def __init__(self, req, link, data, **config):
        super(PEController, self).__init__(req, link, data, **config)
        self.dpset = data['dpset']
        self.waiters = data['waiters']

    @classmethod
    def set_logger(cls, logger):
        cls._LOGGER = logger
        cls._LOGGER.propagate = False
        handler = logging.StreamHandler()
        fmt_str = '[FW][%(levelname)s] %(message)s'
        handler.setFormatter(logging.Formatter(fmt_str))
        cls._LOGGER.addHandler(handler)

    @staticmethod
    def register_ofs(dp):
        dpid_str = dpid_lib.dpid_to_str(dp.id)
        try:
            pe_ofs = PE(dp)
        except OFPUnknownVersion as message:
            PEController._LOGGER.info('dpid=%s: %s', dpid_str, message)
            return

        PEController._OFS_LIST.setdefault(dp.id, pe_ofs)

        pe_ofs.set_disable_flow()
        pe_ofs.set_arp_flow()
        pe_ofs.set_log_enable()
        PEController._LOGGER.info('dpid=%s: Join as provider equipment to SDVPN.', dpid_str)
