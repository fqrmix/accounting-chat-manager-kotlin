import { createTheme, type Theme } from "@mui/material/styles";

export type ColorMode = "light" | "dark";

/** Builds the MUI theme for the given colour mode. */
export function buildTheme(mode: ColorMode): Theme {
  return createTheme({
    palette: {
      mode,
      primary: { main: "#2563eb" },
      secondary: { main: "#7c3aed" },
      ...(mode === "light"
        ? { background: { default: "#f4f6fb" } }
        : { background: { default: "#0f172a", paper: "#1e293b" } }),
    },
    shape: { borderRadius: 12 },
    typography: {
      fontFamily: `"Roboto", "Helvetica", "Arial", sans-serif`,
      h6: { fontWeight: 600 },
    },
    components: {
      MuiButton: { defaultProps: { disableElevation: true } },
      MuiPaper: { styleOverrides: { root: { backgroundImage: "none" } } },
    },
  });
}
