import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from "react";
import { login as loginRequest } from "../api/auth";

interface AuthState {
  /** The JWT, or null when logged out. */
  token: string | null;
  username: string | null;
  signIn: (username: string, password: string) => Promise<void>;
  signOut: () => void;
}

const STORAGE_KEY = "accounting_admin_auth";

const AuthContext = createContext<AuthState | undefined>(undefined);

interface StoredAuth {
  token: string;
  username: string;
}

function loadStored(): StoredAuth | null {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as StoredAuth;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [stored, setStored] = useState<StoredAuth | null>(loadStored);

  const signIn = useCallback(async (username: string, password: string) => {
    const { token } = await loginRequest(username, password);
    const value: StoredAuth = { token, username };
    localStorage.setItem(STORAGE_KEY, JSON.stringify(value));
    setStored(value);
  }, []);

  const signOut = useCallback(() => {
    localStorage.removeItem(STORAGE_KEY);
    setStored(null);
  }, []);

  const value = useMemo<AuthState>(
    () => ({ token: stored?.token ?? null, username: stored?.username ?? null, signIn, signOut }),
    [stored, signIn, signOut],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
