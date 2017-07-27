# !/usr/local/bin/python:

# python script for data analysis

import numpy as np
import sys
import os
import csv
from operator import itemgetter
import ConfigParser

#import timeit
#start_time = timeit.default_timer()

###reading the .csv files containing throughput values for multiple VMs###

#for one csv .csv file
# throughput = []
# file_name = 'throughputvm1host13.csv'
#
# with open(file_name, 'r') as f:
#     reader = csv.reader(f)
#     reader.next()
#     data = list(list(rec) for rec in csv.reader(f, delimiter=',')) #reads csv into a list of lists
#     for row in data:
#         throughput.append(row[3])
#
#     print throughput

#################### functions #####################

#for multiple .csv files

def processFile( directoryPath ):
    #for multiple files
    fileNumber = 0
    csvRows = []
    for csvFilename in os.listdir(directoryPath):
        if csvFilename.endswith('.csv'):
            csvFileObj = open(directoryPath + '/' + csvFilename)
            readerObj = csv.reader(csvFileObj)
            readerObj.next()
            data2 = list(list(rec) for rec in csv.reader(csvFileObj, delimiter=','))
            currentRow = []
            for row in data2:
               currentRow.append(float(row[3]))
            csvRows.append(currentRow)
            csvFileObj.close()
        fileNumber = fileNumber + 1
    return csvRows

######reading and parsing config file#######
config = ConfigParser.ConfigParser()
config.read('configfile.ini')

#print(config.sections())
#def readConfig ( id ):
    #file = openFile(configFile)
    #return parseValueFrom(file, id)
#################################################

# creating the histograms

def histogram ( floathost ):
    hist = []
    for i in range(len(floathost)):
        hist.append(np.histogram(floathost[i], bins=bins))
    return hist

# Extracting the counter values from the histogram
def extractcountervalues ( values ):
    extract = []
    i = 0
    j = 0
    for k in range(0, len(values)):
        i, j = values[k]
        i = i.astype('float')
        extract.append(i)
    return extract

# calculating the probabilities of the counter values

def calculate_probabilities ( value ):
    probability = []
    for i in range(0, len(value)):
      sum = value[i].sum()
      prob = value[i]/sum
      probability.append(prob)
    return probability

#calculating convolution

def calculate_convolution ( probability ):
    i1 = config.getint('bins','first_bin') #the first bin
    i2 = config.getint('bins','bin_size') #size of each bin/state
    i3 = config.getint('bins','max_edge') #the last bin
    result1 = probability[0]
    for i in range(1, len(probability)):
      result1 = np.convolve(result1, probability[i])
      state1  = np.arange(i1 * (i+1),i3 * (i+1)+1, i2)
    return [state1, result1]


#producing bwstates with probabilities

def bwcombinewithconv ( convresult ):
    bwconv = []
    for i in range(len(convresult[0])):
      bwconv.append([convresult[0][i], convresult[1][i]])
    return (bwconv)

#calculating the hightest bandwidth with non-zero value

def lastnonzerovalue(bwconv):
    highest = None
    for i in bwconv:
        if i[1] != 0.0:
            highest = i
    return highest

############### main functionality #################

hosts = []
hists = []
countervalues = []
probabilities = []
convolution = []
bwwithconv = []
highest = []

# definition of the bins and bin size

bin_size = config.getint('bins','bin_size'); #bin_size = 10000 for example;  # binsize has to be chosen carefully.
min_edge = config.getint('bins','min_edge'); #0
max_edge = config.getint('bins','max_edge'); #100000 for example; (use 10 Gigabit, 10 GbE)
N = (max_edge - min_edge) / bin_size;
Nplus1 = N + 1
bins = np.linspace(min_edge, max_edge, Nplus1)

# definition of hosts
#for arbitrary no. of hosts
path = sys.argv[1] #this is giving the next argument, which is the directory path after the script name which is sys.argv[0]
if os.path.exists(path):
    directoryPath = path
    for root, subdirs, files in os.walk(directoryPath):
        for subdir in sorted(subdirs): #its sorting the  hosts, from 0 to the last no. hosts as well as the VMs with highest throughput value at the beginning and the lowest at the end.
            hosts.append(processFile(subdir))

#####for 2 hosts#####
# strHost13 = '/home/mitalee/PoC_result/host1'
# strHost15 = '/home/mitalee/PoC_result/host2'
# hosts.append(processFile(strHost13))
# hosts.append(processFile(strHost15))
# end definition of hosts


####creating directory for the outputs####
os.mkdir('outputfolder')


for currentHost in hosts:
    # output the float values
    sys.stdout = open(os.path.join('outputfolder','host_' + str(hosts.index(currentHost))), 'w')
    print (currentHost)

    # calculate histogram and printing to file
    sys.stdout = open(os.path.join('outputfolder','hist_' + str(hosts.index(currentHost))), 'w')
    hists.append(histogram(currentHost))
    print (hists[-1])

    # calculate countervalues and printing to file
    countervalues.append(extractcountervalues(hists[-1]))
    sys.stdout = open(os.path.join('outputfolder','countervalues_' + str(hosts.index(currentHost))), 'w')
    print (countervalues[-1])

    # calculate probabilities and printing to file
    probabilities.append(calculate_probabilities(countervalues[-1]))
    sys.stdout = open(os.path.join('outputfolder','probabilities_' + str(hosts.index(currentHost))), 'w')
    print (probabilities[-1])

    # calculate convolution and printing to file
    convolution.append(calculate_convolution(probabilities[-1]))
    sys.stdout = open(os.path.join('outputfolder','overlay_' + str(hosts.index(currentHost))), 'w')
    print (convolution[-1])

    # combine bwstates and with convolution result and printing to file
    bwwithconv.append(bwcombinewithconv(convolution[-1]))
    sys.stdout = open(os.path.join('outputfolder','bwconv_' + str(hosts.index(currentHost))), 'w')
    print (bwwithconv[-1])

    #calculate the highest bwstates with non-zero value and printing to file
    highest.append(lastnonzerovalue(bwwithconv[-1]))
    sys.stdout = open(os.path.join('outputfolder','highest_' + str(hosts.index(currentHost))), 'w')
    print (highest[-1])

#plotting the histograms
# plt.hist(histvm1host13, normed=True, bins=bins) # it is showing the probability of the counts
# plt.ylabel('Counts')
# plt.show()


# comparing and producing the candidate host machines

for idx, val in enumerate(highest):
    val.append(idx)


highestSorted = sorted(highest, key=itemgetter(0))
sys.stdout = open(os.path.join('outputfolder','sorted'), 'w')
print (highestSorted)

overbookingfactor = config.getint('overbookingfactor','x');
for i in highestSorted:
    if i[0] > overbookingfactor: #8Gb is the  overbooking factor
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

sys.stdout = open(os.path.join('outputfolder','hostsList'), 'w')
print hostsList

#comparing the hosts for the first bandwidth state
# for val in highestSorted:
#     if val[0] == equalBandwidth:
#         if val[1] < lowestProbability:
#             lowestProbability = val[1]
#     else:
#         break
#
# sys.stdout = open('whichHost', 'w')
# for val in highestSorted:
#     print val[2]


###comparing two hosts###

# if highest1[0] == highest2[0]:
#     if highest1[1] < highest2[1]:
#         print 'host13, host15'
#     else:
#         print 'host15, host13'
# else:
#     if highest1[0] < highest2[0]:
#             print 'host13, host 15'
#     else:
#             print 'host15, host 13'

#elapsed = timeit.default_timer() - start_time
#print elapsed




