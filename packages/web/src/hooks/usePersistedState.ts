import { useState, useEffect, useRef, useCallback } from "react";

const PREFIX = "voile:v1:";

function isSupported(): boolean {
  if (typeof window === "undefined") return false;
  try {
    const test = "__voile_test__";
    localStorage.setItem(test, test);
    localStorage.removeItem(test);
    return true;
  } catch {
    return false;
  }
}

export function usePersistedState<T>(key: string, defaultValue: T) {
  const fullKey = `${PREFIX}${key}`;
  const supported = useRef(isSupported());

  const [value, setValue] = useState<T>(() => {
    if (!supported.current) return defaultValue;
    try {
      const raw = localStorage.getItem(fullKey);
      if (raw === null) return defaultValue;
      return JSON.parse(raw) as T;
    } catch {
      return defaultValue;
    }
  });

  const writeTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const setValueDebounced = useCallback((next: T | ((prev: T) => T)) => {
    setValue((prev) => {
      const resolved = typeof next === "function"
        ? (next as (prev: T) => T)(prev)
        : next;
      if (!supported.current) return resolved;
      if (writeTimer.current) clearTimeout(writeTimer.current);
      writeTimer.current = setTimeout(() => {
        try {
          localStorage.setItem(fullKey, JSON.stringify(resolved));
        } catch (e) {
          console.warn(`[Voile] localStorage write failed for ${key}`, e);
        }
      }, 200);
      return resolved;
    });
  }, [fullKey, key]);

  // Cross-tab sync
  useEffect(() => {
    if (!supported.current) return;
    const onStorage = (e: StorageEvent) => {
      if (e.key === fullKey && e.newValue !== null) {
        try {
          setValue(JSON.parse(e.newValue) as T);
        } catch {}
      }
    };
    window.addEventListener("storage", onStorage);
    return () => window.removeEventListener("storage", onStorage);
  }, [fullKey]);

  return [value, setValueDebounced] as const;
}
