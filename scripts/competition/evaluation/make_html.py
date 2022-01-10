#!/usr/bin/python

import sys, os, stat, glob
import config

all_teams = config.all_teams
team_names = config.team_names

template = """
<?xml version="1.0" encoding="iso-8859-1"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
               "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
lang="en" xml:lang="en">

<head>
<title>Results for map %(mapname)s</title>
<style type="text/css">
  body { font-family: sans-serif; }

  table { border-collapse: collapse; }
  .running { color:red; }
  tr.first { background-color: #E9D44A; }
  tr.second { background-color: #C8C8C8; }
  tr.third { background-color: #C89D4C; }
  div.init-map {  float:left;
                  text-align:center; }
</style>
</head>

<body>
<h1>Results for %(mapname)s</h1>
%(map_download)s
<br clear="all" />
<div class="init-map"><a href="snapshot-init.png"><img src="snapshot-init-small.png" width="400" height="300" alt="Initial situation for %(mapname)s" /></a>
<br />
Initial score: %(init_score).2f</div>
<a href="plot-%(mapname)s.svg"><img src="plot-%(mapname)s.svg" width="400" height="300" alt="Plot of agent scores vs time for %(mapname)s" /></a>

<br clear="all" />
<br />

%(log_download)s
%(table)s

</body>

</html>
"""

class TeamEntry(object):
    def __init__(self, team, mapdata):
        self.id = team
        self.name = team_names[team]
        self.map = mapdata
        self.dir = os.path.join(self.map.path, team)

        self.running = True
        self.init_score = None
        self.final_score = 0.0
        self.last_score = 0.0
        self.scores = None
        self.max_time = 0
        self.rank = -1
        self.jlog= None
        self.full_log=None

        if self.valid():
            for line in open(os.path.join(self.dir, "init-score.txt")):
                self.init_score = float(line.strip())

            for line in open(os.path.join(self.dir, "scores.txt")):
                self.scores = [float(s) for s in line.split()]
            self.last_score = self.scores[-1]

            try:
                for line in open(os.path.join(self.dir, "final-score.txt")):
                    self.final_score = float(line.strip())
                self.running = False
            except:
                pass

            self.max_time = len(self.scores) - 1
        try:
            self.full_log=self.get_logfile(self.map.mapname)[1]
            self.jlog=self.get_jlogfile(self.map.mapname)[1]
        except:
            pass

    def valid(self):
        if not os.path.exists(self.dir):
            return False
        return True

    def get_logfile(self, mapdir):
#        files = glob.glob(os.path.join(mapdir, "*-%s*.gz" % self.name))
        files = glob.glob(os.path.join(mapdir, "*-%s*.7z" % self.name))
        if len(files) != 1:
            #Can't identify team logfile
            raise KeyError
        size = os.stat(files[0])[stat.ST_SIZE]
        return (size, files[0])

    def get_jlogfile(self, mapdir):
        files = glob.glob(os.path.join(mapdir, "*-%s*.jlog.zip" % self.name))
        if len(files) != 1:
            #Can't identify team logfile
            raise KeyError
        size = os.stat(files[0])[stat.ST_SIZE]
        return (size, files[0])

    def get_screenshots(self):
        times = list(self.map.get_screenshot_timepoints())
        for t in times[:-1]:
            path = os.path.join(self.dir, "snapshot-%d.png" % t)
            tn_path = os.path.join(self.dir, "snapshot-%d-tn.jpg" % t)
            if os.path.exists(path) and t <= self.max_time:
                yield(t, path, tn_path, self.scores[t])
            else:
                yield(t, None, None, None)

        if self.max_time != self.map.turns:
            #final snapshot would not correspond to others - don't use it
            yield(self.max_time, None, None, None)
        else:
            path = os.path.join(self.dir, "snapshot-final.png")
            tn_path = os.path.join(self.dir, "snapshot-final-tn.jpg")
            yield(self.max_time, path, tn_path, self.scores[self.max_time])


