#!/usr/bin/python
import sys

result = []

for line in open(sys.argv[1]):
    elems = line.split(" ")
    if elems[0].startswith('"'):
        i = 0
        while not elems[i].endswith('"'):
            i+= 1
        title = " ".join(elems[0:i+1])
        data = elems[i+1:]
    else:
        title = elems[0]
        data = elems[1:]
        
    column = [title]
    for e in data:
        column.append(e)
    result.append(column)

if not result:
    exit

def get_row(colum, row):
    if row < len(colum):
        return colum[row]
    return "?"#colum[-1]
#print [len(r) for r in result]
    
count = len(result[0])
for row in xrange(0, count):
    print " ".join(get_row(col, row).strip() for col in result)
