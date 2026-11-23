import { useState } from 'react';
import styled, { keyframes } from 'styled-components';
import { FaRegCommentDots } from 'react-icons/fa';
import { askRag } from '../../api/ragApi';

const MAX_LENGTH = 500;
const EXAMPLES = ['언제까지 해?', '입장료 있어?', '주차할 곳 있어?'];

const RagAskBox = ({ contentId, festivalTitle }) => {
  const [question, setQuestion] = useState('');
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const ask = async (text) => {
    const q = text.trim();
    if (!q || loading) return;

    setLoading(true);
    setError('');
    setResult(null);
    try {
      const { data } = await askRag({ question: q, contentId });
      setResult({ question: q, ...data });
    } catch (err) {
      setError(
        err.response?.status === 503
          ? 'AI 서버가 바빠요. 잠시 후 다시 물어봐 주세요.'
          : '질문을 보내지 못했어요. 다시 시도해 주세요.',
      );
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    ask(question);
  };

  return (
    <Box>
      <HeaderSection>
        <IconBadge>
          <FaRegCommentDots size={14} />
        </IconBadge>
        <HeaderText>
          <h4>축제 정보에 질문하기</h4>
          <p>{festivalTitle ? `${festivalTitle} 정보와 FAQ에서 찾아 답해요` : '축제 정보와 FAQ에서 찾아 답해요'}</p>
        </HeaderText>
      </HeaderSection>

      <Body>
        <Form onSubmit={handleSubmit}>
          <Input
            value={question}
            onChange={(e) => setQuestion(e.target.value.slice(0, MAX_LENGTH))}
            placeholder="예: 주차할 곳 있어?"
            aria-label="축제 정보 질문"
            disabled={loading}
          />
          <AskButton type="submit" disabled={loading || !question.trim()}>
            {loading ? '찾는 중' : '질문'}
          </AskButton>
        </Form>

        <Examples>
          {EXAMPLES.map((ex) => (
            <ExampleChip
              key={ex}
              type="button"
              disabled={loading}
              onClick={() => {
                setQuestion(ex);
                ask(ex);
              }}
            >
              {ex}
            </ExampleChip>
          ))}
        </Examples>

        {loading && (
          <LoadingRow>
            <Spinner />
            <span>축제 정보에서 답을 찾고 있어요...</span>
          </LoadingRow>
        )}

        {error && <ErrorText>{error}</ErrorText>}

        {result && (
          <Answer>
            <AskedQuestion>Q. {result.question}</AskedQuestion>
            {result.found ? (
              <>
                <AnswerText>{result.answer}</AnswerText>
                <SourcesTitle>참고한 정보</SourcesTitle>
                <SourceList>
                  {result.sources.map((s) => (
                    <SourceItem key={s.number}>
                      <SourceHead>
                        <SourceNumber>[{s.number}]</SourceNumber>
                        <SourceName>{s.sourceType === 'faq' ? `FAQ · ${s.title}` : s.title}</SourceName>
                      </SourceHead>
                      <SourceSnippet>{s.snippet}</SourceSnippet>
                    </SourceItem>
                  ))}
                </SourceList>
              </>
            ) : (
              <NoInfo>
                등록된 정보에서는 답을 찾지 못했어요. 주최 측 공식 안내를 확인해 주세요.
              </NoInfo>
            )}
          </Answer>
        )}
      </Body>
    </Box>
  );
};

export default RagAskBox;

const Box = styled.section`
  background: #ffffff;
  border-radius: ${({ theme }) => theme.borderRadius.lg};
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  border: 1px solid #f4ebe1;
`;

const HeaderSection = styled.div`
  background-color: #33231a;
  padding: 18px 24px;
  display: flex;
  align-items: center;
  gap: 14px;
`;

const IconBadge = styled.div`
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #dfa857;
  flex-shrink: 0;
`;

const HeaderText = styled.div`
  h4 {
    font-size: 16px;
    font-weight: 600;
    color: #ffffff;
    margin: 0;
  }
  p {
    font-size: 11px;
    color: rgba(255, 255, 255, 0.5);
    margin: 2px 0 0 0;
  }
`;

const Body = styled.div`
  padding: 20px 24px 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
`;

const Form = styled.form`
  display: flex;
  gap: 8px;
`;

const Input = styled.input`
  flex: 1;
  min-width: 0;
  padding: 10px 14px;
  border: 1px solid ${({ theme }) => theme.colors.border};
  border-radius: ${({ theme }) => theme.borderRadius.md};
  font-family: inherit;
  font-size: 0.9rem;

  &:focus {
    outline: none;
    border-color: #c05c36;
  }
`;

const AskButton = styled.button`
  padding: 10px 16px;
  border: none;
  border-radius: ${({ theme }) => theme.borderRadius.md};
  background-color: #c05c36;
  color: #ffffff;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  white-space: nowrap;

  &:hover:not(:disabled) {
    background-color: #a04825;
  }
  &:disabled {
    opacity: 0.5;
    cursor: default;
  }
`;

const Examples = styled.div`
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
`;

const ExampleChip = styled.button`
  padding: 4px 10px;
  border: 1px solid #f4ebe1;
  border-radius: ${({ theme }) => theme.borderRadius.pill};
  background: ${({ theme }) => theme.colors.background};
  color: #8c7e74;
  font-size: 0.75rem;
  cursor: pointer;

  &:hover:not(:disabled) {
    border-color: #c05c36;
    color: #c05c36;
  }
`;

const spin = keyframes`
  to { transform: rotate(360deg); }
`;

const LoadingRow = styled.div`
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.85rem;
  color: #8c7e74;
`;

const Spinner = styled.div`
  width: 18px;
  height: 18px;
  border: 3px solid #f4ebe1;
  border-top-color: #c05c36;
  border-radius: 50%;
  animation: ${spin} 1s linear infinite;
`;

const ErrorText = styled.p`
  margin: 0;
  font-size: 0.85rem;
  color: ${({ theme }) => theme.colors.dangerDark};
`;

const Answer = styled.div`
  border-top: 1px solid #f4ebe1;
  padding-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
`;

const AskedQuestion = styled.p`
  margin: 0;
  font-size: 0.8rem;
  color: #8c7e74;
`;

const AnswerText = styled.p`
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.6;
  color: #1e3a2f;
  white-space: pre-line;
`;

const NoInfo = styled.p`
  margin: 0;
  padding: 12px 14px;
  border-radius: ${({ theme }) => theme.borderRadius.md};
  background: ${({ theme }) => theme.colors.background};
  font-size: 0.85rem;
  line-height: 1.5;
  color: #8c7e74;
`;

const SourcesTitle = styled.p`
  margin: 4px 0 0 0;
  font-size: 0.75rem;
  font-weight: 600;
  color: #8c7e74;
`;

const SourceList = styled.ul`
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin: 0;
  padding: 0;
  list-style: none;
`;

const SourceItem = styled.li`
  padding: 10px 12px;
  border-radius: ${({ theme }) => theme.borderRadius.md};
  background: ${({ theme }) => theme.colors.background};
`;

const SourceHead = styled.div`
  display: flex;
  gap: 6px;
  font-size: 0.8rem;
  font-weight: 600;
  color: #33231a;
`;

const SourceNumber = styled.span`
  color: #c05c36;
`;

const SourceName = styled.span`
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
`;

const SourceSnippet = styled.p`
  margin: 4px 0 0 0;
  font-size: 0.75rem;
  line-height: 1.5;
  color: #8c7e74;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
`;
