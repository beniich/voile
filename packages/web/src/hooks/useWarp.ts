import { useState, useCallback } from "react";
import { fetchWarpTrace } from "@voile/core/warp";
import { usePersistedState } from "./usePersistedState.js";
import { logError, logInfo } from "../lib/logError.js";
import type { WarpInfo, TunnelState } from "../types.js";

const DEFAULT_INFO: WarpInfo = {
  ip: "—.—.—.—",
  colo: "—",
  download: "0.0",
  upload: "0.0",
  duration: "00:00:00",
};

export function useWarp() {
  const [state, setState] = useState<TunnelState>("disconnected");
  const [warpInfo, setWarpInfo] = usePersistedState<WarpInfo | null>("warpInfo", null);
  const [realIp, setRealIp] = usePersistedState<string | null>("realIp", null);

  const connect = useCallback(async () => {
    setState("connecting");
    logInfo("WARP connect initiated");

    try {
      const trace = await fetchWarpTrace(fetch);

      if (trace) {
        setState("connected");
        setWarpInfo({
          ...DEFAULT_INFO,
          ip: trace.ip ?? "via Cloudflare",
          colo: trace.colo ?? "—",
        });
        logInfo("WARP connected", { colo: trace.colo, ip: trace.ip });
      } else {
        setState("error");
        setTimeout(() => setState("disconnected"), 3000);
        logError(new Error("WARP trace returned null"), {
          feature: "warp",
          action: "connect",
        });
      }
    } catch (e) {
      setState("error");
      setTimeout(() => setState("disconnected"), 3000);
      logError(e, { feature: "warp", action: "connect" });
    }
  }, [setWarpInfo]);

  const disconnect = useCallback(() => {
    setState("disconnected");
    setWarpInfo(null);
    logInfo("WARP disconnected");
  }, [setWarpInfo]);

  const captureRealIp = useCallback(async () => {
    try {
      const res = await fetch("https://api.ipify.org?format=json");
      const data = await res.json();
      setRealIp(data.ip);
      logInfo("Real IP captured", { ip: data.ip });
    } catch (e) {
      setRealIp("—");
      logError(e, { feature: "warp", action: "captureRealIp" });
    }
  }, [setRealIp]);

  return { state, warpInfo, realIp, connect, disconnect, captureRealIp };
}
