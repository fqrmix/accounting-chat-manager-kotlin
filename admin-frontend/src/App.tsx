import { useMemo } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { QueryClient, QueryClientProvider, MutationCache, QueryCache } from "@tanstack/react-query";

import { useAuth } from "./auth/AuthContext";
import { UnauthorizedError } from "./api/client";
import { useNotification } from "./ui/NotificationContext";
import Layout from "./components/Layout";
import LoginPage from "./pages/LoginPage";
import UsersPage from "./pages/UsersPage";
import SchedulePage from "./pages/SchedulePage";

export default function App() {
  const { token, signOut } = useAuth();
  const { notify } = useNotification();

  // A 401 anywhere means the stored token is stale/expired — drop the session.
  const queryClient = useMemo(() => {
    const onError = (error: unknown) => {
      if (error instanceof UnauthorizedError) {
        signOut();
        notify("Сессия истекла, войди заново", "warning");
      }
    };
    return new QueryClient({
      queryCache: new QueryCache({ onError }),
      mutationCache: new MutationCache({ onError }),
      defaultOptions: { queries: { retry: false, refetchOnWindowFocus: false } },
    });
  }, [signOut, notify]);

  if (!token) {
    return <LoginPage />;
  }

  return (
    <QueryClientProvider client={queryClient}>
      <Layout>
        <Routes>
          <Route path="/" element={<Navigate to="/users" replace />} />
          <Route path="/users" element={<UsersPage />} />
          <Route path="/schedule" element={<SchedulePage />} />
          <Route path="*" element={<Navigate to="/users" replace />} />
        </Routes>
      </Layout>
    </QueryClientProvider>
  );
}
