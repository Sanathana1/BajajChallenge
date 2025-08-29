package com.example.bajajChallenge.runner;

import com.example.bajajChallenge.service.ChallengeService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements ApplicationRunner {

    private final ChallengeService challengeService;

    public StartupRunner(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        challengeService.executeChallenge();
    }
}