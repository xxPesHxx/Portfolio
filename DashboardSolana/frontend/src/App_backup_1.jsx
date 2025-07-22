import React, { useEffect, useState } from "react";
import { Line } from "react-chartjs-2";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
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

export default function App() {
  const [data, setData] = useState(null);
  const [history, setHistory] = useState([]);

  // Pobieranie danych z backendu co 5 sekund
  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await fetch("http://149.50.101.154:5001/api/status");
        const json = await res.json();
        // Zapisujemy cały obiekt, żeby mieć dostęp do validator
        setData(json);

        setHistory((h) => {
          const time = new Date().toLocaleTimeString();
          const newEntry = {
            time,
            cpu: json.system.cpu_percent,
            perCore: json.system.cpu_per_core || [],
          };
          const newHist = [...h, newEntry];
          if (newHist.length > 20) newHist.shift();
          return newHist;
        });
      } catch (e) {
        console.error("Błąd podczas pobierania danych:", e);
      }
    };

    fetchData();
    const interval = setInterval(fetchData, 5000);
    return () => clearInterval(interval);
  }, []);

  if (!data) return <div>Ładowanie danych...</div>;

  // Destrukturyzacja danych: system i validator
  const {
    system: { cpu_percent, memory, disk, cpu_per_core },
    validator,
  } = data;

  // Wykres całkowitego CPU
  const cpuData = {
    labels: history.map((h) => h.time),
    datasets: [
      {
        label: "CPU %",
        data: history.map((h) => h.cpu),
        fill: false,
        borderColor: "#3b82f6",
        backgroundColor: "#2563eb",
        tension: 0.3,
        pointRadius: 2,
      },
    ],
  };
  const cpuOptions = {
    responsive: true,
    plugins: {
      legend: { display: true },
      title: { display: true, text: "Obciążenie CPU" },
    },
    scales: {
      y: { min: 0, max: 100, title: { display: true, text: "%" } },
    },
  };

  // Osobne wykresy CPU per core
  let coresCharts = null;
  if (history.length && Array.isArray(cpu_per_core) && cpu_per_core.length > 0) {
    const labels = history.map((h) => h.time);
    const coreCount = cpu_per_core.length;

    coresCharts = (
      <section style={{ marginTop: 30 }}>
        <h2>Wykresy CPU per core (oddzielne wykresy)</h2>
        <div
          style={{
            display: "grid",
            gridTemplateColumns: "repeat(auto-fit, minmax(250px, 1fr))",
            gap: 20,
          }}
        >
          {Array.from({ length: coreCount }, (_, i) => {
            const coreData = {
              labels,
              datasets: [
                {
                  label: `Core ${i}`,
                  data: history.map((h) => h.perCore[i]),
                  fill: false,
                  tension: 0.3,
                  borderColor: "#3b82f6",
                  backgroundColor: "#2563eb",
                  pointRadius: 1,
                },
              ],
            };
            const coreOptions = {
              responsive: true,
              plugins: {
                legend: { display: false },
                title: { display: true, text: `Rdzeń ${i}` },
              },
              scales: {
                y: { min: 0, max: 100, title: { display: true, text: "%" } },
                x: { display: false },
              },
            };
            return (
              <div
                key={i}
                style={{
                  background: "#374151",
                  borderRadius: 8,
                  padding: 12,
                  color: "white",
                }}
              >
                <Line data={coreData} options={coreOptions} />
              </div>
            );
          })}
        </div>
      </section>
    );
  }

  return (
    <div
      style={{
        fontFamily: "Arial, sans-serif",
        background: "#1f2937",
        color: "white",
        padding: 20,
        maxWidth: 1000,
        margin: "0 auto",
        minHeight: "100vh",
      }}
    >
      <h1>Dashboard Solana Validator</h1>

      <section
        style={{
          display: "flex",
          gap: 20,
          flexWrap: "wrap",
          marginTop: 20,
        }}
      >
        {/* CPU overall */}
        <div
          style={{
            flex: "1 1 220px",
            background: "#374151",
            borderRadius: 8,
            padding: 20,
          }}
        >
          <h2>CPU</h2>
          <p style={{ fontSize: 28, margin: 0 }}>
            {cpu_percent.toFixed(1)}%
          </p>
        </div>

        {/* RAM */}
        <div
          style={{
            flex: "1 1 220px",
            background: "#374151",
            borderRadius: 8,
            padding: 20,
          }}
        >
          <h2>RAM</h2>
          <p style={{ margin: 0 }}>
            Wolne: {(memory.available / 1024 ** 3).toFixed(2)} GB
          </p>
          <p style={{ margin: 0 }}>
            Użyte: {(memory.used / 1024 ** 3).toFixed(2)} GB
          </p>
          <p style={{ margin: 0 }}>
            Całkowite: {(memory.total / 1024 ** 3).toFixed(2)} GB
          </p>
        </div>

        {/* Dysk */}
        <div
          style={{
            flex: "1 1 220px",
            background: "#374151",
            borderRadius: 8,
            padding: 20,
          }}
        >
          <h2>Dysk</h2>
          <p style={{ margin: 0 }}>
            Wolne: {(disk.free / 1024 ** 3).toFixed(2)} GB
          </p>
          <p style={{ margin: 0 }}>
            Użycie: {disk.percent.toFixed(1)}%
          </p>
          <p style={{ margin: 0 }}>
            Całkowite: {(disk.total / 1024 ** 3).toFixed(2)} GB
          </p>
        </div>

        {/* Status validatora */}
        <div
          style={{
            flex: "1 1 220px",
            background: "#374151",
            borderRadius: 8,
            padding: 20,
            display: "flex",
            flexDirection: "column",
            justifyContent: "center",
            alignItems: "center",
            fontWeight: "bold",
            fontSize: 18,
            color:
              validator?.toLowerCase() === "online"
                ? "#4ade80"
                : "#f87171",
          }}
        >
          Status: <span>{validator ?? "—"}</span>
        </div>
      </section>

      {/* Wykres całkowitego CPU */}
      <section style={{ marginTop: 30 }}>
        <Line data={cpuData} options={cpuOptions} />
      </section>

      {/* Wykresy CPU per core */}
      {coresCharts}
    </div>
  );
}
