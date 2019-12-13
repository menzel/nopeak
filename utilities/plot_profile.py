#!/usr/bin/env python3

import sys
import numpy
import matplotlib.pyplot as plt
import matplotlib.cm as cm
import re


### Check params
if len(sys.argv) < 2 or "help" in sys.argv[1] :
    print("usage: ./plot_profile.py profile.csv regex smooth")
    print("example: ./plot_profile.py gapba.csv CC..AA 5")


### Get moving average from number series
# values are the numbers, n is the window size
def get_sma(values, n):
    values = [0 for _ in range(n // 2)] + list(values)

    curr = values[:n]
    sma = [] # [0 for _ in range(n//2)]

    for val in values[n:]:
        sma.append(numpy.mean(curr))
        curr = curr[1:] + [val]

    sma.append(numpy.mean(curr))
    return sma


### Main program 
match = re.compile(sys.argv[2]) 
linecol = [cm.hot(float(x)/1000) for x in range(0, 1000)]
smooth = int(sys.argv[3])
count = 0

#fig, ax = plt.subplots(dpi=1200,figsize=(14.69,9.27))
fig, ax = plt.subplots()

with open(sys.argv[1]) as fp:
    for line in fp:
        if not line.startswith("#") and not line.startswith("qmer") and re.search(match, line[:12]):
            parts = line.split("\t")

            values = [int(x) for x in parts[1:]]
            qmer = parts[0]

            sma = get_sma(values,smooth)[smooth:]
            smamin = numpy.min(sma)
            #sma = [s - smamin for s in sma]
            yvals = [-500 + x + smooth for x in list(range(len(sma)))]
            mx = numpy.max(sma)
            mn = numpy.min(sma)
            argmin = numpy.argmin(values)
            argmax = numpy.argmax(values)
            n = len(values)

            #plt.plot(yvals, sma, linewidth=3, color=linecol[int(mx*1000) % 800]) 
            ax.plot(yvals, sma, linewidth=2, color=linecol[int(mx*1000) % 800]) 
            ax.text(-440,mx,qmer, color=linecol[int(mx*1000) % 800],fontsize=20)  
            #plt.plot([-280, -70], [mx, mx], color=linecol[int(mx*1000) % 800], linestyle='--',alpha=.4) # dotted line between kmer and top

    plt.show() 
    ax.axvline(0, linewidth=2,color='grey',linestyle="--")

    #plt.axhline(numpy.mean(sma), linewidth=1,color='red')
    #plt.axhline(mn, linewidth=1,color='blue')
    ax.set_xlabel("Distance to BS [bp]",fontsize=20)
    ax.set_ylabel("Read count",fontsize=20)

    ax.spines['top'].set_visible(False)
    ax.spines['right'].set_visible(False)
    
    ax.set_xticks(numpy.arange(-500, 501, step=100))
    ax.xaxis.set_tick_params(labelsize=20)
    ax.yaxis.set_tick_params(labelsize=20)


    plt.show()
    #fig.savefig("fig.pdf", format='pdf', bbox_inces='tight')
