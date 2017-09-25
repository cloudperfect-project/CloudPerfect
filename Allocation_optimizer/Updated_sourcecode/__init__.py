from calculator import Calculator
from config import ConfigInitializer
from localfileprovider import LocalFileProvider

configFile = "/home/mitalee/python_script/configfile.ini"

cfgInit = ConfigInitializer()
cfgInit.createConfig(configFile, 0, 100000, 10000, 10000, 8000000000)


lfp = LocalFileProvider()
lfp.setPath("/home/mitalee/bandwidth_usage_values")
hosts = lfp.get_TimeseriesBandwidthvalues()
calculator = Calculator(configFile)
calculator.doOldCalculation(hosts)



