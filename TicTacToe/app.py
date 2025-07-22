import numpy as np
from flask import Flask, request, jsonify
from flask_cors import CORS
from main import MLP, TicTacToe 

app = Flask(__name__)
CORS(app)

model = MLP()
model.load()
game = TicTacToe()


def encode_cell(cell) -> float:
    if isinstance(cell, (int, float)):
        return float(cell)
    s = str(cell).upper()
    if s == "X":
        return 1.0
    elif s == "O":
        return -1.0
    else:
        return 0.0

@app.route("/api/move", methods=["POST"])
def move():
    try:
        data = request.json or {}
        raw = data.get("board", [])

        if isinstance(raw, list) and len(raw) == 9 and not any(isinstance(el, list) for el in raw):
            flat_input = raw
        else:
            flat_input = [cell for row in raw for cell in (row or [])]

        flat = [encode_cell(c) for c in flat_input]

        if len(flat) != 9:
            return jsonify({"error": "board musi mieć dokładnie 9 elementów"}), 400

        X = np.array(flat, dtype=float).reshape(9, 1)
        move_index = model.predict(X)[0]
        return jsonify({"ai_move": int(move_index)})

    except Exception as e:
        app.logger.exception("Błąd w /api/move")
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)

