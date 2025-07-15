import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // 1) Wyłącz stricte ESLint-owe błędy podczas builda
  eslint: {
    ignoreDuringBuilds: true,
  },

  // 2) Proxy wewnętrzny: wszystko z /api/* pójdzie do Flaska
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: "http://localhost:5000/api/:path*", 
      },
    ];
  },

  // ...jeśli masz inne opcje, zostaw je tutaj
};

export default nextConfig;

