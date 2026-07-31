import { createGlobalStyle } from 'styled-components';

const fontImport = `@import url('https://fonts.googleapis.com/css2?family=DM+Serif+Display&family=Plus+Jakarta+Sans:wght@400;500;700&family=Noto+Sans+KR:wght@400;500;700&display=swap');`;

const GlobalStyle = createGlobalStyle`
  ${fontImport}
  
  /* 🌟 여기에 폰트를 적용해야 테마를 이기고 모든 요소에 강제 적용됩니다! */
  *,
  *::before,
  *::after {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
    font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, system-ui, Roboto, 'Helvetica Neue', 'Segoe UI', 'Apple SD Gothic Neo', 'Noto Sans KR', 'Malgun Gothic', sans-serif;
  }

  html {
    font-size: 16px;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
  }

  body {
    background-color: ${({ theme }) => theme.colors.background};
    color: ${({ theme }) => theme.colors.text};
    line-height: 1.6;
    min-height: 100vh;
  }

  #root {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
  }

  a {
    color: inherit;
    text-decoration: none;
    transition: opacity 0.2s ease;

    &:hover {
      opacity: 0.8;
    }
  }
  
  button {
    font-family: inherit;
    cursor: pointer;
    border: none;
    outline: none;
  }

  input,
  textarea {
    font-family: inherit;
    outline: none;
  }

  img {
    max-width: 100%;
    display: block;
  }

  ul, ol {
    list-style: none;
  }
`;

export default GlobalStyle;
