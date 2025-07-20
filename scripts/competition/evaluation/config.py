all_teams = ["TIM","AIT","3RA"]
final_teams = ["TIM","AIT","3RA"]

team_names = {
    # "BAS" : "Baseline (no agents)",
#    "CSU" : "CSU-Yunlu",
    "3RA" : "3Rakshak",
    "TIM" : "Timrad",
    "AIT" : "AIT-Rescue"
}

setup = {'name' : "setup day",
        'shortname' : "setup",
        'maps' : ["kobe-test"],
        'teams' : all_teams}

day1 = {'name' : "Preliminary Day 1",
        'shortname' : "Day1",
        'maps' : ["kobe1", "joao1", "ny1", "vc1", "eindhoven1"],
        'teams' : all_teams,
#        'merge_with' : day1,
#        'highlight' : 8
	}


day2 = {'name' : "Preliminary Day 2",
        'shortname' : "Day2",
        # 'maps' : ["ny1","kobe1", "sydney1", "sakae1", "paris1"],
        'maps' : ["sydney1", "eindhoven2", "montreal1", "istanbul1", "eindhoven3"],
        'teams' : all_teams,
        'merge_with' : day1,
#        'highlight' : 8
	}


final1 = {'name' : "Final 1",
        'shortname' : "Final1",
        'maps' : ["sydney1", "eindhoven2", "montreal1", "istanbul1", "eindhoven3"],
        'teams' : final_teams,
        'show_ranks' : 1}


final2 = {'name' : "Final 2",
        'shortname' : "Final2",
        'maps' : ["paris1","sf1","kobe2","eindhoven4","vc2","bordeaux1","presentation"],
        'teams' : all_teams,
        'merge_with' : final1,
        'show_ranks' : 1}

rounds = [day1,final1, final2]
# rounds = [final1]

log_location = "../../logs/2025"
add_downloads = True
