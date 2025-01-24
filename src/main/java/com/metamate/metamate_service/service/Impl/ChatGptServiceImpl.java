package com.metamate.metamate_service.service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metamate.metamate_service.dto.Ai_Quiz_Meta;
import com.metamate.metamate_service.dto.Ai_Quiz_Question;
import com.metamate.metamate_service.dto.ChatRequest;
import com.metamate.metamate_service.service.ChatGptService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.openai.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatGptServiceImpl implements ChatGptService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Retryable(maxAttempts = 3)
    public List<Ai_Quiz_Question> getChatGptResponse(Ai_Quiz_Meta aiQuizMeta) throws JsonProcessingException {
        logger.debug("ChatGptServiceImpl :: getChatGptResponse :: aiQuizMeta ={}",aiQuizMeta);

        // OpenAI API 호출 URL
        String url = baseUrl + "/chat/completions";
        logger.debug("url = {}",url);
        logger.debug("apiKey = {}",apiKey);
        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        String systemInstructionsTemplate = """
                   # Assistant Instructions: **High-Quality Quiz Generation**
                                                    
                                                    ---
                                                    
                                                    ## **1. 입력 처리**  

                                                    
                                                    ### 핵심 개념 추출
                                                    - 입력된 데이터를 바탕으로 핵심 개념을 추출합니다.
                                                    - 추출된 개념은 퀴즈 설계 목표와 일치하도록 정리합니다.
                                                    
                                                    ---
                                                    
                                                    ## **2. 출력 형식**
                                                    * 생성된 퀴즈는 다음 요소를 포함해야 합니다:
                                                    1. **문제 수**: 10문항
                                                    2. **문제 형식**: 각 문제는 4지선다형 형식으로 작성됩니다.
                                                    3. **해설 포함**: 각 정답과 오답에 대한 상세 해설이 포함되어야 합니다.
                                                    4. **명확성과 간결성**: 문제, 정답, 오답, 해설 모두 명확하고 간결하게 작성합니다.
                                                    
                                                    * 출력 결과 예시
                                                    ## 1. [Question Text]
                                                       - a) [Option 1]
                                                       - b) [Option 2]
                                                       - c) [Option 3] (Correct Answer)
                                                       - d) [Option 4]
                                                    
                                                    ### 해설
                                                       - a) [Explanation]
                                                       - b) [Explanation]
                                                       - c) [Explanation]
                                                       - d) [Explanation]
                                                    
                                                    ---
                                                    
                                                    ## **3. 퀴즈 설계 원칙**
                                                    
                                                    ### 학습 목표와의 연관성
                                                    - 모든 문제는 입력된 학습 목표와 밀접하게 연관되어야 합니다.
                                                    - 학생들이 **핵심 개념**을 이해했는지 평가하는 데 중점을 둡니다.
                                                    
                                                    ### 문제 유형의 다양성
                                                    - 모든 문제는 **4지선다형 형식**을 사용해야 합니다.
                                                    - 주제에 충실하면서 질문 표현을 다양화합니다.
                                                    
                                                    ### 실생활 연관성
                                                    - 문제를 **실생활 사례**나 응용과 연결하여 학생들의 흥미를 유도합니다.
                                                    
                                                    ### Bloom의 분류법 기반 설계
                                                    문제를 **Bloom의 인지적 영역**에 따라 단계별로 설계합니다:
                                                    1. **기억하기 (Remember)**: 기본적인 정보 회상. \s
                                                       *예: "태양계에서 가장 큰 행성은 무엇인가요?"*
                                                    2. **이해하기 (Understand)**: 정보를 요약하거나 해석. \s
                                                       *예: "목성이 가장 큰 행성인 이유는 무엇인가요?"*
                                                    3. **적용하기 (Apply)**: 배운 지식을 새로운 상황에 적용. \s
                                                       *예: "목성의 중력장을 통과하려면 우주선 설계에서 무엇을 고려해야 할까요?"*
                                                    4. **분석하기 (Analyze)**: 관계를 분석하고 구조를 이해. \s
                                                       *예: "대기의 구성 요소가 유사한 두 행성을 고르세요."*
                                                    5. **평가하기 (Evaluate)**: 주어진 자료를 평가하고 결론 도출. \s
                                                       *예: "생명체가 존재할 가능성이 높은 행성을 선택하고 이유를 설명하세요."*
                                                    6. **창의적으로 만들기 (Create)**: 새로운 아이디어나 설계를 창출. \s
                                                       *예: "태양계를 주제로 한 교육 게임을 설계하고 학습 목표를 설명하세요."*
                                                    
                                                    ### 문제 난이도
                                                    - **하 (Easy)**: 기억과 이해 수준의 문제.
                                                    - **중 (Medium)**: 적용과 분석 수준의 문제.
                                                    - **상 (Hard)**: 평가와 창의적 설계를 요구하는 문제.
                                                    
                                                    ---
                                                    
                                                    ## **4. 선택지 및 해설**
                                                    
                                                    ### 선택지
                                                    - 각 문제는 하나의 정답과 세 개의 설득력 있는 오답을 포함해야 합니다.
                                                    - 오답은 다음 기준을 충족해야 합니다:
                                                      - 그럴듯해 보이지만 명확히 틀린 답이어야 합니다.
                                                      - 논란의 여지가 없도록 분명하게 작성합니다.
                                                      - 학생들이 흔히 **오해하거나 실수할 가능성**을 반영하되, 정답과 혼동되지 않도록 합니다.
                                                    
                                                    ### 상세 해설
                                                    - 각 선택지에 대해 의미 있는 **피드백**을 제공합니다:
                                                      - **정답**: 정답이 올바른 이유를 설명하며, 학습을 강화할 수 있는 추가 자료를 제공합니다.
                                                      - **오답**: 각 선택지에 대해서 틀린 이유를 명확히 설명하며, 학생들이 헷갈릴 수 있는 부분을 보완합니다.
                                                    
                                                    ---
                                                    
                                                    ## **5. 적응형 및 동적 설계**
                                                    - 퀴즈 내용을 다음 기준에 따라 조정합니다:
                                                      - **학년**: 학생의 연령에 적합한 언어와 예제를 사용합니다.
                                                      - **난이도 조정**:
                                                        - 초기 질문은 쉬운 문제로 시작하여 흥미를 유도합니다.
                                                        - 점진적으로 복잡성과 도전성을 높여 학습 참여를 유지합니다.
                                                    
                                                    ---
                                                    
                                                    ## **6. 기술 지침**
                                                    
                                                    ### 형식
                                                    - 퀴즈는 **일관되고 명확한 형식**으로 작성해야 합니다:
                                                      - 문제 텍스트와 선택지는 보기 편하게 나열합니다.
                                                      - 해설은 각 선택지에 대해 구체적이고 상세히 작성합니다.
                                                    - 동일 주제의 질문은 논리적으로 그룹화합니다.
                                                    
                                                    ---
                                                    
                                                    ## **7. 피드백 및 반복**
                                                    - **지속적인 개선**:
                                                      - 사용자 요구와 최신 교육 표준에 맞게 템플릿을 조정합니다.
                                                      
                                                    
                                                    ---
                                                    
                                                    ## **8. 품질 보증**
                                                    - 모든 생성된 퀴즈는 다음 기준을 충족해야 합니다:
                                                      - **문법적으로 정확**하고 언어적으로 명확합니다.
                                                      - **단순하고 익숙한 표현 사용**: 기술 용어, 속어를 피하고 쉽게 이해 가능한 단어를 사용합니다.
                                                      - **명확성과 일관성 유지**: 문제 형식과 표현이 논리적이고 일관되도록 작성합니다.
                                                      - **공정성 유지**: 문화적, 성별, 사회경제적 편향을 피합니다.
                                                      - 제공된 입력과 직접적으로 연관된 **사실 기반** 내용을 포함합니다.
                                                      
                                                       Generate a quiz in JSON format with the following structure:
                                                          {
                                                              "questions": [
                                                                  {
                                                                      "question": "Question text here",
                                                                      "options": ["Option 1", "Option 2", "Option 3", "Option 4"],
                                                                      "answer": "Correct option text",
                                                                      "explanations": {
                                                                          "Option 1": "Explanation for Option 1",
                                                                          "Option 2": "Explanation for Option 2",
                                                                          "Option 3": "Explanation for Option 3",
                                                                          "Option 4": "Explanation for Option 4"
                                                                      }
                                                                  }
                                                              ]
                                                          }
                """;

        // 사용자 요청 메시지 (사용자 입력 데이터)
        String userPrompt = String.format("""
        **수업 제목:** %s
        **수업 주제 또는 목표:** %s
        **수업 내용:** %s
        **학생 학년:** %s
        **퀴즈 난이도:** %s
        **핵심 키워드:** %s
        **퀴즈 개수:** 10개
        **답변 언어:** ko-kr
        """,
                aiQuizMeta.getTopic(),
                aiQuizMeta.getObjective(),
                aiQuizMeta.getContent(),
                aiQuizMeta.getGrade_level(),
                aiQuizMeta.getDifficulty_level(),
                aiQuizMeta.getKeyword()
        );

        logger.debug("ChatGptServiceImpl :: getChatGptResponse :: systemInstructions ={}",systemInstructionsTemplate);

        // 메시지 구성
        Map<String, Object> systemMessage = Map.of(
                "role", "system",
                "content", systemInstructionsTemplate
        );

        Map<String, Object> userMessage = Map.of(
                "role", "user",
                "content", userPrompt
        );

        // 요청 바디 설정 "gpt-3.5-turbo"
        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(systemMessage,userMessage)
        );

        // HTTP 요청
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        // 결과 파싱
        // 응답에서 choices 필드 파싱
        logger.debug("response.getBody() = {}",response.getBody());
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> firstChoice = choices.get(0); // 첫 번째 응답 선택
        logger.debug("response.firstChoice = {}",firstChoice);
        // 메시지에서 content 추출
        Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
        logger.debug("response.message = {}",message);
        logger.debug("response.message.content = {}",message.get("content"));
        String responseContent = (String) message.get("content");

        try {
            return parseQuizQuestions(responseContent);
        } catch (JsonProcessingException e) {
            logger.error("JSON 파싱 실패: {}", e.getMessage());
            throw new RuntimeException("퀴즈 응답을 파싱하는 중 오류가 발생했습니다. JSON 형식이 올바르지 않습니다.", e);
        }
    }

    @Recover
    public List<Ai_Quiz_Question> recover(RuntimeException e, Ai_Quiz_Meta aiQuizMeta) {
        logger.error("재시도 실패: {}", e.getMessage());
        return Collections.emptyList();  // 기본 응답으로 빈 리스트 반환
    }

    private List<Ai_Quiz_Question> parseQuizQuestions(String responseContent) throws JsonProcessingException{
        // JSON 시작과 끝을 찾아서 JSON 부분만 추출
        int startIndex = responseContent.indexOf("{");
        int endIndex = responseContent.lastIndexOf("}");

        if (startIndex == -1 || endIndex == -1) {
            throw new JsonProcessingException("Invalid response format. JSON not found.") {};
        }

        String jsonString = responseContent.substring(startIndex, endIndex + 1);

        logger.debug("Extracted JSON: {}", jsonString);

        // JSON 파싱
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> parsedJson = objectMapper.readValue(jsonString, Map.class);

        // "questions" 리스트 추출 및 변환
        List<Map<String, Object>> questions = (List<Map<String, Object>>) parsedJson.get("questions");
        List<Ai_Quiz_Question> quizQuestions = new ArrayList<>();

        for (Map<String, Object> questionMap : questions) {
            Ai_Quiz_Question quizQuestion = new Ai_Quiz_Question();
            quizQuestion.setQuestion((String) questionMap.get("question"));
            quizQuestion.setOptions((List<String>) questionMap.get("options"));
            quizQuestion.setAnswer((String) questionMap.get("answer"));
            quizQuestion.setExplanations((Map<String, String>) questionMap.get("explanations"));
            quizQuestions.add(quizQuestion);
        }

        return quizQuestions;
    }
}
