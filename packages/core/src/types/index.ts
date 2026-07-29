// Types partagés utilisés cross-platform

export type ConnectionStatus = "disconnected" | "connecting" | "connected" | "error";

export interface VpnSession {
  id: string;
  serverId: number;
  startedAt: number;         // Unix timestamp ms
  endedAt: number | null;
  bytesDown: number;
  bytesUp: number;
  durationSec: number;
  vpnIp: string;
  colo: string | null;
}

export interface UserSettings {
  protocol: "WireGuard" | "OpenVPN" | "IKEv2";
  killSwitch: boolean;
  autoConnect: boolean;
  cyberSec: boolean;
  splitTunneling: boolean;
  splitApps: string[];
  selectedServerId: number;
}

export const DEFAULT_SETTINGS: UserSettings = {
  protocol: "WireGuard",
  killSwitch: true,
  autoConnect: false,
  cyberSec: true,
  splitTunneling: false,
  splitApps: [],
  selectedServerId: 1,
} as const;
