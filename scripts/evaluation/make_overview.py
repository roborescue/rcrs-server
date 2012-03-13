#!/usr/bin/python

import sys, os, stat, glob
import make_html

template = """
<?xml version="1.0" encoding="iso-8859-1"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
               "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
lang="en" xml:lang="en">

<head>
<title>Robocup 2011 Rescue Simulation League Results</title>
<style type="text/css">
  body { font-family: sans-serif; }

  table { border-collapse: collapse; margin-bottom:5em }
  tr.first { background-color: #E9D44A; }
  tr.second { background-color: #C8C8C8; }
  tr.third { background-color: #C89D4C; }
  tr.qualified { background-color: #AABBFF; }
  div.init-map {  float:left;
                  text-align:center; }
</style>
</head>

<body>
<h1>RoboCup 2011 Rescue Simulation League Results</h1>

<p>Click on the map names to get detailed results and logfiles</p>
<p>Download all maps <a href="%(mapfile)s">here</a> (%(mapsize)s)</p>

%(body)s

</body>

</html>
"""

class DayData(object):
    def __init__(self, name, shortname, maps, teams, highlight=None):
        self.name = name
        self.shortname = shortname
        self.teams = teams
        self.maps = maps
        self.runs = []
        self.prev_day = None
        self.highlight = highlight

        self.runs = [make_html.MapData(m, teams) for m in self.maps]
        self.calculate_result()

    def calculate_result(self):
        self.total_ranks = dict((t, 0) for t in self.teams)
        self.total_scores = dict((t, 0.0) for t in self.teams)
        self.final_rank = {}
        for run in self.runs:
            for team in run.entries:
                self.total_ranks[team.id] += team.rank
                self.total_scores[team.id] += team.final_score
        if self.prev_day:
            for t in self.prev_day.teams:
                self.total_ranks[t] += self.prev_day.total_ranks[t]
                self.total_scores[t] += self.prev_day.total_scores[t]
                
        sorted_teams = sorted(self.teams, key=lambda t: -self.total_scores[t])
        sorted_teams = sorted(sorted_teams, key=lambda t: self.total_ranks[t])
        for i, t in enumerate(sorted_teams):
            self.final_rank[t] = i+1

    def add_day(self, day):
        self.prev_day = day
        self.calculate_result()

    def get_team_data(self, team):
        single_results = []
        for run in self.runs:
            t = run.get_team(team)
            single_results.append((t.final_score, t.rank))
        return single_results, (self.total_scores[team], self.total_ranks[team], self.final_rank[team])

    def __str__(self):
        s = "%s:\n" % self.name
        for t in self.teams:
            ts = "%s: " % t
            res, (tot_score, tot_rank, rank) = self.get_team_data(t)
            ts += "| ".join("%7.3f   %2d" % r for r in res)
            ts += " | %7.3f  %3d | %2d " % (tot_score, tot_rank, rank)
            s += ts + "\n"
        return s
    

def get_mappack():
    path = "maps2011.tar.gz"
    if not os.path.exists(path):
        return 0, None
    size = os.stat(path)[stat.ST_SIZE]
    return size, path
       
if __name__ == '__main__':
    all_teams = make_html.all_teams
    semi_teams = ["RAK", "SBC", "POS", "IAM", "MRL", "RI1", "SEU", "RMA"]
    final_teams = ["POS", "IAM", "SEU", "RMA"]
    
    day1 = DayData("Preliminaries Day 1", "Preliminary1", ["VC1", "Paris1", "Kobe1", "Berlin1", "Istanbul1"], all_teams)
    day2 = DayData("Preliminaries Day 2", "Preliminary2", ["Kobe2", "Paris2", "Istanbul2", "Berlin2", "VC2"], all_teams, 8)
    day2.add_day(day1)
    semi = DayData("Semifinals", "Semifinals", ["Paris3", "Istanbul3", "Berlin3", "Kobe3", "Istanbul4", "Berlin4", "VC4", "Paris4"], semi_teams, 4)
    final = DayData("Finals", "Finals", ["Paris5", "Berlin5", "Kobe4", "Istanbul5", "VC5"], final_teams, 0)

    mapsize, mapfile = get_mappack()
    mapsize = make_html.sizeof_fmt(mapsize)

    def make_table_row(day, team):
        results = []
        res, (tot_score, tot_rank, final_rank) = day.get_team_data(team)
        results.append(make_html.team_names[team])
        for score, rank in res:
            results.append("%.2f" % score)
            results.append("%d" % rank)
        if day.prev_day:
            _, (prev_score, prev_rank, _) = day.prev_day.get_team_data(team)
            results.append("%.2f" % prev_score)
            results.append("%d" % prev_rank)

        results.append("%.2f" % tot_score)
        results.append("%d" % tot_rank)
        results.append("%d" % final_rank)

        classes = []
        if day.highlight == 0:
            if final_rank == 1:
                classes.append("first")
            elif final_rank == 2:
                classes.append("second")
            elif final_rank == 3:
                classes.append("third")
        elif day.highlight is not None:
            if final_rank <= day.highlight:
                classes.append("qualified")
            

        return results, classes

    def make_header(day):
        result = ['<th rowspan="2">Team</th>']
        result2 = []
        for m in day.maps:
            result.append('<th colspan="2"><a href="%s-eval/index.html">%s</a></th>' % (m,m))
            result2.append("<th>Score</th>")
            result2.append("<th>Rank</th>")
        if day.prev_day:
            result.append('<th colspan="2">%s</th>' % day.prev_day.shortname)
            result2.append("<th>Score</th>")
            result2.append("<th>Rank</th>")
            
        result.append('<th colspan="2">Total</th>')
        result2.append("<th>Score</th>")
        result2.append("<th>Rank</th>")
        result.append('<th rowspan="2">Rank</th>')
        return result, result2

    body = ""
    for run in [day1, day2, semi, final]:
        body += "<h2>%s</h2>\n" % run.name
        table = '<table border="2" cellspacing="0" cellpadding="5">'
        h1, h2 = make_header(run)
        table += "<tr>" + "".join(h1) + "</tr>\n"
        table += "<tr>" + "".join(h2) + "</tr>\n"
        for t in run.teams:
            table += make_html.list_to_row(make_table_row(run, t)) + "\n"
        table += "</table>"
        body += table

    print template % locals()

        
