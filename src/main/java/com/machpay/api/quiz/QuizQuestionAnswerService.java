package com.machpay.api.quiz;

import com.machpay.api.common.exception.BadRequestException;
import com.machpay.api.common.exception.ResourceNotFoundException;
import com.machpay.api.entity.QuizAnswer;
import com.machpay.api.entity.QuizQuestion;
import com.machpay.api.entity.QuizSeason;
import com.machpay.api.entity.User;
import com.machpay.api.quiz.dto.AnswerRequest;
import com.machpay.api.quiz.dto.AnswerResponse;
import com.machpay.api.quiz.dto.QuestionRequest;
import com.machpay.api.quiz.dto.QuestionResponse;
import com.machpay.api.quiz.repository.QuizAnswerRepository;
import com.machpay.api.quiz.repository.QuizPlayRepository;
import com.machpay.api.quiz.repository.QuizQuestionRepository;
import com.machpay.api.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuizQuestionAnswerService {
    @Autowired
    private QuizMapper quizMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private QuizPlayService quizPlayService;

    @Autowired
    private QuizSeasonService quizSeasonService;

    @Autowired
    private QuizPlayRepository quizPlayRepository;

    @Autowired
    private QuizAnswerRepository quizAnswerRepository;


    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    public QuizAnswer findAnswerByQuestionAndId(QuizQuestion question, Long id) {
        return quizAnswerRepository.findByQuestionAndId(question, id).orElseThrow(() -> new ResourceNotFoundException("Answer", "id", id));
    }

    public QuizAnswer findByQuestionAndCorrectTrue(QuizQuestion question) {
        return quizAnswerRepository.findByQuestionAndCorrectTrue(question).orElseThrow(() -> new ResourceNotFoundException("Answer", "question id", question.getId()));
    }

    public QuizQuestion findQuestionById(UUID id) {
        return quizQuestionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Question", "id",
                id));
    }

    @Transactional
    public void createQuestion(QuestionRequest questionRequest, Long userId) {
        validateAnswers(questionRequest.getAnswers());
        User user = userService.findById(userId);
        QuizQuestion quizQuestion = quizMapper.toQuizQuestion(questionRequest);
        quizQuestion.setUser(user);

        quizQuestionRepository.save(quizQuestion);
        createAnswers(quizQuestion, questionRequest.getAnswers());
    }

    private void validateAnswers(List<QuestionRequest.Answer> answers) {
        int correctAnswerCount = 0;

        for (QuestionRequest.Answer answer : answers) {
            if (answer.isCorrect())
                correctAnswerCount++;
        }

        if (correctAnswerCount > 1)
            throw new BadRequestException("Multiple options cannot be correct");
    }

    @Transactional
    public void createAnswers(QuizQuestion question, List<QuestionRequest.Answer> answers) {
        QuizSeason quizSeason = quizSeasonService.getActiveSeason();
        List<QuizAnswer> quizAnswers = quizMapper.toQuizAnswerList(answers);
        quizAnswers = quizAnswers.stream().map(answer -> setQuestion(question, answer)).collect(Collectors.toList());

        quizAnswerRepository.saveAll(quizAnswers);
        quizPlayRepository.unLockByQuizSeason(quizSeason);
    }

    private QuizAnswer setQuestion(QuizQuestion question, QuizAnswer answer) {
        answer.setQuestion(question);

        return answer;
    }

    public QuestionResponse getLatestQuestion() {
        QuizQuestion quizQuestion = quizQuestionRepository.findFirstByOrderByCreatedAtDesc();
        List<QuizAnswer> quizAnswers = quizAnswerRepository.findAllByQuestion(quizQuestion);
        QuestionResponse questionResponse = quizMapper.toQuestionResponse(quizQuestion);
        questionResponse.setAnswers(quizMapper.toAnswerResponseList(quizAnswers));

        return questionResponse;
    }

    @Transactional
    public AnswerResponse checkAnswer(AnswerRequest answerRequest, Long userId) {
        User user = userService.findById(userId);
        QuizQuestion quizQuestion = findQuestionById(answerRequest.getQuestion());
        QuizAnswer playerAnswer = findAnswerByQuestionAndId(quizQuestion, answerRequest.getAnswer());
        QuizAnswer correctAnswer = findByQuestionAndCorrectTrue(quizQuestion);

        if (playerAnswer.isCorrect()) {
            quizPlayService.updateQuizPlay(user, quizQuestion.getPoint(), answerRequest.getTimeTaken());

            return buildAnswerResponse(playerAnswer, correctAnswer);
        }

        quizPlayService.lockPlayerForQuiz(user);
        return buildAnswerResponse(playerAnswer, correctAnswer);
    }

    private AnswerResponse buildAnswerResponse(QuizAnswer playerAnswer, QuizAnswer correctAnswer) {
        AnswerResponse answerResponse = new AnswerResponse();
        answerResponse.setCorrect(playerAnswer.isCorrect());
        answerResponse.setCorrectAnswer(correctAnswer.getId());

        return answerResponse;
    }
}
