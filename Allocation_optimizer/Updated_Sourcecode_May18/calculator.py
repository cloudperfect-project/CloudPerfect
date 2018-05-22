import numpy as np
import sys
import os
import csv
from operator import itemgetter
import ConfigParser



class Calculator:

    def __init__(self, configfile):
        self.config = ConfigParser.ConfigParser()
        self.config.read(configfile)


    # creating the histograms
    def histogram(self, floathost):
        hist = []
        for i in range(len(floathost)):
            hist.append(np.histogram(floathost[i], bins=self.bins))
        return hist

    # Extracting the counter values from the histogram

    def extractcountervalues(self, values):
        extract = []
        i = 0
        j = 0
        for k in range(0, len(values)):
            i, j = values[k]
            i = i.astype('float')
            extract.append(i)
        return extract

    # calculating the probabilities of the counter values
    def calculate_probabilities(self, value):
        probability = []
        for i in range(0, len(value)):
            sum = value[i].sum()
            prob = value[i] / sum
            probability.append(prob)
        return probability

    # calculating convolution

    def calculate_convolution(self, probability):
        i1 = self.config.getint('bins', 'first_bin')  # the first bin
        i2 = self.config.getint('bins', 'bin_size')  # size of each bin/state
        i3 = self.config.getint('bins', 'max_edge')  # the last bin
        result1 = probability[0]
        for i in range(1, len(probability)):
            result1 = np.convolve(result1, probability[i])
            state1 = np.arange(i1 * (i + 1), i3 * (i + 1) + 1, i2)
        return [state1, result1]

    # producing bwstates with probabilities

    def bwcombinewithconv(self, convresult):
        bwconv = []
        for i in range(len(convresult[0])):
            bwconv.append([convresult[0][i], convresult[1][i]])
        return (bwconv)

    # calculating the hightest bandwidth with non-zero value
    def lastnonzerovalue(self, bwconv):
        highest = None
        for i in bwconv:
            if i[1] != 0.0:
                highest = i
        return highest


############### main functionality #################

    def doOldCalculation(self, hosts):
        hists = []
        countervalues = []
        probabilities = []
        convolution = []
        bwwithconv = []
        highest = []

        # save the "real" stdout (Terminal, not a file!)
        sysStdout = sys.stdout

        # definition of the bins and bin size

        self.bin_size = self.config.getint('bins', 'bin_size');  # bin_size = 10000 for example;  # binsize has to be chosen carefully.
        self.min_edge = self.config.getint('bins', 'min_edge');  # 0
        self.max_edge = self.config.getint('bins', 'max_edge');  # 100000 (use 10 Giga, 10 GbE)
        self.N = (self.max_edge - self.min_edge) / self.bin_size;
        self.Nplus1 = self.N + 1
        self.bins = np.linspace(self.min_edge, self.max_edge, self.Nplus1)

        ####creating directory for the outputs####
        if not os.path.isdir('outputfolder'):
            os.mkdir('outputfolder')

        for currentHost in hosts:
            sys.stdout = sysStdout
            print "[ -> ] Processing server " + str(hosts.index(currentHost))
            # output the float values
            sys.stdout = open(os.path.join('outputfolder', 'host_' + str(hosts.index(currentHost))), 'w')
            print (currentHost)

            # calculate histogram and printing to file
            sys.stdout = open(os.path.join('outputfolder', 'hist_' + str(hosts.index(currentHost))), 'w')
            hists.append(self.histogram(currentHost))
            print (hists[-1])

            # calculate countervalues and printing to file
            countervalues.append(self.extractcountervalues(hists[-1]))
            sys.stdout = open(os.path.join('outputfolder', 'countervalues_' + str(hosts.index(currentHost))), 'w')
            print (countervalues[-1])

            # calculate probabilities and printing to file
            probabilities.append(self.calculate_probabilities(countervalues[-1]))
            sys.stdout = open(os.path.join('outputfolder', 'probabilities_' + str(hosts.index(currentHost))), 'w')
            #sys.stdout = sysStdout
            print (probabilities[-1])

            # calculate convolution and printing to file
            convolution.append(self.calculate_convolution(probabilities[-1]))
            sys.stdout = open(os.path.join('outputfolder', 'overlay_' + str(hosts.index(currentHost))), 'w')
            #sys.stdout = sysStdout
            #print (convolution[-1])

            # combine bwstates and with convolution result and printing to file
            bwwithconv.append(self.bwcombinewithconv(convolution[-1]))
            #sys.stdout = open(os.path.join('outputfolder', 'bwconv_' + str(hosts.index(currentHost))), 'w')
            sys.stdout = sysStdout
            print (bwwithconv[-1])

            # calculate the highest bwstates with non-zero value and printing to file
            highest.append(self.lastnonzerovalue(bwwithconv[-1]))
            sys.stdout = open(os.path.join('outputfolder', 'highest_' + str(hosts.index(currentHost))), 'w')
            print (highest[-1])

        # comparing and producing the candidate host machines

        for idx, val in enumerate(highest):
            val.append(idx)

        highestSorted = sorted(highest, key=itemgetter(0))
        sys.stdout = open(os.path.join('outputfolder', 'sorted'), 'w')
        print (highestSorted)

        overbookingfactor = self.config.getint('overbookingfactor', 'x');
        for i in highestSorted:
            if i[0] > overbookingfactor:  # 8Gb is the  overbooking factor
                print 'highestSorted ' + i[2] + 'is crossing the limit'

        equalBandwidth = highestSorted[0][0]
        lowestProbability = 1

        # [ [bandwidth, lowest probability. host]]
        hostsList = []

        for val in highestSorted:
            if (len(hostsList) > 0):
                if hostsList[-1][0] == val[0]:
                    if val[1] < hostsList[-1][1]:
                        hostsList[-1][1] = val[1]
                        hostsList[-1][2] = val[2]
                else:
                    hostsList.append(val)
            else:
                hostsList.append(val)

        #sys.stdout = open(os.path.join('outputfolder', 'hostsList'), 'w')
        sys.stdout = sysStdout
        print "Serverlist" + str(hostsList)


    def setHostData(self, data):
        self.data = data
