import psutil
import platform
import time
import subprocess

def get_system_info():
    per_core = psutil.cpu_percent(interval=1, percpu=True)
    return {
        "cpu_percent": psutil.cpu_percent(interval=None),
        "cpu_per_core": per_core,
        "memory": psutil.virtual_memory()._asdict(),
        "disk": psutil.disk_usage("/")._asdict(),
        "uptime": time.time() - psutil.boot_time(),
        "load_avg": psutil.getloadavg() if platform.system() != "Windows" else None,
        "platform": platform.platform(),
    }

def get_validator_status(port=8899):
    try:
        output = subprocess.check_output(f"ss -tuln | grep :{port}", shell=True)
        return "online" if output else "offline"
    except subprocess.CalledProcessError:
        return "offline"
