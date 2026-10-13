import { api } from "./client";
import type { User } from "./types";

export function listUsers(token: string): Promise<User[]> {
  return api.get<User[]>(token, "/users");
}

export function createUser(token: string, user: User): Promise<User> {
  return api.post<User>(token, "/users", user);
}

export function updateUser(token: string, user: User): Promise<User> {
  return api.put<User>(token, `/users/${user.id}`, user);
}

export function deleteUser(token: string, id: number): Promise<void> {
  return api.delete<void>(token, `/users/${id}`);
}
