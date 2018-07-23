from calculator import Calculator
from config import ConfigInitializer
from localfileprovider import LocalFileProvider

configFile = "/home/mitalee/Documents/Updated_sourcecode/configfile.ini"

cfgInit = ConfigInitializer()
#cfgInit.createConfig(configFile, 0, 10000000000, 10000, 10000, 8000000000)
cfgInit.createConfig(configFile, 0, 1000000000, 10000, 10000, 8000000000)



lfp = LocalFileProvider()
lfp.setPath("/home/mitalee/Documents/Updated_sourcecode")
print "----------------------------------------------------------"
print "- [Stage 1] Collecting data                              -"
print "----------------------------------------------------------"
hosts = lfp.get_TimeseriesBandwidthvalues()

print "----------------------------------------------------------"
print "- [Stage 2] Calculating results                          -"
print "----------------------------------------------------------"
calculator = Calculator(configFile)
calculator.doOldCalculation(hosts)



