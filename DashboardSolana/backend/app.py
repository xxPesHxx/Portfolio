from flask import Flask, jsonify
from system_info import get_system_info, get_validator_status
from flask_cors import CORS

app = Flask(__name__)
CORS(app)  # zezwala Reactowi na zapytania

@app.route("/api/status")
def status():
    return jsonify({
        "system": get_system_info(),
        "validator": get_validator_status()
    })

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001, debug=True)

