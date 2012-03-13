set title system("echo Scores for $RCR_MAP")
set datafile missing "-"
set key outside vertical
set ylabel "score"
set xlabel "time"

count = system("echo $RCR_COUNT")

set style line 1 lt 1 lc rgb "red"
set style line 2 lt 1 lc rgb "green"
set style line 3 lt 1 lc rgb "blue"
set style line 4 lt 1 lc rgb "cyan"
set style line 5 lt 1 lc rgb "yellow"
set style line 6 lt 1 lc rgb "black"
set style line 7 lt 1 lc rgb "magenta"
set style line 8 lt 1 lc rgb "orange"
set style line 9 lt 2 lc rgb "red"
set style line 10 lt 2 lc rgb "green"
set style line 11 lt 2 lc rgb "blue"
set style line 12 lt 2 lc rgb "black"
set style line 13 lt 4 lc rgb "red"
set style line 14 lt 4 lc rgb "green"
set style line 15 lt 4 lc rgb "blue"
set style line 16 lt 4 lc rgb "black"
set style increment user 

set terminal svg dashed lw 2 size 1024,768
set output system("echo plot-$RCR_MAP.svg")

#test

set style data lines
plot 'scores.dat' using 1 title columnheader(1), for [i=2:count] '' using i  title columnheader(i)
#pause -1