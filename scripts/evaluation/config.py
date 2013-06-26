all_teams = ["BAS", "ANC", "APO", "CSU", "GUC", "LTI", "MIN", "MRL", "NAI", "POS", "RI1", "RAK", "SOS", "ZJU"]

team_names = {
    "BAS" : "Baseline (no agents)",
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
        'maps' : ["Berlin1", "Kobe1", "Paris1"],
        'teams' : all_teams}

day2 = {'name' : "Day 2",
        'shortname' : "Day2",
        'maps' : ["Istanbul1", "VC1", "Kobe2"],
        'teams' : all_teams,
        'merge_with' : day1}

day3 = {'name' : "Day 3",
        'shortname' : "Day3",
        'maps' : ["Paris2", "Berlin2", "Istanbul2"],
        'teams' : all_teams,
        'merge_with' : day2}

final = {'name' : "Finals",
        'shortname' : "final",
        'maps' : ["Eindhoven1"],
        'teams' : all_teams,
        'merge_with' : day3,
        'show_ranks' : 1}

rounds = [day1, day2, day3, final]

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

