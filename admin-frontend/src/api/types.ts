// Mirrors the Kotlin admin API DTOs (src/main/kotlin/web/dto/Dto.kt).

export const USER_GROUPS = ["CONNECTION", "SUPPORT"] as const;
export type UserGroup = (typeof USER_GROUPS)[number];

export const USER_GROUP_LABELS: Record<UserGroup, string> = {
  CONNECTION: "Группа подключения",
  SUPPORT: "Группа сопровождения",
};

export interface User {
  id?: number;
  name: string;
  lunchTime: string;
  groupName: UserGroup;
  telegramId?: number | null;
}

export interface Schedule {
  id?: number;
  /** ISO-8601 local date-time, e.g. "2026-10-13T09:00:00". */
  startDateTime: string;
  endDateTime: string;
  userId: number;
  userName?: string;
}

export interface UploadResult {
  createdCount: number;
  deletedCount: number;
}
