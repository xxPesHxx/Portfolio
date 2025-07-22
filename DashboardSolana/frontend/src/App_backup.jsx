import { useEffect, useState, useRef } from "react";
import { Line } from "react-chartjs-2";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Legend,
  Title,
  Tooltip
} from "chart.js";

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

function App() {
  const [status, setStatus] = useState(null);
  const [history, setHistory] = useState([]);
  const fetchRef = useRef();

  useEffect(() => {
    fetchRef.current = async () => {
      try {
        const res = await fetch("http://149.50.101.154:5001/api/status");
        const json = await res.json();
        setStatus(json);

        const cpuPerCore = json.system.cpu_per_core;
        if (Array.isArray(cpuPerCore)) {
          const now = new Date().toLocaleTimeString();
          setHistory(h => {
            const next = [...h, { time: now, perCore: cpuPerCore }];
            return next.slice(-20);
          });
        }
      } catch (e) {
        console.error(e);
      }
    };

    fetchRef.current();
    const id = setInterval(() => fetchRef.current(), 5000);
    return () => clearInterval(id);
  }, []);

  if (!status) {
    return <div style={{ padding: 20 }}>Ładowanie danych…</div>;
  }

  const { system, validator } = status;
  const {
    cpu_percent,
    load_avg,
    platform,
    uptime,
    disk,
    memory,
  } = system;

  // Uptime
  const hours = Math.floor(uptime / 3600);
  const mins = Math.floor((uptime % 3600) / 60);

  // Konwersja GiB
  const toGiB = bytes => (bytes / 1024 ** 3).toFixed(1);

  // Przygotowanie wykresu per‑core jeśli mamy dane
  let chartSection = null;
  if (history.length && Array.isArray(history[0].perCore)) {
    const labels = history.map(h => h.time);
    const coreCount = history[0].perCore.length;
    const datasets = Array.from({ length: coreCount }, (_, i) => ({
      label: `Core ${i}`,
      data: history.map(h => h.perCore[i]),
      fill: false,
      tension: 0.3,
    }));
    const data = { labels, datasets };
    const options = {
      responsive: true,
      plugins: {
        legend: { position: "bottom" },
        title: { display: true, text: "CPU per‑core (%)" },
      },
      scales: {
        y: { min: 0, max: 100, title: { display: true, text: "%" } },
        x: { title: { display: true, text: "Time" } },
      },
    };
    chartSection = (
      <section style={{ marginTop: 20 }}>
        <h2>📊 CPU per‑core</h2>
        <Line data={data} options={options} />
      </section>
    );
  }

  return (
    <div style={{ fontFamily: "Arial, sans-serif", padding: 20, maxWidth: 900, margin: "0 auto" }}>
      <h1 style={{ color: "#3b82f6", textAlign: "center" }}>Solana Validator Dashboard</h1>

      <div style={{
        display: "flex",
        flexWrap: "wrap",
        gap: 16,
        marginTop: 16
      }}>
        <div style={{
          background: "#374151",
          borderRadius: 8,
          padding: 16,
          flex: "1 1 calc(50% - 16px)"
        }}>
          <h2 style={{ color: "#3b82f6" }}>🖥 System</h2>
          <p><strong>Platforma:</strong> {platform}</p>
          <p><strong>Uptime:</strong> {hours}h {mins}m</p>
          <p><strong>CPU:</strong> {cpu_percent}%</p>
          {load_avg && <p><strong>Load avg:</strong> {load_avg.map(l => l.toFixed(2)).join(", ")}</p>}
        </div>

        <div style={{
          background: "#374151",
          borderRadius: 8,
          padding: 16,
          flex: "1 1 calc(50% - 16px)"
        }}>
          <h2 style={{ color: "#3b82f6" }}>💾 Dysk</h2>
          <p><strong>Użyte:</strong> {toGiB(disk.used)} GiB</p>
          <p><strong>Wolne:</strong> {toGiB(disk.free)} GiB ({disk.percent}%)</p>
        </div>

        <div style={{
          background: "#374151",
          borderRadius: 8,
          padding: 16,
          flex: "1 1 calc(50% - 16px)"
        }}>
          <h2 style={{ color: "#3b82f6" }}>🧠 Pamięć</h2>
          <p><strong>Użyte:</strong> {toGiB(memory.used)} GiB</p>
          <p><strong>Dostępne:</strong> {toGiB(memory.available)} GiB ({memory.percent}%)</p>
        </div>

        <div style={{
          background: "#374151",
          borderRadius: 8,
          padding: 16,
          flex: "1 1 calc(50% - 16px)"
        }}>
          <h2 style={{ color: "#3b82f6" }}>🔗 Validator</h2>
          <p>Status: <span style={{ color: validator === "online" ? "#10b981" : "#ef4444" }}>{validator}</span></p>
        </div>
      </div>

      {chartSection}
    </div>
  );
}

export default App;

