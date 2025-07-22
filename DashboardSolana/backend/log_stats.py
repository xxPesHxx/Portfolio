import json
from system_info import get_system_info
from datetime import datetime

log_file = "data/stats.json"

def log():
    with open(log_file, "a") as f:
        json.dump({
            "timestamp": datetime.now().isoformat(),
            "data": get_system_info()
        }, f)
        f.write("\n")

log()
