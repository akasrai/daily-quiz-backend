package com.machpay.api.quiz;

import com.machpay.api.common.exception.BadRequestException;
import com.machpay.api.common.exception.ResourceNotFoundException;
import com.machpay.api.entity.Member;
import com.machpay.api.entity.QuizAnswer;
import com.machpay.api.entity.QuizPlay;
import com.machpay.api.entity.QuizQuestion;
import com.machpay.api.entity.QuizSeason;
import com.machpay.api.entity.User;
import com.machpay.api.quiz.dto.AnswerRequest;
import com.machpay.api.quiz.dto.AnswerResponse;
import com.machpay.api.quiz.dto.PlayerCurrentStatusResponse;
import com.machpay.api.quiz.dto.QuestionRequest;
import com.machpay.api.quiz.dto.QuestionResponse;
import com.machpay.api.quiz.dto.QuizPlayResponse;
import com.machpay.api.quiz.repository.QuizAnswerRepository;
import com.machpay.api.quiz.repository.QuizPlayRepository;
import com.machpay.api.quiz.repository.QuizQuestionRepository;
import com.machpay.api.quiz.repository.QuizResultRepository;
import com.machpay.api.quiz.repository.QuizSeasonRepository;
import com.machpay.api.security.UserPrincipal;
import com.machpay.api.user.UserService;
import com.machpay.api.user.member.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    private QuizSeasonRepository quizSeasonRepository;

    @Autowired
    private QuizAnswerRepository quizAnswerRepository;

    @Autowired
    private QuizResultRepository quizResultRepository;

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
        QuizSeason quizSeason = getActiveSeason();
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
            updateQuizPlay(user, quizQuestion.getPoint(), answerRequest.getTimeTaken());

            return buildAnswerResponse(playerAnswer, correctAnswer);
        }

        lockPlayerForQuiz(user);
        return buildAnswerResponse(playerAnswer, correctAnswer);
    }

    private AnswerResponse buildAnswerResponse(QuizAnswer playerAnswer, QuizAnswer correctAnswer) {
        AnswerResponse answerResponse = new AnswerResponse();
        answerResponse.setCorrect(playerAnswer.isCorrect());
        answerResponse.setCorrectAnswer(correctAnswer.getId());

        return answerResponse;
    }

    public QuizSeason getActiveSeason() {
        return quizSeasonRepository.findFirstByActiveTrue().orElseThrow(() -> new ResourceNotFoundException("Quiz " +
                "Season", "active", true));
    }

    @Transactional
    public void lockPlayerForQuiz(User user) {
        QuizSeason quizSeason = getActiveSeason();
        Optional<QuizPlay> existingPoints = quizPlayRepository.findByUserAndSeason(user, quizSeason);

        if (existingPoints.isPresent()) {
            QuizPlay existingQuizPlay = existingPoints.get();
            existingQuizPlay.setLocked(true);

            quizPlayRepository.save(existingQuizPlay);
        }
    }

    @Transactional
    public QuizPlay updateQuizPlay(User user, Long point, Long timeTaken) {
        QuizSeason quizSeason = getActiveSeason();
        Optional<QuizPlay> existingPoints = quizPlayRepository.findByUserAndSeason(user, quizSeason);

        if (existingPoints.isPresent()) {
            QuizPlay existingQuizPlay = existingPoints.get();
            existingQuizPlay.setLocked(true);
            existingQuizPlay.setPoint(existingQuizPlay.getPoint() + point);
            existingQuizPlay.setGamePlayed(existingQuizPlay.getGamePlayed() + 1);
            existingQuizPlay.setTimeTaken(existingQuizPlay.getTimeTaken() + timeTaken);

            return quizPlayRepository.save(existingQuizPlay);
        }

        QuizPlay quizPlay = new QuizPlay();
        quizPlay.setUser(user);
        quizPlay.setPoint(point);
        quizPlay.setLocked(true);
        quizPlay.setSeason(quizSeason);
        quizPlay.setTimeTaken(timeTaken);
        quizPlay.setGamePlayed(Long.valueOf(1));

        return quizPlayRepository.save(quizPlay);
    }

    public List<QuizPlayResponse> getLeaderBoard() {
        List<QuizPlay> quizPlays = quizPlayRepository.findAllByOrderByPointDescTimeTakenAsc();

        return getQuizPlayResponse(quizPlays);
    }

    private List<QuizPlayResponse> getQuizPlayResponse(List<QuizPlay> quizPlays) {
        return quizPlays.stream().map(quizPlay -> {
            QuizPlayResponse quizPlayResponse = new QuizPlayResponse();
            quizPlayResponse.setPoint(quizPlay.getPoint());
            quizPlayResponse.setGamePlayed(quizPlay.getGamePlayed());
            quizPlayResponse.setPlayer(getPlayer(quizPlay.getUser()));

            return quizPlayResponse;
        }).collect(Collectors.toList());
    }

    private QuizPlayResponse.Player getPlayer(User user) {
        QuizPlayResponse.Player player = new QuizPlayResponse.Player();
        Member member = memberService.findById(user.getId());
        player.setName(member.getFullName());
        player.setPhoto(member.getPhoto());

        return player;
    }

    public PlayerCurrentStatusResponse getPlayerCurrentStatus(UserPrincipal userPrincipal) {
        Member member = memberService.findByEmail(userPrincipal.getEmail());
        List<QuizPlay> quizPlays = quizPlayRepository.findAllByOrderByPointDescTimeTakenAsc();

        return calculateGamePosition(member, quizPlays);
    }

    private PlayerCurrentStatusResponse calculateGamePosition(Member member, List<QuizPlay> quizPlays) {
        int position = 0;
        PlayerCurrentStatusResponse playerCurrentStatusResponse = new PlayerCurrentStatusResponse();

        for (QuizPlay quizPlay : quizPlays) {
            position++;

            if (member.equals(quizPlay.getUser())) {
                playerCurrentStatusResponse.setPosition(position);
                playerCurrentStatusResponse.setPoint(quizPlay.getPoint());
                playerCurrentStatusResponse.setGamePlayed(quizPlay.getGamePlayed());

                break;
            }
        }

        return playerCurrentStatusResponse;
    }

    public boolean isEligible(UserPrincipal userPrincipal) {
        QuizSeason quizSeason = getActiveSeason();
        Member member = memberService.findByEmail(userPrincipal.getEmail());
        Optional<QuizPlay> quizPlay = quizPlayRepository.findByUserAndSeason(member, quizSeason);

        if (quizPlay.isPresent()) {
            return !quizPlay.get().isLocked();
        }

        return true;
    }
}
