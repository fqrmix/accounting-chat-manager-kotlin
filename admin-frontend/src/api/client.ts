// Thin fetch wrapper around the Ktor admin REST API. All authenticated
// requests carry a JWT Bearer Authorization header; a 401 response is
// surfaced as UnauthorizedError so the UI can drop the session.

const BASE_URL = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? "/api";

export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

export class UnauthorizedError extends ApiError {
  constructor(message = "Unauthorized") {
    super(401, message);
    this.name = "UnauthorizedError";
  }
}

async function parseError(res: Response): Promise<string> {
  try {
    const body = await res.json();
    if (body && typeof body.error === "string") return body.error;
  } catch {
    // not JSON — fall through
  }
  return res.statusText || `HTTP ${res.status}`;
}

async function request<T>(
  token: string | null,
  method: string,
  path: string,
  body?: unknown,
): Promise<T> {
  const headers: Record<string, string> = {};
  if (token) headers.Authorization = `Bearer ${token}`;
  let payload: BodyInit | undefined;

  if (body instanceof FormData) {
    payload = body;
  } else if (body !== undefined) {
    headers["Content-Type"] = "application/json";
    payload = JSON.stringify(body);
  }

  const res = await fetch(BASE_URL + path, { method, headers, body: payload });

  if (res.status === 401) throw new UnauthorizedError(await parseError(res));
  if (!res.ok) throw new ApiError(res.status, await parseError(res));

  if (res.status === 204) return undefined as T;
  const contentType = res.headers.get("content-type") ?? "";
  if (contentType.includes("application/json")) return (await res.json()) as T;
  return undefined as T;
}

export const api = {
  get: <T>(token: string, path: string) => request<T>(token, "GET", path),
  post: <T>(token: string, path: string, body?: unknown) => request<T>(token, "POST", path, body),
  put: <T>(token: string, path: string, body?: unknown) => request<T>(token, "PUT", path, body),
  delete: <T>(token: string, path: string) => request<T>(token, "DELETE", path),

  /** For endpoints that don't require a session yet, e.g. login. */
  postPublic: <T>(path: string, body?: unknown) => request<T>(null, "POST", path, body),
};
