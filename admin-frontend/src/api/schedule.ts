import { api } from "./client";
import type { Schedule, UploadResult } from "./types";

export function listSchedules(token: string, date?: string): Promise<Schedule[]> {
  return api.get<Schedule[]>(token, date ? `/schedules?date=${date}` : "/schedules");
}

export function createSchedule(token: string, schedule: Schedule): Promise<Schedule> {
  return api.post<Schedule>(token, "/schedules", schedule);
}

export function updateSchedule(token: string, schedule: Schedule): Promise<Schedule> {
  return api.put<Schedule>(token, `/schedules/${schedule.id}`, schedule);
}

export function deleteSchedule(token: string, id: number): Promise<void> {
  return api.delete<void>(token, `/schedules/${id}`);
}

/** Uploads a schedule Excel file; the server replaces overlapping dates and keeps the rest. */
export function uploadSchedule(token: string, file: File): Promise<UploadResult> {
  const form = new FormData();
  form.append("file", file);
  return api.post<UploadResult>(token, "/schedules/upload", form);
}
