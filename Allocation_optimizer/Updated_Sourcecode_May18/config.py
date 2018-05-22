import ConfigParser


class ConfigInitializer:
    def createConfig(self, file, min_edge, max_edge, bin_size, first_bin, overbookingfactor):
        config = ConfigParser.ConfigParser()
        config.add_section('bins')
        config.set('bins', 'min_edge', min_edge)
        config.set('bins', 'max_edge', max_edge)
        config.set('bins', 'bin_size', bin_size)
        config.set('bins', 'first_bin', first_bin)

        config.add_section('overbookingfactor')
        config.set('overbookingfactor', 'x', overbookingfactor)

        configfile = open(file, 'w')
        config.write(configfile)
