export type TunnelState = "disconnected" | "connecting" | "connected" | "error";

export interface WarpInfo {
  ip: string;
  colo: string;
  download: string;
  upload: string;
  duration: string;
}

export interface Server {
  id: number;
  country: string;
  city: string;
  flag: string;
  ping: number;
  load: number;
}

export interface VoileSettings {
  protocol: "WireGuard" | "OpenVPN";
  killSwitch: boolean;
  autoConnect: boolean;
  cyberSec: boolean;
  splitTunneling: boolean;
  splitApps: string[];
}

export const DEFAULT_SETTINGS: VoileSettings = {
  protocol: "WireGuard",
  killSwitch: true,
  autoConnect: false,
  cyberSec: true,
  splitTunneling: false,
  splitApps: [],
};
