package com.fpt.careermate.services.job_services.service;

import com.fpt.careermate.services.job_services.domain.JobPosting;
import com.fpt.careermate.services.job_services.repository.JobPostingRepo;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import io.weaviate.client.base.Result;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WeaviateImp {

    WeaviateClient client;
    JobPostingRepo jobPostingRepo;


    public void createJobPostingCollection() {
        WeaviateClass jobPostingClass = WeaviateClass.builder()
                .className("JobPosting")
                .description("Job posting ")
                .vectorizer("text2vec-weaviate")
                .properties(List.of(
                        Property.builder().name("jobId").dataType(List.of("int")).build(),
                        Property.builder().name("title").dataType(List.of("string")).build(),
                        Property.builder().name("description").dataType(List.of("string")).build(),
                        Property.builder().name("skills").dataType(List.of("string[]")).build(),
                        Property.builder().name("address").dataType(List.of("string")).build()
                ))
                .build();

        client.schema().classCreator()
                .withClass(jobPostingClass)
                .run();

        log.info("Collection 'JobPosting' created!");
    }

    public void getAllCollections() {
        var schema = client.schema().getter().run();
        log.info("Current Weaviate Schema: {}", schema);
    }

    public void deleteJobPostingCollection() {
        client.schema().classDeleter()
                .withClassName("JobPosting")
                .run();

        log.info("Collection 'JobPosting' deleted!");
    }

    public void addJobPosting(JobPosting jobPosting) {
        // Convert JobDescription -> List<String> skills
        List<String> skills = jobPosting.getJobDescriptions().stream()
                .map(jd -> jd.getJdSkill().getName())
                .toList();

        // Create Map property
        Map<String, Object> properties = Map.of(
                "jobId", jobPosting.getId(),
                "title", jobPosting.getTitle(),
                "description", jobPosting.getDescription(),
                "skills", skills,
                "address", jobPosting.getAddress()
        );

        // Save to Weaviate
        client.data().creator()
                .withClassName("JobPosting")
                .withProperties(properties)
                .run();

        log.info("JobPosting added: " + jobPosting.getTitle());
    }

    public void getJobPostings() {
        Result<List<WeaviateObject>> results = client.data().objectsGetter()
                .withClassName("JobPosting")
                .withLimit(5)
                .withVector()
                .run();

        if (results.getError() == null) {
            List<WeaviateObject> objects = results.getResult();
            for (WeaviateObject obj : objects) {
                log.info("ID: " + obj.getId());
                log.info("Properties: " + obj.getProperties());
                log.info("Vector: " + Arrays.toString(obj.getVector()));
                log.info("--------------------");
            }
        } else {
            log.error("Error: " + results.getError().getMessages());
        }
    }
}
