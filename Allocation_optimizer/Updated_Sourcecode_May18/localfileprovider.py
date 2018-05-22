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
                    print "[ -> ] Searching directory: " + root + '/' + subdir
                    currentHost = self.readFile(root + '/' + subdir)
                    if currentHost:
                        self.TimeseriesBandwidthvalues.append(currentHost)
        return self.TimeseriesBandwidthvalues


    def readFile(self, directoryPath):
        result = []
        for csvFilename in os.listdir(directoryPath):
            if csvFilename.endswith('.rx'):
                print "[ OK ] found VM: " + csvFilename.replace('.rx', '');
                rxFileObj = open(directoryPath + '/' + csvFilename) 
                txFileObj = open(directoryPath + '/' + csvFilename.replace('.rx', '.tx'))
                rxReaderObj = csv.reader(rxFileObj)
                txReaderObj = csv.reader(txFileObj)
                rxReaderObj.next()
                txReaderObj.next()
                dataRx = list(list(rec) for rec in csv.reader(rxFileObj, delimiter=' '))
                dataTx = list(list(rec) for rec in csv.reader(txFileObj, delimiter=' '))
                currentRow = []
                for rowRx, rowTx in zip(dataRx, dataTx):
                    if rowRx != []:
                        if rowTx != []:
                            currentRow.append(float(rowRx[1]) + float(rowTx[1])) # currentRow.append(float(rowRx[1]) + float(rowTx[1]))
                result.append(currentRow)
                rxFileObj.close()
                txFileObj.close()
        return result
