'use client';
import { useState } from 'react';
import Image from 'next/image';
import './../globals.css';

type CellValue = 0 | 1 | -1;

export default function TicTacToeBoard() {
  const [board, setBoard] = useState<CellValue[]>(Array(9).fill(0));
  const [message, setMessage] = useState<string>('');
  const [xHistory, setXHistory] = useState<number[]>([]);
  const [oHistory, setOHistory] = useState<number[]>([]);
  const [fadingIndex, setFadingIndex] = useState<number | null>(null);
  const [isProcessing, setIsProcessing] = useState(false);
  const [fadeMode, setFadeMode] = useState<boolean>(false);

  const winningCombinations = [
    [0,1,2],[3,4,5],[6,7,8],
    [0,3,6],[1,4,7],[2,5,8],
    [0,4,8],[2,4,6],
  ];


  const isNextToFade = (idx: number): boolean => {
    if (!fadeMode) return false;
    if (xHistory.length >= 3 && idx === xHistory[0]) return true;
    if (oHistory.length >= 3 && idx === oHistory[0]) return true;
    return false;
  };

  const getSymbol = (val: CellValue, idx: number) => {
    const gray = isNextToFade(idx);
    const fade = fadingIndex === idx;
    const style = fade ? { opacity: 0.3, transition: 'opacity 0.5s' } : {};


    if (val === 1) {
      const src = gray ? '/x_gray.png' : '/x_icon.png';
      return <Image src={src} alt="X" width={60} height={60} style={style} />;
    }
    if (val === -1) {
      const src = gray ? '/o_gray.png' : '/o_icon.png';
      return <Image src={src} alt="O" width={60} height={60} style={style} />;
    }
    return null;
  };

  const checkWinner = (b: CellValue[]): CellValue | null => {
    for (const [a,b1,c] of winningCombinations) {
      const sum = b[a] + b[b1] + b[c];
      if (sum === 3) return 1;
      if (sum === -3) return -1;
    }
    return b.includes(0) ? null : 0;
  };

  const removeOldestMove = (history: number[], setHistory: (h: number[]) => void) => {
    if (!fadeMode || history.length <= 3) return;
    const oldest = history[0];
    setFadingIndex(oldest);
    setTimeout(() => {
      setBoard(prev => {
        const nb = [...prev]; nb[oldest] = 0; return nb;
      });
      setHistory(history.slice(1));
      setFadingIndex(null);
    }, 500);
  };

  const doPlayerMove = (index: number) => {
    setBoard(prev => { const nb = [...prev]; nb[index] = 1; return nb; });
    const newX = [...xHistory, index];
    setXHistory(newX);
    return newX;
  };

  const doAiMove = async (currentBoard: CellValue[]) => {
    if (fadeMode) await new Promise(resolve => setTimeout(resolve, 500));
    try {
      const res = await fetch('/api/move', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ board: currentBoard, player: -1 }),
      });
      const { ai_move } = await res.json();
      if (currentBoard[ai_move] === 0) {
        setBoard(prev => { const nb = [...prev]; nb[ai_move] = -1; return nb; });
        const newO = [...oHistory, ai_move];
        setOHistory(newO);
        return newO;
      }
    } catch (e) {
      console.error('AI error', e);
    }
    return oHistory;
  };

  const playTurn = async (index: number) => {
    if (isProcessing || message || board[index] !== 0) return;
    setIsProcessing(true);

    // Player
    const newX = doPlayerMove(index);
    let tempBoard = board.map((v,i) => (i===index?1:v));
    let win = checkWinner(tempBoard);
    if (win !== null) {
      setMessage(win===0?'Remis!':`Wygrał: ${win===1?'X':'O'}`);
      setIsProcessing(false);
      return;
    }
    removeOldestMove(newX, setXHistory);

    // AI
    const newO = await doAiMove(tempBoard);
    tempBoard = tempBoard.map((v,i)=>(newO.includes(i)&&board[i]===0?-1:v));
    win = checkWinner(tempBoard);
    if (win !== null) {
      setMessage(win===0?'Remis!':`Wygrał: ${win===1?'X':'O'}`);
      setIsProcessing(false);
      return;
    }
    removeOldestMove(newO, setOHistory);

    setIsProcessing(false);
  };

  const resetGame = () => {
    setBoard(Array(9).fill(0)); setXHistory([]); setOHistory([]);
    setMessage(''); setFadingIndex(null); setIsProcessing(false);
  };

  return (
    <div className="container">
      <div className="mode-select">
        <button
          className={fadeMode ? '' : 'active'}
          onClick={() => { resetGame(); setFadeMode(false); }}
        >
          Klasyczny
        </button>
        <button
          className={fadeMode ? 'active' : ''}
          onClick={() => { resetGame(); setFadeMode(true); }}
        >
          Znikanie
        </button>
      </div>
      <h2 className="status">{message || 'Gracz: X'}</h2>
      <div className="board">
        {board.map((v,i)=>(
          <div key={i} className="cell" onClick={()=>playTurn(i)}>
            {getSymbol(v,i)}
          </div>
        ))}
      </div>
      {message && <button className="reset-button" onClick={resetGame}>Zagraj ponownie</button>}
    </div>
  );
}
