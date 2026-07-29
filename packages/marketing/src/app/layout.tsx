import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Voile VPN — VPN transparent propulsé par Cloudflare",
  description:
    "Protégez votre vie privée avec Voile VPN, un VPN gratuit et rapide utilisant l'infrastructure Cloudflare WARP. Disponible sur web et Android.",
  keywords: "VPN, Cloudflare WARP, WireGuard, vie privée, anonymat, gratuit",
  openGraph: {
    title: "Voile VPN",
    description: "VPN transparent, gratuit et rapide propulsé par Cloudflare WARP.",
    url: "https://voile.ricecloud.com",
    siteName: "Voile VPN",
    type: "website",
  },
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="fr">
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
        <link
          href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@300;400;500;600;700&family=Inter:wght@300;400;500;600&family=JetBrains+Mono:wght@400;500&display=swap"
          rel="stylesheet"
        />
      </head>
      <body>{children}</body>
    </html>
  );
}
