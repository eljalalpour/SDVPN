#!/usr/bin/env python

"""
mn.py:
    Script for running our sample topology on mininet
    and connect it into contorller on remote host.
Usage (example uses IP = 192.168.1.2):
    From the command line:
        sudo python mn.py --ip 192.168.1.2
"""
from functools import partial
from mininet.node import Host

from mininet.net import Mininet
from mininet.net import CLI
from mininet.log import setLogLevel
from mininet.node import RemoteController
from mininet.node import OVSSwitch
from mininet.topo import Topo
import argparse

#
# We want to build following topology:
# h1 --- s1 -+- s2 --- h4
# h2 --- |   |   | --- h5
#            s5
#            |
# h3 --- s3 -+- s4 --- h6
#

class VLANHost(Host):
    "Host connected to VLAN interface"

    def config(self, vlan=100, **params):
        """Configure VLANHost according to (optional) parameters:
           vlan: VLAN ID for default interface"""

        r = super(VLANHost, self).config(**params)

        intf = self.defaultIntf()
        # remove IP from default, "physical" interface
        self.cmd('ifconfig %s inet 0' % intf)
        # create VLAN interface
        self.cmd('vconfig add %s %d' % (intf, vlan))
        # assign the host's IP to the VLAN interface
        self.cmd('ifconfig %s.%d inet %s' % (intf, vlan, params['ip']))
        # update the intf name and host's intf map
        newName = '%s.%d' % (intf, vlan)
        # update the (Mininet) interface to refer to VLAN interface name
        intf.name = newName
        # add VLAN interface to host's name to intf map
        self.nameToIntf[newName] = intf

        return r


class SampleTopology(Topo):
    """
    Subclass of mininet Topo class for
    creating path topology.
    """
    def build(self, *args, **params):
        s1 = self.addSwitch(name='s1')
        s2 = self.addSwitch(name='s2')
        s3 = self.addSwitch(name='s3')
        s4 = self.addSwitch(name='s4')
        s5 = self.addSwitch(name='s5')
        #h1 = self.addHost(name='h1')
        #h2 = self.addHost(name='h2')
        h1 = self.addHost(name='h1', cls=VLANHost, vlan=102)
        h2 = self.addHost(name='h2', cls=VLANHost, vlan=102)
        h3 = self.addHost(name='h3')
        h4 = self.addHost(name='h4')
        h5 = self.addHost(name='h5')
        h6 = self.addHost(name='h6')
        self.addLink(h1, s1)
        self.addLink(h2, s1)
        self.addLink(h4, s2)
        self.addLink(h5, s2)
        self.addLink(h3, s3)
        self.addLink(h6, s4)
        self.addLink(s1, s5)
        self.addLink(s2, s5)
        self.addLink(s3, s5)
        self.addLink(s4, s5)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--ip', dest='ip', help='onos', default='172.16.79.1', type=str)
    cli_args = parser.parse_args()

    setLogLevel('info')

    switch = partial(OVSSwitch, protocols='OpenFlow13')
    net = Mininet(topo=SampleTopology(), controller=RemoteController('ONOS', ip=cli_args.ip, port=6633), switch=switch)
    net.start()
    CLI(net)
    net.stop()
