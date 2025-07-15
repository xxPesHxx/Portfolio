/* eslint-disable prefer-const */

'use client';

import { useState } from 'react';
import Image from 'next/image';

type CellValue = 0 | 1 | -1;

export default function TicTacToeBoard() {
  const [board, setBoard] = useState<CellValue[]>(Array(9).fill(0));
  const [currentPlayer, setCurrentPlayer] = useState<1 | -1>(1);
  const [message, setMessage] = useState<string>('');

  const getSymbol = (val: CellValue) => {
    if (val === 1)
      return <img src="/x_icon.png" alt="X" width={60} height={60} />;
    if (val === -1)
      return <img src="/o_icon.png" alt="O" width={60} height={60} />;
    return null;
  };

  const getSymbolText = (val: CellValue): string => {
    if (val === 1) return 'X';
    if (val === -1) return 'O';
    return '';
  };



  const checkWinner = (b: CellValue[]) => {
    const lines = [
      [0,1,2],[3,4,5],[6,7,8], // rows
      [0,3,6],[1,4,7],[2,5,8], // columns
      [0,4,8],[2,4,6],         // diagonals
    ];
    for (let [a,b1,c] of lines) {
      const sum = b[a] + b[b1] + b[c];
      if (sum === 3) return 1;
      if (sum === -3) return -1;
    }
    if (!b.includes(0)) return 0;
    return null;
  };

  /*const handleClick = (index: number) => {
    if (board[index] !== 0 || message) return;

    const newBoard = [...board];
    newBoard[index] = currentPlayer;
    setBoard(newBoard);
    console.log('Kliknięto pole:', index);
    console.log('Aktualna plansza:', board);

    const winner = checkWinner(newBoard);
    if (winner !== null) {
      setMessage(winner === 0 ? 'Remis!' : `Wygrał ${getSymbolText(winner)}`);
    } else {
      setCurrentPlayer(currentPlayer === 1 ? -1 : 1);
    }
  };
*/
  const handleClick = async (index: number) => {
    if (board[index] !== 0 || message) return;

    const newBoard = [...board];
    newBoard[index] = currentPlayer;
    setBoard(newBoard);

    const winner = checkWinner(newBoard);
    if (winner !== null) {
      setMessage(winner === 0 ? 'Remis!' : `Wygrał: ${getSymbol(winner)}`);
      return;
    }

    // 🔁 Zapytaj backend o ruch AI
    try {
      const response = await fetch('/api/move', {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          board: newBoard,
          player: -currentPlayer, // AI to przeciwnik
        }),
      });

      const data = await response.json();
      const aiMove = data.ai_move;

      if (newBoard[aiMove] !== 0) return; // AI podał zły ruch
      newBoard[aiMove] = -currentPlayer as CellValue;
      setBoard([...newBoard]);

      const newWinner = checkWinner(newBoard);
      if (newWinner !== null) {
        setMessage(newWinner === 0 ? 'Remis!' : `Wygrał: ${getSymbolText(newWinner)}`);
      }
    } catch (error) {
      console.error("Błąd zapytania do backendu:", error);
    }
  };

  const resetGame = () => {
    setBoard(Array(9).fill(0));
    setCurrentPlayer(1);
    setMessage('');
  };

  return (
    <div style={{ textAlign: 'center', marginTop: '2rem' }}>
      <h2>Gracz: {getSymbolText(currentPlayer)}</h2>
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(3, 100px)',
        gap: '10px',
        justifyContent: 'center',
      }}>
        {board.map((val, i) => (
          <div
            key={i}
            onClick={() => handleClick(i)}
            style={{
              width: '100px',
              height: '100px',
              backgroundColor: '#eee',
              fontSize: '2rem',
              lineHeight: '100px',
              textAlign: 'center',
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center',
              cursor: val === 0 && !message ? 'pointer' : 'default',
              border: '1px solid #aaa',
              userSelect: 'none',
            }}
          >
            {getSymbol(val)}
          </div>
        ))}
      </div>
      {message && (
        <>
          <p style={{ marginTop: '1rem', fontSize: '1.2rem' }}>{message}</p>
          <button onClick={resetGame} style={{ padding: '0.5rem 1rem', marginTop: '1rem' }}>
            Zagraj ponownie
          </button>
        </>
      )}
    </div>
  );
}
