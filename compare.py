#!/usr/bin/python

import sys
import numpy as np
import matplotlib.pyplot as plt
import mpl_toolkits.mplot3d.axes3d as a3d

def main():
	if len(sys.argv) != 2:
		print 'usage: python %s result_file' % sys.argv[0]
		sys.exit()
	print >> sys.stderr, "comparing data!"
	print >> sys.stderr, "reading results..."
	infile = open(sys.argv[1], 'r')
	data = {}
	#label = None
	label = 'logLikelihood'
	count = 0
	for line in infile:
		if len(line) > 0 and not line.startswith('##'): # ignore ## comments and blanks
			# lines tab delineated - lag	n	corr
			data[label] = data.get(label, []) # init with list
			things = line.strip().split()
			data[label].append( (count, things[0]) )
			count = count + 1
	infile.close()

	# now data maps label -> list of (lag, n, corr) tuples
	# for each label, plot lag v correlation, and save image
	for lbl in data:
		# get data label, and continue using that label
		label = 'iterations vs logLikelihood'
		
		x = []
		z = []
		for t in data[lbl]:
			time = float(t[0])
			corr = float(t[1])
			x.append(time)
			z.append(corr)
		# graph lag, correlation
		fig, axes = plt.subplots()
		axes.plot(x,z)

		axes.set_title('Number iterations vs log-likelihood',fontsize=8)
		axes.set_xlabel('Iterations', fontsize = 8)
		axes.set_ylabel('Log-likelihood', fontsize = 8)
		label = label.replace(' ', '_')
		fig.savefig('iter-vs-loglike.png')
		plt.close()


if __name__ == "__main__":
	main()
