import { api } from "./client";

export interface LoginResponse {
  token: string;
}

export function login(username: string, password: string): Promise<LoginResponse> {
  return api.postPublic<LoginResponse>("/auth/login", { username, password });
}
