import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import Chip from "@mui/material/Chip";
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
import { DataGrid, type GridColDef } from "@mui/x-data-grid";

import { useAuth } from "../auth/AuthContext";
import { useNotification } from "../ui/NotificationContext";
import { createUser, deleteUser, listUsers, updateUser } from "../api/users";
import { USER_GROUP_LABELS, type User, type UserGroup } from "../api/types";
import { ApiError } from "../api/client";
import UserFormDialog, { type UserFormValues } from "./UserFormDialog";

export default function UsersPage() {
  const { token } = useAuth();
  const authToken = token!;
  const { notify } = useNotification();
  const queryClient = useQueryClient();

  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState<User | null>(null);
  const [deleting, setDeleting] = useState<User | null>(null);

  const usersQuery = useQuery({
    queryKey: ["users"],
    queryFn: () => listUsers(authToken),
  });

  const saveMutation = useMutation({
    mutationFn: (values: UserFormValues) =>
      editing ? updateUser(authToken, { ...values, id: editing.id }) : createUser(authToken, values),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["users"] });
      setDialogOpen(false);
      notify(editing ? "Сотрудник обновлён" : "Сотрудник создан", "success");
    },
    onError: (err) => {
      if (err instanceof ApiError) notify(err.message, "error");
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (user: User) => deleteUser(authToken, user.id!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["users"] });
      setDeleting(null);
      notify("Сотрудник удалён", "success");
    },
    onError: (err) => {
      if (err instanceof ApiError) notify(err.message, "error");
    },
  });

  const columns: GridColDef<User>[] = [
    { field: "name", headerName: "Имя", flex: 1, minWidth: 180 },
    { field: "lunchTime", headerName: "Обед", width: 100 },
    {
      field: "groupName",
      headerName: "Группа",
      width: 220,
      renderCell: (params) => <Chip size="small" label={USER_GROUP_LABELS[params.value as UserGroup]} />,
    },
    { field: "telegramId", headerName: "Telegram ID", width: 140 },
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
        <Typography variant="h5">Сотрудники</Typography>
        <Stack direction="row" spacing={1}>
          <Button startIcon={<RefreshIcon />} onClick={() => usersQuery.refetch()}>
            Обновить
          </Button>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => {
              setEditing(null);
              setDialogOpen(true);
            }}
          >
            Добавить
          </Button>
        </Stack>
      </Stack>

      {usersQuery.isError && !(usersQuery.error instanceof ApiError && usersQuery.error.status === 401) && (
        <Alert severity="error" sx={{ mb: 2 }}>
          Не удалось загрузить список сотрудников
        </Alert>
      )}

      <Paper sx={{ height: 640, width: "100%" }}>
        <DataGrid
          rows={usersQuery.data ?? []}
          columns={columns}
          getRowId={(row) => row.id!}
          loading={usersQuery.isLoading}
          disableRowSelectionOnClick
          initialState={{ pagination: { paginationModel: { pageSize: 25 } } }}
          pageSizeOptions={[10, 25, 50, 100]}
        />
      </Paper>

      <UserFormDialog
        open={dialogOpen}
        editing={editing}
        busy={saveMutation.isPending}
        onClose={() => setDialogOpen(false)}
        onSubmit={(values) => saveMutation.mutate(values)}
      />

      <Dialog open={deleting !== null} onClose={() => setDeleting(null)}>
        <DialogTitle>Удалить сотрудника?</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Сотрудник «{deleting?.name}» и все его записи расписания будут удалены безвозвратно.
          </DialogContentText>
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
