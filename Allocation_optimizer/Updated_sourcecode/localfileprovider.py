import csv
import os
import sys

from dataprovider import DataProvider


class LocalFileProvider(DataProvider):

    def __init__(self):
        self.TimeseriesBandwidthvalues = []
        self.path = ''
    def get_IDs(self):
        pass
        #how to check the IDs from Openstack Compute API
    def setPath(self, path):
        self.path = path

    def get_TimeseriesBandwidthvalues(self):
        # definition of hosts
        # for arbitrary no. of hosts

        if os.path.exists(self.path):
            directoryPath = self.path
            for root, subdirs, files in os.walk(directoryPath):
                for subdir in sorted(
                        subdirs):  # its sorting the  hosts, from 0 to the last no. hosts as well as the VMs with highest throughput value at the beginning and the lowest at the end.
                    self.TimeseriesBandwidthvalues.append(self.readFile(root + '/' + subdir))
        return self.TimeseriesBandwidthvalues


    def readFile(self, directoryPath):
        fileNumber = 0
        result = []
        for csvFilename in os.listdir(directoryPath):
            if csvFilename.endswith('.csv'):
                csvFileObj = open(directoryPath + '/' + csvFilename)
                readerObj = csv.reader(csvFileObj)
                readerObj.next()
                data2 = list(list(rec) for rec in csv.reader(csvFileObj, delimiter=','))
                currentRow = []
                for row in data2:
                    currentRow.append(float(row[3]))
                result.append(currentRow)
                csvFileObj.close()
            fileNumber = fileNumber + 1
        return result