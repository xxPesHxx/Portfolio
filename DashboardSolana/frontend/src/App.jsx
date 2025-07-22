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

// Prosty komponent Card z inline-stylami
function Card({ title, children, highlight }) {
  const cardStyle = {
    flex: "1 1 200px",
    background: "#374151",
    borderRadius: 8,
    padding: 16,
    boxShadow: "0 2px 6px rgba(0,0,0,0.3)",
    color: "white",
    display: "flex",
    flexDirection: "column",
    gap: 8,
  };
  const titleStyle = {
    margin: 0,
    fontSize: "1.1rem",
    borderBottom: "1px solid #4b5563",
    paddingBottom: 4,
  };
  const contentStyle = {
    fontSize: "1.25rem",
    fontWeight: highlight ? "bold" : "normal",
  };

  return (
    <div style={cardStyle}>
      <h3 style={titleStyle}>{title}</h3>
      <div style={contentStyle}>{children}</div>
    </div>
  );
}

export default function App() {
  const [data, setData] = useState(null);
  const [history, setHistory] = useState([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await fetch("/dashboard/api/status");
        const json = await res.json();
        setData(json);
        setHistory((h) => {
          const time = new Date().toLocaleTimeString();
          const newEntry = {
            time,
            cpu: json.system.cpu_percent,
            perCore: json.system.cpu_per_core || [],
          };
          return [...h, newEntry].slice(-20);
        });
      } catch (e) {
        console.error(e);
      }
    };
    fetchData();
    const interval = setInterval(fetchData, 5000);
    return () => clearInterval(interval);
  }, []);

  if (!data)
    return (
      <div style={{ textAlign: "center", fontSize: "1.25rem", padding: 40 }}>
        Ładowanie danych...
      </div>
    );

  const {
    system: { cpu_percent, memory, disk, cpu_per_core },
    validator,
  } = data;

  const cpuData = {
    labels: history.map((h) => h.time),
    datasets: [
      {
        label: "CPU %",
        data: history.map((h) => h.cpu),
        fill: false,
        borderColor: "#3b82f6",
        tension: 0.3,
        pointRadius: 2,
      },
    ],
  };
  const cpuOptions = {
    responsive: true,
    plugins: { legend: { display: false }, title: { display: true, text: "Obciążenie CPU" } },
    scales: { y: { min: 0, max: 100 } },
  };

  const containerStyle = {
    fontFamily: "Arial, sans-serif",
    background: "#1f2937",
    color: "white",
    padding: 24,
    maxWidth: 960,
    margin: "auto",
  };
  const statsStyle = {
    display: "flex",
    flexWrap: "wrap",
    gap: 16,
    marginBottom: 32,
  };
  const chartStyle = {
    background: "#374151",
    padding: 16,
    borderRadius: 8,
    marginBottom: 32,
  };
  const coresSectionStyle = { marginTop: 32 };
  const coresGridStyle = {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))",
    gap: 16,
  };
  const coreChartStyle = {
    background: "#374151",
    padding: 12,
    borderRadius: 8,
  };

  return (
    <div style={containerStyle}>
      <h1 style={{ textAlign: "center", marginBottom: 24 }}>Dashboard Solana Validator</h1>

      <div style={statsStyle}>
        <Card title="CPU" highlight>
          {cpu_percent.toFixed(1)}%
        </Card>
        <Card title="RAM">
          Użyte: {(memory.used / 1024 ** 3).toFixed(2)} GB
          <br />
          Wolne: {(memory.available / 1024 ** 3).toFixed(2)} GB
        </Card>
        <Card title="Dysk">
          Użycie: {disk.percent.toFixed(1)}%
          <br />
          Wolne: {(disk.free / 1024 ** 3).toFixed(2)} GB
        </Card>
        <Card title="Status">
          <span
            style={{
              color: validator === "online" ? "#4ade80" : "#f87171",
              fontWeight: "bold",
            }}
          >
            {validator}
          </span>
        </Card>
      </div>

      <div style={chartStyle}>
        <Line data={cpuData} options={cpuOptions} />
      </div>

      {cpu_per_core.length > 0 && (
        <section style={coresSectionStyle}>
          <h2 style={{ marginBottom: 16 }}>Wykresy CPU per Rdzeń</h2>
          <div style={coresGridStyle}>
            {cpu_per_core.map((_, i) => {
              const dataSet = {
                labels: history.map((h) => h.time),
                datasets: [
                  {
                    label: `Core ${i}`,
                    data: history.map((h) => h.perCore[i]),
                    fill: false,
                    tension: 0.3,
                    borderColor: "#3b82f6",
                    pointRadius: 1,
                  },
                ],
              };
              const options = {
                responsive: true,
                plugins: { legend: { display: false }, title: { display: true, text: `Rdzeń ${i}` } },
                scales: { y: { min: 0, max: 100 }, x: { display: false } },
              };
              return (
                <div key={i} style={coreChartStyle}>
                  <Line data={dataSet} options={options} />
                </div>
              );
            })}
          </div>
        </section>
      )}
    </div>
  );
}

