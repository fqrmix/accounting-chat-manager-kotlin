import { useEffect, useState } from "react";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import DialogActions from "@mui/material/DialogActions";
import Button from "@mui/material/Button";
import TextField from "@mui/material/TextField";
import MenuItem from "@mui/material/MenuItem";
import Stack from "@mui/material/Stack";

import type { Schedule, User } from "../api/types";

export interface ScheduleFormValues {
  userId: number;
  date: string;
  startTime: string;
  endTime: string;
}

interface Props {
  open: boolean;
  /** The entry being edited, or null when creating a new one. */
  editing: Schedule | null;
  users: User[];
  defaultDate: string;
  busy: boolean;
  onClose: () => void;
  onSubmit: (values: ScheduleFormValues) => void;
}

function splitIsoDateTime(iso: string): { date: string; time: string } {
  const [date, time] = iso.split("T");
  return { date, time: time.slice(0, 5) };
}

function emptyValues(userId: number, date: string): ScheduleFormValues {
  return { userId, date, startTime: "09:00", endTime: "18:00" };
}

export default function ScheduleFormDialog({ open, editing, users, defaultDate, busy, onClose, onSubmit }: Props) {
  const [values, setValues] = useState<ScheduleFormValues>(emptyValues(users[0]?.id ?? 0, defaultDate));

  useEffect(() => {
    if (editing) {
      const start = splitIsoDateTime(editing.startDateTime);
      const end = splitIsoDateTime(editing.endDateTime);
      setValues({ userId: editing.userId, date: start.date, startTime: start.time, endTime: end.time });
    } else {
      setValues(emptyValues(users[0]?.id ?? 0, defaultDate));
    }
  }, [editing, open, defaultDate, users]);

  const isEdit = editing !== null;
  const canSubmit = values.userId > 0 && values.date !== "" && values.startTime < values.endTime;

  function update<K extends keyof ScheduleFormValues>(key: K, value: ScheduleFormValues[K]) {
    setValues((prev) => ({ ...prev, [key]: value }));
  }

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{isEdit ? "Редактирование смены" : "Новая смена"}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField
            select
            label="Сотрудник"
            value={values.userId || ""}
            onChange={(e) => update("userId", Number(e.target.value))}
            fullWidth
          >
            {users.map((user) => (
              <MenuItem key={user.id} value={user.id}>
                {user.name}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            label="Дата"
            type="date"
            value={values.date}
            onChange={(e) => update("date", e.target.value)}
            InputLabelProps={{ shrink: true }}
            fullWidth
          />
          <Stack direction="row" spacing={2}>
            <TextField
              label="Начало"
              type="time"
              value={values.startTime}
              onChange={(e) => update("startTime", e.target.value)}
              InputLabelProps={{ shrink: true }}
              fullWidth
            />
            <TextField
              label="Окончание"
              type="time"
              value={values.endTime}
              onChange={(e) => update("endTime", e.target.value)}
              error={values.startTime >= values.endTime}
              helperText={values.startTime >= values.endTime ? "Должно быть позже начала" : undefined}
              InputLabelProps={{ shrink: true }}
              fullWidth
            />
          </Stack>
        </Stack>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={busy}>
          Отмена
        </Button>
        <Button variant="contained" onClick={() => onSubmit(values)} disabled={busy || !canSubmit}>
          {busy ? "Сохранение…" : isEdit ? "Сохранить" : "Создать"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
