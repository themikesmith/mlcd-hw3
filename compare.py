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
			data[label].append( (count, count, things[0]) )
			count = count + 1
	infile.close()

	# now data maps label -> list of (lag, n, corr) tuples
	# for each label, plot lag v correlation, and save image
	for lbl in data:
		# get data label, and continue using that label
		print >> sys.stderr, "have lbl:%s\n" % lbl
		daily = (lbl.find('daily') != -1)
		asdf = lbl.split('vs')
		x = asdf[0]
		y = asdf[1]
		url = (lbl.find('url') != -1)
		nourl = (lbl.find('nourl') != -1)
		label = 'logLikelihood vs iterations '
		
		x = []
		y = []
		z = []
		posZ = []
		smallestZ = float("inf")
		largestTime = float("-inf")
		smallestTime = float("inf")
		for t in data[lbl]:
			time = float(t[0])
			corr = float(t[2])
			if time > largestTime:
				largestTime = time
			if time < smallestTime:
				smallestTime = time
			x.append(time)
			y.append(float(t[1]))
			if corr < smallestZ:
				smallestZ = corr
			z.append(corr)
			posZ.append( (time,corr) )
		# graph lag, correlation
		fig, axes = plt.subplots()
		axes.plot(x,z)
		# now that we have data, make all data positive so can compute area under the curve
		totalZLessZero = 0
		totalZGreaterZero = 0
		for index, (time, value) in enumerate(posZ):
			posZ[index] = (time, (value - smallestZ))
			if time < 0:
				totalZLessZero += (value - smallestZ)
			elif time > 0:
				totalZGreaterZero += (value - smallestZ)

		avgLessZero = totalZLessZero / (0 - smallestTime)
		avgGreaterZero = totalZGreaterZero / (largestTime - 0)
		axes.set_title('Lag vs correlation:'+label+'\nLessZero Total:'+str(totalZLessZero)+' Avg:'+str(avgLessZero)+'\nGreaterZero Total:'+str(totalZGreaterZero)+' Avg:'+str(avgGreaterZero)+'\nRatioGreaterToLess:'+str((totalZGreaterZero/totalZLessZero)),fontsize=8)
		units = 'weeks'
		if daily:
			units = 'days'
		axes.set_xlabel('Lag units: '+units, fontsize = 8)
		axes.set_ylabel('Correlation', fontsize = 8)
		label = label.replace(' ', '_')
		fig.savefig('lag-vs-corr_'+label+'.png')
		plt.close()
		# graph lag, n, correlation
		#X = np.array(x)
		#Y = np.array(y)
		#Z = np.array(z)
		#X,Y = np.meshgrid(X,Y)
		#fig = plt.figure()
		#axes = fig.add_subplot(1,1,1, projection='3d')
		#surf = axes.plot_surface(X, Y, Z)
		#axes.set_title('Lag, sample size vs correlation:'+lbl)
		#axes.set_xlabel('Lag units')
		#axes.set_ylabel('Sample size')
		#axes.set_zlabel('Correlation')
		#fig.savefig('lag-vs-corr_3d_'+lbl+'.png')
		#plt.close()


if __name__ == "__main__":
	main()
