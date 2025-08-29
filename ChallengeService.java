package com.example.bajajChallenge.service;

import com.example.bajajChallenge.model.InitialRequest;
import com.example.bajajChallenge.model.SolutionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ChallengeService {

    private static final Logger logger = LoggerFactory.getLogger(ChallengeService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    public void executeChallenge() {
        logger.info("Starting the Bajaj Finserv Health Challenge process...");

        InitialRequest personalDetails = new InitialRequest(
                "Pvm Sanathan",           
                "1319",                    
                "sanathan.pvm2022@vitstudent.ac.in"    
        );

        try {
            Map<String, String> webhookData = generateWebhook(personalDetails);
            String submissionUrl = webhookData.get("submissionUrl");
            String accessToken = webhookData.get("accessToken");

            String finalQuery = prepareFinalQuery(personalDetails.getRegNo());
            logger.info("Final SQL Query prepared: {}", finalQuery);

            submitSolution(submissionUrl, accessToken, finalQuery);

        } catch (Exception e) {
            logger.error("An error occurred during the challenge execution: ", e);
        }
    }

    private Map<String, String> generateWebhook(InitialRequest personalDetails) {
        String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        logger.info("Sending POST request to generate webhook at: {}", generateUrl);

        ParameterizedTypeReference<Map<String, String>> responseType = new ParameterizedTypeReference<>() {};
        ResponseEntity<Map<String, String>> responseEntity = restTemplate.exchange(
                generateUrl, HttpMethod.POST, new HttpEntity<>(personalDetails), responseType);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to generate webhook. Status: " + responseEntity.getStatusCode());
        }

        Map<String, String> responseBody = responseEntity.getBody();
        logger.info("Received webhook response: {}", responseBody);
        String submissionUrl = findValueInMap(responseBody, "webhook");
        String accessToken = findValueInMap(responseBody, "accesstoken");

        if (submissionUrl == null || accessToken == null) {
            throw new RuntimeException("Could not find 'webhook URL' or 'accessToken' in the response.");
        }
        logger.info("Successfully received webhook URL: {}", submissionUrl);
        return Map.of("submissionUrl", submissionUrl, "accessToken", accessToken);
    }

    private String prepareFinalQuery(String regNo) {
        int lastTwoDigits = Integer.parseInt(regNo.substring(regNo.length() - 2));

        if (lastTwoDigits % 2 != 0) {
            logger.info("Registration number ends in an ODD number ({}). Using solved query for Question 1.", lastTwoDigits);
            return "SELECT p.AMOUNT AS SALARY, CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, d.DEPARTMENT_NAME FROM PAYMENTS p JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID WHERE EXTRACT(DAY FROM p.PAYMENT_TIME) <> 1 ORDER BY p.AMOUNT DESC LIMIT 1";
        } else {
            logger.info("Registration number ends in an EVEN number ({}).", lastTwoDigits);
            return "YOUR_SQL_QUERY_FOR_QUESTION_2_GOES_HERE";
        }
    }

    private void submitSolution(String url, String token, String query) {
        SolutionRequest solution = new SolutionRequest(query);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);

        HttpEntity<SolutionRequest> requestEntity = new HttpEntity<>(solution, headers);
        logger.info("Sending solution to the webhook URL...");
        ResponseEntity<String> submissionResponse = restTemplate.postForEntity(url, requestEntity, String.class);
        logger.info("✅ Submission complete!");
        logger.info("Response Status: {}", submissionResponse.getStatusCode());
        logger.info("Response Body: {}", submissionResponse.getBody());
    }
    
    private String findValueInMap(Map<String, String> map, String partialKey) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey().toLowerCase().replace(" ", "").contains(partialKey)) {
                return entry.getValue();
            }
        }
        return null;
    }
}