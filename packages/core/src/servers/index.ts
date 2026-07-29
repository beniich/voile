export interface Server {
  id: number;
  country: string;
  city: string;
  flag: string;
  ping: number;
  load: number;
}

/**
 * Liste canonique des serveurs — partagée Web et Android.
 * Android la consomme via un JSON statique exporté par gen:servers.
 */
export const SERVERS: readonly Server[] = [
  { id: 1, country: "France",     city: "Paris",      flag: "🇫🇷", ping: 12,  load: 34 },
  { id: 2, country: "Pays-Bas",   city: "Amsterdam",  flag: "🇳🇱", ping: 19,  load: 21 },
  { id: 3, country: "Allemagne",  city: "Francfort",  flag: "🇩🇪", ping: 24,  load: 58 },
  { id: 4, country: "Maroc",      city: "Casablanca", flag: "🇲🇦", ping: 8,   load: 42 },
  { id: 5, country: "Canada",     city: "Montréal",   flag: "🇨🇦", ping: 87,  load: 15 },
  { id: 6, country: "Singapour",  city: "Singapour",  flag: "🇸🇬", ping: 145, load: 63 },
  { id: 7, country: "Japon",      city: "Tokyo",      flag: "🇯🇵", ping: 168, load: 29 },
] as const;

export function findServer(id: number): Server | undefined {
  return SERVERS.find((s) => s.id === id);
}

export function sortByPing(servers: readonly Server[]): Server[] {
  return [...servers].sort((a, b) => a.ping - b.ping);
}

export function filterByLoad(servers: readonly Server[], maxLoad: number): Server[] {
  return servers.filter((s) => s.load <= maxLoad);
}
