import { ThemeProvider } from 'styled-components';
import { BrowserRouter } from 'react-router-dom';
import GlobalStyle from './styles/GlobalStyle';
import theme from './styles/theme';
import AppRoutes from './routes/AppRoutes';

const App = () => (
  <ThemeProvider theme={theme}>
    <GlobalStyle />
    <BrowserRouter>
      <AppRoutes />
    </BrowserRouter>
  </ThemeProvider>
);

export default App;
