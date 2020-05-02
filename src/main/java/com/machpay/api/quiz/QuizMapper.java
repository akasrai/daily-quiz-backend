package com.machpay.api.quiz;

import com.machpay.api.entity.QuizAnswer;
import com.machpay.api.entity.QuizQuestion;
import com.machpay.api.quiz.dto.AnswerRequest;
import com.machpay.api.quiz.dto.AnswerResponse;
import com.machpay.api.quiz.dto.QuestionRequest;
import com.machpay.api.quiz.dto.QuestionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface QuizMapper {
    QuizQuestion toQuizQuestion(QuestionRequest questionRequest);

    QuestionResponse toQuestionResponse(QuizQuestion quizQuestion);

    QuizAnswer toQuizAnswer(AnswerRequest answerRequest);

    List<QuizAnswer> toQuizAnswerList(List<AnswerRequest> answerRequests);

    AnswerResponse toAnswerResponse(QuizAnswer quizAnswer);

    List<AnswerResponse> toAnswerResponseList(List<QuizAnswer> quizAnswers);
}
