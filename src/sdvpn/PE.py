# In The Name Of God
# ========================================
# [] File Name : pe.py
#
# [] Creation Date : 10/31/15
#
# [] Created By : Elahe Jalalpour (el.jalalpour@gmail.com)
# =======================================
__author__ = 'Elahe Jalalpour'


class PE:
    """
    :type mac_table: dict[str, int]
    """
    def __init__(self, datapath):
        self.datapath = datapath
        self.mac_table = {}

    def add_mac(self, mac, port):
        self.mac_table[mac] = port

    def get_mac(self, mac):
        return self.mac_table[mac]

