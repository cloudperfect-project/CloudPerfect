import ConfigParser
config = ConfigParser.ConfigParser()

config.add_section('bins')
config.set('bins', 'min_edge', 0)
config.set('bins', 'max_edge', 10000000000)
config.set('bins', 'bin_size', 10000)
config.set('bins', 'first_bin', 10000)

config.add_section('overbookingfactor')
config.set('overbookingfactor', 'x', 8000000000)

with open('configfile.ini', 'w') as configfile:
  config.write(configfile)