class MapData(object):
    def __init__(self, mapname, teams=None):
        self.mapname = mapname
        self.teamdict = None
        self.entries = []
        self.init_score = -1
        self.turns = -1
        self.path = None
        self.started = False

        if os.path.exists("%s-eval" % mapname):
            self.path = "%s-eval" % mapname
        elif os.path.exists(os.path.join(mapname, "plot-%s.svg" % mapname)):
            self.path = mapname
        elif os.path.exists("plot-%s.svg" % mapname):
            self.path = "."
        else:
            print >> sys.stderr, "Couldn't find eval directory for map %s." % mapname
            return

        used_teams = teams if teams else all_teams
        for t in used_teams:
          try:
            entry = TeamEntry(t, self)
            if entry.valid():
                self.entries.append(entry)
                self.init_score = entry.init_score
                self.turns = max(self.turns, entry.max_time)
            elif teams is not None:
                self.entries.append(entry)
          except:
             print >> sys.stderr, "Couldn't find eval directory for map %s team %s." % (mapname,t)
             return
        for t in self.entries:
            if t.max_time != self.turns:
                t.final_score = 0.0

        sorted_by_score = sorted(self.entries, key=lambda t: -t.final_score)
        if sorted_by_score[0].last_score != 0.0:
            self.started = True

        i = 1
        prev_score = -1
        prev_teams = []
        for t in sorted_by_score:
            # print >> sys.stderr, t.final_score, prev_score, i
            if t.final_score != prev_score:
                prev_teams = []

            for prev in prev_teams:
                prev.rank = i
            t.rank = i

            prev_teams.append(t)
            prev_score = t.final_score
            i += 1


        self.step_ranking(self.entries)
        # for i, t in enumerate(sorted_by_score):
        #     t.rank = i+1

    def step_ranking(self, entries):
        sorted_by_score = sorted(entries, key=lambda t: -t.final_score)
        best = sorted_by_score[0].final_score
        n = len(entries)
        delta = best/(2.0*n)

        def get_upper_bound(t):
            score = best
            upper = 2*n
            while score > t.final_score:
                score -= delta
                upper -= 1
            return upper + 1

        prev_rank = 2*n
        prev_score = sorted_by_score[0].final_score
        sorted_by_score[0].rank = 2*n
        for t in sorted_by_score[1:]:
            upper = get_upper_bound(t)
            if prev_score == t.final_score:
                t.rank = prev_rank
            elif upper >= prev_rank:
                t.rank = prev_rank - 1
            else:
                t.rank = upper
            if t.rank < 1:
                t.rank = 1
            prev_rank = t.rank
            prev_score = t.final_score

    def get_team(self, id):
        if self.teamdict is None:
            self.teamdict = dict((t.id, t) for t in self.entries)
        return self.teamdict[id]

    def get_screenshot_timepoints(self):
        t = 50
        while t < self.turns:
            yield t
            t += 50
        yield self.turns

    def get_mapfile(self):
        names = ["%s.7z" % self.mapname, "%s-map.tar.gz" % self.mapname, "%s-map.tgz" % self.mapname, "%s.tgz" % self.mapname, "%s.tar.gz" % self.mapname]
        for fname in names:
            if os.path.exists(fname):
                size = os.stat(fname)[stat.ST_SIZE]
                return size, fname
        return 0, None

    def get_logpackage(self):
        path = "%s-logs.7z" % self.mapname
        descent = 3
        while not os.path.exists(path) and descent > 0:
            path = os.path.join("..", path)
            descent -= 1

        if not os.path.exists(path):
            return 0, None
        size = os.stat(path)[stat.ST_SIZE]
        return size, path

def sizeof_fmt(num):
    for x in ['bytes','KB','MB','GB','TB']:
        if num < 1024.0:
            return "%3.1f%s" % (num, x)
        num /= 1024.0


def list_to_row(l, elem='td'):
    elems, classes = l
    if classes:
        cl_string = 'class="%s"' % ", ".join(classes)
    else:
        cl_string = ""
    delim = "</%s><%s>" % (elem, elem)
    return "<tr %s><%s>" % (cl_string, elem) + delim.join(elems) + "</%s></tr>" % elem

if __name__ == '__main__':
    mapname = sys.argv[1]

    data = MapData(mapname)
    init_score = data.init_score

    pack_size, pack_path = data.get_logpackage()
    log_download = ""
    if pack_path:
        archive_url = "http://sourceforge.net/projects/roborescue/files/logs/2011/%s/%s-all.tar" % (mapname, mapname)
        log_download = '<a href="%s">Download all logs</a> (Size: %s)' % (archive_url, sizeof_fmt(pack_size))

    map_size, map_path = data.get_mapfile()
    map_download = ""
    if map_path:
        map_download = '<a href="%s">Download map</a> (Size: %s)' % (map_path, sizeof_fmt(map_size))

    sorted_by_rank = sorted(data.entries, key=lambda t: -t.rank)

    def make_table_row(team, count):
        classes = []
        if team == sorted_by_rank[0]:
            classes.append("first")
        elif team == sorted_by_rank[1]:
            classes.append("second")
        elif team == sorted_by_rank[2]:
            classes.append("third")

        if team.final_score == team.last_score:
            result = [team.name, "%.6f" % team.final_score, "%d" % team.rank]
        else:
            result = [team.name, "<span class=\"running\">%.6f</span>" % team.last_score, "%d" % team.rank]

        for t, path, tn_path, score in team.get_screenshots():
            if path:
                html = '<a href="%s"><img src="%s" width="100" height="75" alt="Map at turn %d" /></a><br />%.4f' % (path, tn_path, t, score)
            else:
                html = ''
            result.append(html)
        if config.add_downloads and team.valid():
            try:
                size, log = team.get_logfile(data.path)
                jsize, jlog = team.get_jlogfile(data.path)
                # log_url = log
#                log_url = "http://sourceforge.net/projects/roborescue/files/%s/%s/%s/download" % (config.log_location, data.mapname, log)
                log_url = "%s" % (log)
                jlog_url= "../viewer/?jlog=../%s/%s&full_log=../%s/%s"%(mapname,jlog,mapname,log_url)
                # log_url = "http://sourceforge.net/projects/roborescue/files/logs/2011/%s" % log
                result += ['<a href="%s">View Simulation</a> (%s)<br/><a href="%s">Download Simulation</a> (%s)' % (jlog_url,sizeof_fmt(jsize), log_url, sizeof_fmt(size))]
            except KeyError:
                pass
        else:
            result.append("")

            #result += [""] * (count - len(result))
        return result, classes

    headers = ["Team", "Score", "Points"]
    headers += ["%d" % t for t in data.get_screenshot_timepoints()]
    headers.append("Logfile")

    table = '<table border="2" cellspacing="0" cellpadding="5">'
    table += list_to_row((headers, None), "th")
    for t in data.entries:
        table += list_to_row(make_table_row(t, len(headers))) + "\n"
    table += "</table"


    print template % locals()
