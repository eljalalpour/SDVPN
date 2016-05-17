"""
vlan.py: Host subclass that uses a VLAN tag for the default interface.
Dependencies:
    This class depends on the "vlan" package
    $ sudo apt-get install vlan
"""

from mininet.node import Host

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
        new_name = '%s.%d' % (intf, vlan)
        # update the (Mininet) interface to refer to VLAN interface name
        intf.name = new_name
        # add VLAN interface to host's name to intf map
        self.nameToIntf[new_name] = intf

        return r
