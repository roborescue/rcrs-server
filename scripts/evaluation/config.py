all_teams = ["ANC", "APO", "CSU", "GUC", "LTI", "MIN", "MRL", "NAI", "POS", "RI1", "RAK", "SOS", "ZJU"]
semi_teams = ["APO", "CSU", "GUC", "MIN", "MRL", "POS", "SOS", "ZJU"]

team_names = {
    # "BAS" : "Baseline (no agents)",
    "ANC" : "anct_rescue2013",
    "APO" : "Apollo-Rescue",
    "CSU" : "CSU-YUNLU",
    "GUC" : "GUC_ArtSapience",
    "LTI" : "LTI-Agent-Rescue",
    "MIN" : "MinERS",
    "MRL" : "MRL",
    "NAI" : "NAITO-Rescue2013",
    "POS" : "Poseidon",
    "RI1" : "Ri-one",
    "RAK" : "RoboAKUT",
    "SOS" : "S.O.S.",
    "ZJU" : "ZJUBase"
}

day1 = {'name' : "Day 1",
        'shortname' : "Day1",
        'maps' : ["Berlin1", "Eindhoven1", "Kobe1", "Paris1", "VC1"],
        'teams' : all_teams}

day2 = {'name' : "Day 2",
        'shortname' : "Day2",
        'maps' : ["Mexico1", "Kobe2", "Eindhoven2", "Istanbul1", "Paris2"],
        'teams' : all_teams,
        'merge_with' : day1,
        'highlight' : 8}


semi = {'name' : "Semifinals",
        'shortname' : "Semifinals",
        'maps' : ["VC2", "Berlin2", "Kobe3", "Istanbul2", "Mexico2", "Eindhoven3", "Paris3", "Eindhoven4"],
        'teams' : semi_teams,
        'highlight' : 4}


# final = {'name' : "Finals",
#         'shortname' : "final",
#         'maps' : ["Eindhoven1"],
#         'teams' : all_teams,
#         'merge_with' : day3,
#         'show_ranks' : 1}

rounds = [day1, day2, semi]

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

log_location = "logs/2013"
add_downloads = True
