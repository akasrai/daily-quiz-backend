package com.machpay.api.quiz;

import com.machpay.api.common.exception.BadRequestException;
import com.machpay.api.entity.QuizAnswer;
import com.machpay.api.entity.QuizQuestion;
import com.machpay.api.entity.User;
import com.machpay.api.quiz.dto.AnswerRequest;
import com.machpay.api.quiz.dto.QuestionRequest;
import com.machpay.api.quiz.dto.QuestionResponse;
import com.machpay.api.quiz.repository.QuizAnswerRepository;
import com.machpay.api.quiz.repository.QuizPlayRepository;
import com.machpay.api.quiz.repository.QuizQuestionRepository;
import com.machpay.api.quiz.repository.QuizResultRepository;
import com.machpay.api.user.UserService;
import com.machpay.api.user.member.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizService {
    @Autowired
    private QuizMapper quizMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private QuizPlayRepository quizPlayRepository;

    @Autowired
    private QuizAnswerRepository quizAnswerRepository;

    @Autowired
    private QuizResultRepository quizResultRepository;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    public void createQuestion(QuestionRequest questionRequest, Long userId) {
        validateAnswers(questionRequest.getAnswers());
        User user = userService.findById(userId);
        QuizQuestion quizQuestion = quizMapper.toQuizQuestion(questionRequest);
        quizQuestion.setUser(user);

        quizQuestionRepository.save(quizQuestion);
        createAnswers(quizQuestion, questionRequest.getAnswers());
    }

    private void validateAnswers(List<AnswerRequest> answerRequests) {
        int correctAnswerCount = 0;

        for (AnswerRequest answerRequest : answerRequests) {
            if (answerRequest.isCorrect())
                correctAnswerCount++;
        }

        if (correctAnswerCount > 1)
            throw new BadRequestException("Multiple options cannot be correct");
    }

    public void createAnswers(QuizQuestion question, List<AnswerRequest> answers) {
        List<QuizAnswer> quizAnswers = quizMapper.toQuizAnswerList(answers);
        quizAnswers = quizAnswers.stream().map(answer -> setQuestion(question, answer)).collect(Collectors.toList());

        quizAnswerRepository.saveAll(quizAnswers);
    }

    private QuizAnswer setQuestion(QuizQuestion question, QuizAnswer answer) {
        answer.setQuestion(question);

        return answer;
    }

    public QuestionResponse getLatestQuestion() {
        QuizQuestion quizQuestion = quizQuestionRepository.findTopByOrderByIdDesc();
        List<QuizAnswer> quizAnswers = quizAnswerRepository.findAllByQuestion(quizQuestion);
        QuestionResponse questionResponse = quizMapper.toQuestionResponse(quizQuestion);
        questionResponse.setAnswers(quizMapper.toAnswerResponseList(quizAnswers));

        return questionResponse;
    }

}
