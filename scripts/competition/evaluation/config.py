all_teams = ["RI1","MRL","CSU","AIT"]
final_teams = ["MRL","CSU","AIT"]

team_names = {
    # "BAS" : "Baseline (no agents)",
    "CSU" : "CSU-Yunlu",
    "MRL" : "MRL",
    "RI1" : "Ri-one",
    "AIT" : "AIT-Rescue"
}

setup = {'name' : "setup day",
        'shortname' : "setup",
        'maps' : ["kobe-test"],
        'teams' : all_teams}

day1 = {'name' : "Preliminary Day 1",
        'shortname' : "Day1",
        'maps' : ["vc1", "joao1","berlin1", "eindhoven1", "mexico1"],
        'teams' : all_teams,
#        'merge_with' : day1,
#        'highlight' : 8
	}


day2 = {'name' : "Preliminary Day 2",
        'shortname' : "Day2",
        'maps' : ["ny1","kobe1", "sydney1", "sakae1", "paris1"],
        'teams' : all_teams,
#        'merge_with' : day1,
#        'highlight' : 8
	}


semi = {'name' : "Semifinal",
        'shortname' : "Semifinal",
        'maps' : ["vc2", "berlin2", "eindhoven2", "istanbul1", "sf1", "ny2", "paris2", "kobe2"],
        'teams' : all_teams,
	'show_ranks' : 3}


final = {'name' : "Finals",
        'shortname' : "final",
        'maps' : ["montreal1", "sf2", "sydney2", "berlin3", "kobe3", "sakae2", "eindhoven3", "paris3"],
        'teams' : final_teams,
        'show_ranks' : 2}


# final = {'name' : "Finals",
#         'shortname' : "final",
#         'maps' : ["Eindhoven1"],
#         'teams' : all_teams,
#         'merge_with' : day3,
#         'show_ranks' : 1}

rounds = [day1, day2, semi, final]

# semi_teams = ["RAK", "SBC", "POS", "IAM", "MRL", "RI1", "SEU", "RMA"]
# final_teams = ["POS", "IAM", "SEU", "RMA"]

# day1 = {'name' : "Preliminaries Day 1",
#         'shortname' : "Preliminary1",
#         'maps' : ["VC1", "Paris1", "Kobe1", "Berlin1", "Istanbul1"],
#         'teams' : all_teams}

# day2 = {'name' : "Preliminaries Day 2",
#         'shortname' : "Preliminary2",
#         'maps' : ["Kobe2", "Paris2", "Istanbul2", "Berlin2", "VC2"],
#         'teams' : all_teams
#         'merge_with' : day1
#         'highlight' : 8}

# semi = {'name' : "Semifinals",
#         'shortname' : "Semifinals",
#         'maps' : ["Kobe2", "Paris2", "Istanbul2", "Berlin2", "VC2"],
#         'teams' : semi_teams,
#         'highlight' : 4}

# final = {'name' : "Finals",
#         'shortname' : "Finals",
#         'maps' : ["Kobe2", "Paris2", "Istanbul2", "Berlin2", "VC2"],
#         'teams' : ["Paris5", "Berlin5", "Kobe4", "Istanbul5", "VC5"],
#         'show_ranks' : 3}

# rounds = [day1, day2, semi, final]

log_location = "../../logs/2022"
add_downloads = True
