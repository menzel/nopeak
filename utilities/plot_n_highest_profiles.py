#!/usr/bin/env python3

import sys
import numpy
import matplotlib.pyplot as plt
import matplotlib.cm as cm
import re


### Check params
if len(sys.argv) < 2 or "help" in sys.argv[1] :
    print("usage: ./plot_profile.py profile.csv profile_count smooth")
    print("example: ./plot_profile.py gapba.csv 50 5")


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

fig, ax = plt.subplots()

count = int(sys.argv[2])
smooth = int(sys.argv[3])
linecol = [cm.hot(float(x)/1000) for x in range(0, 1000)]

profiles = {}
heights = {}
num_lines = str(sum(1 for line in open(sys.argv[1])))

with open(sys.argv[1]) as fp:
    for i,line in enumerate(fp):

        if i % 1000 == 0:
            print(str(i) + "/" + num_lines)

        if not line.startswith("#") and not line.startswith("qmer"):
            parts = line.split("\t")

            qmer = parts[0]
            values = [int(x) for x in parts[1:]]
            sma = get_sma(values,smooth)[smooth:]

            mx = numpy.max(sma)
            mn = numpy.mean(sma)
            mi = numpy.min(sma)

            profiles[qmer] = sma
            heights[qmer] = (mx - mn)/(mn - mi)


highest = list({k:v for k,v in sorted(heights.items(), key=lambda item: item[1])}.keys())[-count:]

for qmer in highest:
    sma = profiles[qmer]

    yvals = [-500 + x + smooth for x in list(range(len(sma)))]
    mx = numpy.max(sma)
    mn = numpy.min(sma)

    ax.plot(yvals, sma, linewidth=2, color=linecol[int(mx*1000) % 800]) 
    ax.text(-440,mx,qmer, color=linecol[int(mx*1000) % 800],fontsize=20)  


ax.axvline(0, linewidth=2,color='grey',linestyle="--")

ax.set_xlabel("Distance to BS [bp]",fontsize=20)
ax.set_ylabel("Read count",fontsize=20)

ax.spines['top'].set_visible(False)
ax.spines['right'].set_visible(False)

ax.set_xticks(numpy.arange(-500, 501, step=100))
ax.xaxis.set_tick_params(labelsize=20)
ax.yaxis.set_tick_params(labelsize=20)


plt.show()
#fig.savefig("fig.pdf", format='pdf', bbox_inces='tight')
