export interface WarpTrace {
  ip?: string;
  colo?: string;
}

export async function fetchWarpTrace(fetcher: typeof fetch): Promise<WarpTrace | null> {
  // Stub implementation
  try {
    const res = await fetcher("https://1.1.1.1/cdn-cgi/trace");
    const text = await res.text();
    return parseWarpTrace(text);
  } catch {
    return null;
  }
}

export function parseWarpTrace(text: string): WarpTrace {
  const trace: Record<string, string> = {};
  for (const line of text.split("\n")) {
    const [key, val] = line.split("=");
    if (key && val) trace[key] = val;
  }
  return {
    ip: trace.ip,
    colo: trace.colo,
  };
}
