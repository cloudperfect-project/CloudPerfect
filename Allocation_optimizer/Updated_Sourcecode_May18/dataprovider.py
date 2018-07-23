from abc import ABCMeta, abstractmethod


class DataProvider(object):
    """ It provides VM and Host IDs and timeseries bandwidth values


    Attributes:
        VMandHostIDs : A List of string values representing the identification values of the virtual machines and their respective host machines.
        TimeseriesBandwidthvalues : A set of .CSV files representing the timeseries bandwidth usage values of the virtual machines for last 24 hours.
    """

    __metaclass__ = ABCMeta

    VMandHostIDs = []
    TimeseriesBandwidthalues = []

    def __init__(self, VMandHostIDs, TimeseriesBandwidthvalues):
        self.VMandHostIDs = VMandHostIDs
        self.TimeseriesBandwidthvalues = TimeseriesBandwidthvalues

    @abstractmethod
    def get_IDs(self):
        pass
        """ Return a list of the VM IDs associated with the host IDs """
    def get_TimeseriesBandwidthvalues(self):
        pass
        """ Return a list of realtime network bandwidth usgae values for last 24 hours """



