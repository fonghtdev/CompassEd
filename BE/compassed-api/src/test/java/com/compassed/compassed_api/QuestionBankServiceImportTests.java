package com.compassed.compassed_api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.compassed.compassed_api.domain.QuestionBank;
import com.compassed.compassed_api.domain.entity.Subject;
import com.compassed.compassed_api.repository.QuestionBankRepository;
import com.compassed.compassed_api.repository.SubjectRepository;
import com.compassed.compassed_api.service.impl.QuestionBankServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

class QuestionBankServiceImportTests {

    @Test
    void shouldNormalizePlainMathExponentToLatexWhenImportingLegacyJson() {
        QuestionBankRepository questionBankRepository = Mockito.mock(QuestionBankRepository.class);
        SubjectRepository subjectRepository = Mockito.mock(SubjectRepository.class);
        QuestionBankServiceImpl service = new QuestionBankServiceImpl(
                questionBankRepository,
                subjectRepository,
                new ObjectMapper());

        Subject math = new Subject();
        math.setId(1L);
        math.setCode("MATH");
        math.setName("Mathematics");

        when(subjectRepository.findByCode("M")).thenReturn(Optional.empty());
        when(subjectRepository.findByCode("MATH")).thenReturn(Optional.of(math));
        when(questionBankRepository.save(any(QuestionBank.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String rawJson = """
                [
                  {
                    "ID":"Math_M1_1",
                    "Subject_Code":"M",
                    "Level":1,
                    "Skill_Tag":"Ham so",
                    "Question_Text":"Cho ham so y=-x^3+3x. Tim dao ham.",
                    "Option_A":"y'=-3x^2+3",
                    "Option_B":"y'=-x^3",
                    "Option_C":"y'=3x",
                    "Option_D":"y'=0",
                    "Correct":"A",
                    "Class":11,
                    "Classify":""
                  }
                ]
                """;

        service.importQuestionsFromLegacyJson(rawJson);

        ArgumentCaptor<QuestionBank> captor = ArgumentCaptor.forClass(QuestionBank.class);
        verify(questionBankRepository).save(captor.capture());
        QuestionBank saved = captor.getValue();

        assertEquals("Cho ham so y=-x^{3}+3x. Tim dao ham.", saved.getQuestionText());
        assertEquals("[\"A. y'=-3x^{2}+3\",\"B. y'=-x^{3}\",\"C. y'=3x\",\"D. y'=0\"]", saved.getOptions());
    }
}
