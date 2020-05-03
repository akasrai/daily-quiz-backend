package com.machpay.api.quiz;

import com.machpay.api.entity.QuizAnswer;
import com.machpay.api.entity.QuizQuestion;
import com.machpay.api.quiz.dto.QuestionRequest;
import com.machpay.api.quiz.dto.QuestionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface QuizMapper {
    QuizQuestion toQuizQuestion(QuestionRequest questionRequest);

    QuestionResponse toQuestionResponse(QuizQuestion quizQuestion);

    QuizAnswer toQuizAnswer(QuestionRequest.Answer answer);

    List<QuizAnswer> toQuizAnswerList(List<QuestionRequest.Answer> answers);

    QuestionResponse.Answer toAnswerResponse(QuizAnswer quizAnswer);

    List<QuestionResponse.Answer> toAnswerResponseList(List<QuizAnswer> quizAnswers);
}
