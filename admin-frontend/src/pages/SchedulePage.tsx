import { useRef, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import IconButton from "@mui/material/IconButton";
import Tooltip from "@mui/material/Tooltip";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import DialogContentText from "@mui/material/DialogContentText";
import DialogActions from "@mui/material/DialogActions";
import Alert from "@mui/material/Alert";
import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import RefreshIcon from "@mui/icons-material/Refresh";
import UploadIcon from "@mui/icons-material/Upload";
import { DataGrid, type GridColDef } from "@mui/x-data-grid";

import { useAuth } from "../auth/AuthContext";
import { useNotification } from "../ui/NotificationContext";
import { listUsers } from "../api/users";
import { createSchedule, deleteSchedule, listSchedules, updateSchedule, uploadSchedule } from "../api/schedule";
import type { Schedule } from "../api/types";
import { ApiError } from "../api/client";
import ScheduleFormDialog, { type ScheduleFormValues } from "./ScheduleFormDialog";

function today(): string {
  return new Date().toISOString().slice(0, 10);
}

function formatTime(iso: string): string {
  return iso.split("T")[1]?.slice(0, 5) ?? "";
}

export default function SchedulePage() {
  const { token } = useAuth();
  const authToken = token!;
  const { notify } = useNotification();
  const queryClient = useQueryClient();
  const fileInput = useRef<HTMLInputElement>(null);

  const [date, setDate] = useState(today());
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState<Schedule | null>(null);
  const [deleting, setDeleting] = useState<Schedule | null>(null);
  const [uploadBusy, setUploadBusy] = useState(false);

  const usersQuery = useQuery({ queryKey: ["users"], queryFn: () => listUsers(authToken) });

  const scheduleQuery = useQuery({
    queryKey: ["schedules", date],
    queryFn: () => listSchedules(authToken, date),
  });

  function invalidateSchedules() {
    queryClient.invalidateQueries({ queryKey: ["schedules"] });
  }

  const saveMutation = useMutation({
    mutationFn: (values: ScheduleFormValues) => {
      const schedule: Schedule = {
        id: editing?.id,
        userId: values.userId,
        startDateTime: `${values.date}T${values.startTime}:00`,
        endDateTime: `${values.date}T${values.endTime}:00`,
      };
      return editing ? updateSchedule(authToken, schedule) : createSchedule(authToken, schedule);
    },
    onSuccess: () => {
      invalidateSchedules();
      setDialogOpen(false);
      notify(editing ? "Смена обновлена" : "Смена добавлена", "success");
    },
    onError: (err) => {
      if (err instanceof ApiError) notify(err.message, "error");
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (schedule: Schedule) => deleteSchedule(authToken, schedule.id!),
    onSuccess: () => {
      invalidateSchedules();
      setDeleting(null);
      notify("Смена удалена", "success");
    },
    onError: (err) => {
      if (err instanceof ApiError) notify(err.message, "error");
    },
  });

  async function handleUpload(file: File) {
    setUploadBusy(true);
    try {
      const result = await uploadSchedule(authToken, file);
      invalidateSchedules();
      notify(`Загружено: ${result.createdCount}, заменено: ${result.deletedCount}`, "success");
    } catch (err) {
      if (err instanceof ApiError) notify(err.message, "error");
    } finally {
      setUploadBusy(false);
      if (fileInput.current) fileInput.current.value = "";
    }
  }

  const columns: GridColDef<Schedule>[] = [
    { field: "userName", headerName: "Сотрудник", flex: 1, minWidth: 180 },
    {
      field: "startDateTime",
      headerName: "Начало",
      width: 110,
      valueGetter: (_, row) => formatTime(row.startDateTime),
    },
    {
      field: "endDateTime",
      headerName: "Окончание",
      width: 110,
      valueGetter: (_, row) => formatTime(row.endDateTime),
    },
    {
      field: "actions",
      headerName: "",
      width: 100,
      sortable: false,
      filterable: false,
      renderCell: (params) => (
        <Stack direction="row" spacing={0.5}>
          <Tooltip title="Редактировать">
            <IconButton
              size="small"
              onClick={() => {
                setEditing(params.row);
                setDialogOpen(true);
              }}
            >
              <EditIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title="Удалить">
            <IconButton size="small" onClick={() => setDeleting(params.row)}>
              <DeleteIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </Stack>
      ),
    },
  ];

  return (
    <Box>
      <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ mb: 2 }}>
        <Typography variant="h5">Расписание</Typography>
        <Stack direction="row" spacing={2} alignItems="center">
          <TextField
            label="Дата"
            type="date"
            size="small"
            value={date}
            onChange={(e) => setDate(e.target.value)}
            InputLabelProps={{ shrink: true }}
          />
          <Button startIcon={<RefreshIcon />} onClick={() => scheduleQuery.refetch()}>
            Обновить
          </Button>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            disabled={!usersQuery.data?.length}
            onClick={() => {
              setEditing(null);
              setDialogOpen(true);
            }}
          >
            Добавить смену
          </Button>
        </Stack>
      </Stack>

      <Card sx={{ mb: 2, maxWidth: 480 }}>
        <CardContent>
          <Typography variant="subtitle1" gutterBottom>
            Загрузка графика из Excel
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Записи на даты, встречающиеся в файле, будут заменены новыми.
          </Typography>
          <Button startIcon={<UploadIcon />} onClick={() => fileInput.current?.click()} disabled={uploadBusy}>
            {uploadBusy ? "Загрузка…" : "Выбрать файл"}
          </Button>
          <input
            ref={fileInput}
            type="file"
            accept=".xlsx,.xls"
            hidden
            onChange={(e) => {
              const file = e.target.files?.[0];
              if (file) void handleUpload(file);
            }}
          />
        </CardContent>
      </Card>

      {scheduleQuery.isError && !(scheduleQuery.error instanceof ApiError && scheduleQuery.error.status === 401) && (
        <Alert severity="error" sx={{ mb: 2 }}>
          Не удалось загрузить расписание
        </Alert>
      )}

      <Paper sx={{ height: 560, width: "100%" }}>
        <DataGrid
          rows={scheduleQuery.data ?? []}
          columns={columns}
          getRowId={(row) => row.id!}
          loading={scheduleQuery.isLoading}
          disableRowSelectionOnClick
          initialState={{ pagination: { paginationModel: { pageSize: 25 } } }}
          pageSizeOptions={[10, 25, 50, 100]}
        />
      </Paper>

      <ScheduleFormDialog
        open={dialogOpen}
        editing={editing}
        users={usersQuery.data ?? []}
        defaultDate={date}
        busy={saveMutation.isPending}
        onClose={() => setDialogOpen(false)}
        onSubmit={(values) => saveMutation.mutate(values)}
      />

      <Dialog open={deleting !== null} onClose={() => setDeleting(null)}>
        <DialogTitle>Удалить смену?</DialogTitle>
        <DialogContent>
          <DialogContentText>Это действие необратимо.</DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleting(null)} disabled={deleteMutation.isPending}>
            Отмена
          </Button>
          <Button
            color="error"
            variant="contained"
            onClick={() => deleteMutation.mutate(deleting!)}
            disabled={deleteMutation.isPending}
          >
            {deleteMutation.isPending ? "Удаление…" : "Удалить"}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
