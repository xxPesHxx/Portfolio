export default function HomePage() {
  return (
    <main style={{ padding: '2rem', fontFamily: 'sans-serif' }}>
      <h1>Jan Kowalski</h1>
      <p>Programista z doświadczeniem w Python, Rust, Solana...</p>
      <h2>Projekty</h2>
      <ul>
        <li>
          <a href="/tictactoe">Kolko i krzyzyk z AI</a>
        </li>
        {/* Dodaj wiecej projektow */}
      </ul>
    </main>
  );
}
