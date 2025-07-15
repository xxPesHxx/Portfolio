import numpy as np
import pickle
import sys
import os
from functools import lru_cache

# ----------------------------------------
# Part 1: TicTacToe game logic + Minimax with memoization
# ----------------------------------------

WINNING_LINES = [
    (0,1,2), (3,4,5), (6,7,8),  # rows
    (0,3,6), (1,4,7), (2,5,8),  # cols
    (0,4,8), (2,4,6)            # diags
]

class TicTacToe:
    def __init__(self, board=None):
        # board: tuple length 9, values 1=X, -1=O, 0=empty
        self.board = tuple([0]*9) if board is None else board

    def available_moves(self, board):
        return [i for i,v in enumerate(board) if v == 0]

    def check_winner(self, board):
        for line in WINNING_LINES:
            s = board[line[0]] + board[line[1]] + board[line[2]]
            if s == 3:
                return 1
            if s == -3:
                return -1
        if 0 not in board:
            return 0  # draw
        return None  # game continues

    @lru_cache(maxsize=None)
    def minimax(self, board, player):
        winner = self.check_winner(board)
        if winner is not None:
            return winner * player, None

        best_score = -np.inf
        best_move = None
        for move in self.available_moves(board):
            new_board = list(board)
            new_board[move] = player
            score, _ = self.minimax(tuple(new_board), -player)
            score = -score
            if score > best_score:
                best_score = score
                best_move = move
        return best_score, best_move

# ----------------------------------------------------
# Part 2: Generate training data using Minimax labeling with caching
# ----------------------------------------------------

def generate_dataset():
    states = []
    moves = []
    visited = set()
    game = TicTacToe()

    def traverse(board, player):
        key = (board, player)
        if key in visited:
            return
        visited.add(key)
        winner = game.check_winner(board)
        if winner is not None:
            return
        # get best move via minimax
        _, best = game.minimax(board, player)
        if best is not None:
            states.append(board)
            moves.append(best)
        # explore deeper
        for mv in game.available_moves(board):
            new_board = list(board)
            new_board[mv] = player
            traverse(tuple(new_board), -player)

    traverse(tuple([0]*9), player=1)
    # prepare arrays
    X = np.array(states, dtype=float).T      # shape (9, N)
    Y = np.zeros((9, X.shape[1]), dtype=float)
    for i, mv in enumerate(moves):
        Y[mv, i] = 1
    return X, Y

# -----------------------------------------------
# Part 3: Define simple MLP
# -----------------------------------------------

class MLP:
    def __init__(self, input_size=9, hidden_size=64, output_size=9, lr=1.0):
        self.lr = lr
        self.W1 = np.random.randn(hidden_size, input_size) * 0.01
        self.b1 = np.zeros((hidden_size,1))
        self.W2 = np.random.randn(output_size, hidden_size) * 0.01
        self.b2 = np.zeros((output_size,1))

    def relu(self, z): return np.maximum(0, z)
    def relu_deriv(self, z): return (z > 0).astype(float)
    def softmax(self, z):
        e = np.exp(z - np.max(z, axis=0, keepdims=True))
        return e / np.sum(e, axis=0, keepdims=True)

    def forward(self, X):
        self.Z1 = self.W1 @ X + self.b1
        self.A1 = self.relu(self.Z1)
        self.Z2 = self.W2 @ self.A1 + self.b2
        self.A2 = self.softmax(self.Z2)
        return self.A2

    def compute_loss(self, Y_hat, Y):
        m = Y.shape[1]
        return -np.sum(Y * np.log(Y_hat + 1e-8)) / m

    def backward(self, X, Y):
        m = X.shape[1]
        dZ2 = self.A2 - Y
        dW2 = (dZ2 @ self.A1.T) / m
        db2 = np.sum(dZ2, axis=1, keepdims=True) / m
        dA1 = self.W2.T @ dZ2
        dZ1 = dA1 * self.relu_deriv(self.Z1)
        dW1 = (dZ1 @ X.T) / m
        db1 = np.sum(dZ1, axis=1, keepdims=True) / m

        self.W2 -= self.lr * dW2
        self.b2 -= self.lr * db2
        self.W1 -= self.lr * dW1
        self.b1 -= self.lr * db1

    def train(self, X, Y, epochs=1000):
        for i in range(1, epochs+1):
            Y_hat = self.forward(X)
            loss = self.compute_loss(Y_hat, Y)
            self.backward(X, Y)
            if i % (epochs // 10) == 0:
                print(f"Epoch {i}/{epochs}, loss={loss:.4f}")
        print("Training complete.")

    def predict(self, X):
        return np.argmax(self.forward(X), axis=0)

    def save(self, path='ttt_model.pkl'):
        with open(path,'wb') as f:
            pickle.dump({'W1':self.W1,'b1':self.b1,'W2':self.W2,'b2':self.b2}, f)

    def load(self, path='ttt_model.pkl'):
        with open(path,'rb') as f:
            p = pickle.load(f)
        self.W1,self.b1,self.W2,self.b2 = p['W1'],p['b1'],p['W2'],p['b2']

# ----------------------------------------
# Part 4: CLI play vs AI
# ----------------------------------------

def print_board(board):
    symbols = {1: 'X', -1: 'O', 0: ' '}
    for i in range(3):
        row = [symbols[board[3*i + j]] for j in range(3)]
        print(' | '.join(row))
        if i < 2:
            print('---------')


def play_vs_ai(model):
    game = TicTacToe()
    board = tuple([0]*9)
    current = 1  # human = X = 1, AI = O = -1
    while True:
        print_board(board)
        if current == 1:
            move = int(input("Twój ruch (0-8): "))
            if board[move] != 0:
                print("Pole zajęte, spróbuj inne.")
                continue
            board = list(board)
            board[move] = current
            board = tuple(board)
        else:
            # use network prediction
            X = np.array(board, dtype=float).reshape(9,1)
            pred = model.predict(X)[0]
            print(f"AI gra na pozycji {pred}")
            board = list(board)
            board[pred] = current
            board = tuple(board)

        winner = game.check_winner(board)
        if winner is not None:
            print_board(board)
            if winner == 1:
                print("Wygrywasz!")
            elif winner == -1:
                print("AI wygrało.")
            else:
                print("Remis.")
            break

        current *= -1

# -----------------------------
# Main
# -----------------------------

if __name__ == "__main__":
    if len(sys.argv) > 1 and sys.argv[1] == 'play':
        model = MLP()
        model.load()
        play_vs_ai(model)
    else:
        # train model
        print("Generuję dane...")
        X, Y = generate_dataset()
        print(f"Zebrano {X.shape[1]} próbek.")
        idx = np.random.permutation(X.shape[1])
        split = int(0.8 * idx.size)
        tr, te = idx[:split], idx[split:]
        X_train, Y_train = X[:, tr], Y[:, tr]
        X_test, Y_test = X[:, te], Y[:, te]

        model = MLP()
        if os.path.exists("ttt_model.pkl"):
            print("Wczytywanie istniejącego modelu...")
            model.load()
        else:
            print("Tworzenie nowego modelu...")

        model.train(X_train, Y_train, epochs=1000)
        model.save()
        preds = model.predict(X_test)
        true = np.argmax(Y_test, axis=0)
        acc = np.mean(preds == true)
        print(f"Test accuracy: {acc * 100:.2f}%")
