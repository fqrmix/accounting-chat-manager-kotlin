import { useEffect, useState } from "react";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import DialogActions from "@mui/material/DialogActions";
import Button from "@mui/material/Button";
import TextField from "@mui/material/TextField";
import MenuItem from "@mui/material/MenuItem";
import Stack from "@mui/material/Stack";

import { USER_GROUPS, USER_GROUP_LABELS, type User } from "../api/types";

export interface UserFormValues extends User {}

interface Props {
  open: boolean;
  /** The user being edited, or null when creating a new one. */
  editing: User | null;
  busy: boolean;
  onClose: () => void;
  onSubmit: (values: UserFormValues) => void;
}

const TIME_RE = /^([01]\d|2[0-3]):[0-5]\d$/;

const emptyValues: UserFormValues = {
  name: "",
  lunchTime: "13:00",
  groupName: "CONNECTION",
  telegramId: null,
};

export default function UserFormDialog({ open, editing, busy, onClose, onSubmit }: Props) {
  const [values, setValues] = useState<UserFormValues>(emptyValues);

  useEffect(() => {
    setValues(editing ?? emptyValues);
  }, [editing, open]);

  const isEdit = editing !== null;

  const timeInvalid = !TIME_RE.test(values.lunchTime);
  const canSubmit = values.name.trim() !== "" && !timeInvalid;

  function update<K extends keyof UserFormValues>(key: K, value: UserFormValues[K]) {
    setValues((prev) => ({ ...prev, [key]: value }));
  }

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{isEdit ? `Редактирование: ${editing?.name}` : "Новый сотрудник"}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField
            label="Имя"
            value={values.name}
            onChange={(e) => update("name", e.target.value)}
            helperText="Например: Иванов Иван"
            fullWidth
          />
          <TextField
            label="Время обеда (ЧЧ:ММ)"
            value={values.lunchTime}
            onChange={(e) => update("lunchTime", e.target.value)}
            error={timeInvalid}
            helperText={timeInvalid ? "Формат ЧЧ:ММ, например 13:00" : undefined}
            fullWidth
          />
          <TextField
            select
            label="Группа"
            value={values.groupName}
            onChange={(e) => update("groupName", e.target.value as User["groupName"])}
            fullWidth
          >
            {USER_GROUPS.map((group) => (
              <MenuItem key={group} value={group}>
                {USER_GROUP_LABELS[group]}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            label="Telegram ID"
            type="number"
            value={values.telegramId ?? ""}
            onChange={(e) => update("telegramId", e.target.value === "" ? null : Number(e.target.value))}
            helperText="Необязательно"
            fullWidth
          />
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
