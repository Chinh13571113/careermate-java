# Candidate Recommendation System - Technical Documentation

## Executive Summary
This system provides AI-powered candidate recommendations for job postings using semantic search and intelligent skill matching. It combines vector embeddings for semantic similarity with rule-based skill matching to accurately rank candidates.

---

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Core Technologies](#core-technologies)
3. [Algorithm & Scoring Logic](#algorithm--scoring-logic)
4. [Implementation Details](#implementation-details)
5. [API Endpoints](#api-endpoints)
6. [Known Issues & Limitations](#known-issues--limitations)
7. [Improvements Needed](#improvements-needed)
8. [Troubleshooting](#troubleshooting)

---

## Architecture Overview

### System Flow
```
Job Posting → Extract Required Skills → Semantic Search (Weaviate) → Skill Matching → Scoring → Ranked Candidates
```

### Components
1. **Weaviate Vector Database**: Stores candidate profiles with semantic embeddings
2. **SkillMatcher Utility**: Handles synonym matching and skill hierarchy
3. **CandidateRecommendationService**: Main recommendation logic
4. **WeaviateConfig**: Connection and authentication setup

---

## Core Technologies

### 1. Weaviate Cloud (Vector Database)
- **Version**: 1.33.4
- **Purpose**: Store candidate profiles and perform semantic search
- **Vectorizer**: `text2vec-weaviate` (Weaviate's hosted inference API)
- **Default Embedding Model**: `all-MiniLM-L6-v2` (384 dimensions)
- **Connection**: HTTPS with API key authentication
- **Location**: Google Cloud Asia-Southeast1

**Why Weaviate?**
- Built-in vector search capabilities
- Automatic text vectorization via hosted API
- Scalable cloud infrastructure
- GraphQL API for complex queries

### 2. Sentence Transformers (Embeddings)
- **Model**: Snowflake/snowflake-arctic-embed-l-v2.0 (default from Weaviate)
- **Type**: Bi-encoder transformer model
- **Vector Size**: 384 dimensions
- **Use Case**: Convert text (skills, profile summaries) into dense vectors for semantic similarity

**How It Works:**
- Skills and profile text are automatically embedded by Weaviate
- Similar skills/concepts are close in vector space
- Enables finding candidates with related but not exact-match skills

### 3. SkillMatcher (Custom Rule Engine)
- **Language**: Java
- **Purpose**: Intelligent skill matching with synonyms and hierarchy
- **Features**:
  - Synonym mapping (e.g., "JS" = "JavaScript", "React" = "ReactJS")
  - Skill hierarchy (e.g., "JavaScript" includes "React", "Node.js")
  - Case-insensitive matching

---

## Algorithm & Scoring Logic

### Overall Score Calculation

```
Final Score = (Skill Match Score × 0.5 + Semantic Score × 0.4) × Experience Factor
```

**Weighted Components:**
- **50%** - Exact Skill Matching (with synonyms)
- **40%** - Semantic Similarity (vector distance)
- **10%** - Experience Factor (multiplier)

### 1. Skill Match Score (50% weight)

**Base Formula:**
```
Skill Match Score = (Matched Skills / Required Skills) + Hierarchy Bonus
```

**Synonym Matching:**
- "JavaScript" matches: js, javascript, ecmascript, es6, es2015
- "Spring Boot" matches: spring, spring boot, springboot
- "PostgreSQL" matches: postgres, postgresql, psql

**Hierarchy Bonus (up to +30%):**
- If job requires "React" and candidate has "JavaScript" → +10% bonus
- If job requires "Spring" and candidate has "Java" → +10% bonus
- Maximum hierarchy bonus: 30%

**Example:**
```
Job requires: ["Java", "Spring Boot", "PostgreSQL"]
Candidate has: ["Java", "Spring", "MySQL", "Docker"]

Matched: ["Java", "Spring Boot"] = 2/3 = 0.67
Hierarchy bonus: Java ecosystem = +0.10
Skill Score: 0.67 + 0.10 = 0.77
```

### 2. Semantic Similarity Score (40% weight)

**How It Works:**
1. Job skills concatenated into query string: "Java Spring Boot PostgreSQL"
2. Weaviate performs `nearText` semantic search
3. Returns candidates with similar skill vectors
4. Certainty score (0.0-1.0) from cosine similarity

**Vector Comparison:**
- High score (>0.8): Very similar skill sets
- Medium score (0.5-0.8): Related but different areas
- Low score (<0.5): Unrelated skill sets

**Example Semantic Matches:**
```
Query: "React JavaScript Frontend"
High matches:
- "Vue.js JavaScript CSS" (0.85) - same domain
- "Angular TypeScript HTML" (0.78) - similar frontend
Low matches:
- "Python Django Backend" (0.42) - different domain
```

### 3. Experience Factor (Multiplier)

**Formula:**
```
If candidate_exp >= required_exp:
    factor = min(1.2, 1.0 + (candidate_exp - required_exp) × 0.02)
Else:
    factor = 0.8 + (candidate_exp / required_exp) × 0.2
```

**Effects:**
- Meets requirement: 1.0 to 1.2× multiplier (up to 20% bonus)
- Below requirement: 0.8 to 1.0× multiplier (20% penalty)

**Example:**
```
Job requires: 3 years
Candidate A has: 5 years → factor = 1.04× (4% bonus)
Candidate B has: 2 years → factor = 0.93× (7% penalty)
```

### 4. Final Scoring Example

**Job Posting:**
- Title: "Senior Java Developer"
- Skills: ["Java", "Spring Boot", "PostgreSQL", "Docker"]
- Experience: 5 years

**Candidate Profile:**
- Skills: ["Java", "Spring", "MySQL", "Kubernetes", "AWS"]
- Experience: 6 years
- Summary: "Backend developer with 6 years experience..."

**Calculation:**
1. Skill matching:
   - Matched: Java, Spring Boot (synonym), Docker (kubernetes related)
   - Score: 3/4 = 0.75
   - Hierarchy bonus: +0.10
   - **Skill Score: 0.85**

2. Semantic similarity:
   - Profile vectors match job keywords
   - **Semantic Score: 0.72**

3. Experience factor:
   - Has 6 years, needs 5
   - Factor: 1.0 + (6-5) × 0.02 = **1.02×**

4. Final calculation:
   ```
   Base = (0.85 × 0.5) + (0.72 × 0.4) = 0.425 + 0.288 = 0.713
   Final = 0.713 × 1.02 = 0.727 (72.7% match)
   ```

---

## Implementation Details

### Data Structure in Weaviate

**Class Name:** `CandidateProfile`

**Schema:**
```json
{
  "class": "CandidateProfile",
  "vectorizer": "text2vec-weaviate",
  "properties": [
    {
      "name": "candidateId",
      "dataType": ["int"],
      "description": "Unique candidate identifier",
      "moduleConfig": {
        "text2vec-weaviate": { "skip": true }
      }
    },
    {
      "name": "candidateName",
      "dataType": ["text"],
      "moduleConfig": {
        "text2vec-weaviate": { "skip": true }
      }
    },
    {
      "name": "email",
      "dataType": ["text"],
      "moduleConfig": {
        "text2vec-weaviate": { "skip": true }
      }
    },
    {
      "name": "skills",
      "dataType": ["text[]"],
      "description": "List of skills - VECTORIZED for semantic search",
      "moduleConfig": {
        "text2vec-weaviate": { "skip": false }
      }
    },
    {
      "name": "totalExperience",
      "dataType": ["int"],
      "moduleConfig": {
        "text2vec-weaviate": { "skip": true }
      }
    },
    {
      "name": "aboutMe",
      "dataType": ["text"],
      "description": "Profile summary - VECTORIZED for semantic search",
      "moduleConfig": {
        "text2vec-weaviate": { "skip": false }
      }
    },
    {
      "name": "syncedAt",
      "dataType": ["text"],
      "moduleConfig": {
        "text2vec-weaviate": { "skip": true }
      }
    }
  ]
}
```

**Important Configuration:**
- `skills` and `aboutMe` are vectorized (semantic search enabled)
- Other fields skipped to reduce vector noise
- UUID generated from candidateId: `UUID.nameUUIDFromBytes(String.valueOf(candidateId).getBytes())`

### Semantic Search Query

**GraphQL Query Structure:**
```graphql
{
  Get {
    CandidateProfile(
      nearText: {
        concepts: ["Java Spring Boot PostgreSQL"]
        certainty: 0.3
      }
      limit: 30
    ) {
      candidateId
      candidateName
      email
      skills
      totalExperience
      aboutMe
      _additional {
        certainty
        distance
      }
    }
  }
}
```

**Query Parameters:**
- `concepts`: Job skills concatenated into search string
- `certainty`: 0.3 (low initial threshold to fetch more candidates)
- `limit`: 3× requested candidates (for filtering)

### SkillMatcher Mappings

**Synonym Groups (130+ mappings):**
```java
// Programming Languages
"javascript" → [js, javascript, ecmascript, es6, es2015]
"python" → [python, python3, py]
"java" → [java, java8, java11, java17, java21]
"c#" → [c#, csharp, c sharp, .net]

// Frameworks
"react" → [react, reactjs, react.js]
"spring" → [spring, spring boot, springboot]
"django" → [django, django rest framework, drf]

// Databases
"postgresql" → [postgres, postgresql, psql]
"mongodb" → [mongo, mongodb]

// Cloud
"aws" → [aws, amazon web services]
"kubernetes" → [kubernetes, k8s]

// Soft Skills
"teamwork" → [teamwork, team work, collaboration]
"problem solving" → [problem solving, problem-solving, critical thinking]
```

**Hierarchy Mappings:**
```java
"frontend development" → [html, css, javascript, react, vue, angular]
"backend development" → [java, python, node.js, go, c#]
"java" → [spring, hibernate, maven, gradle, junit]
"javascript" → [react, vue, angular, node.js, express]
"cloud computing" → [aws, azure, gcp, docker, kubernetes]
```

### Configuration

**application.yml:**
```yaml
weaviate:
  url: ${WEAVIATE_URL}
  api-key: ${WEAVIATE_API_KEY}
  vectorizer: text2vec-weaviate
  # Model is NOT specified - uses Weaviate's default (all-MiniLM-L6-v2)
```

**Environment Variables:**
```
WEAVIATE_URL=oei76mp3ttcpw5prggx3fq.c0.asia-southeast1.gcp.weaviate.cloud
WEAVIATE_API_KEY=<your-api-key>
```

---

## API Endpoints

### 1. Get Recommended Candidates
```
GET /api/recruiter/recommendations/{jobPostingId}
```

**Parameters:**
| Name | Type | Default | Description |
|------|------|---------|-------------|
| jobPostingId | int (path) | - | Job posting ID |
| maxCandidates | int (query) | 10 | Maximum candidates to return |
| minMatchScore | double (query) | 0.5 | Minimum match score (0.0-1.0) |

**Example Request:**
```
GET /api/recruiter/recommendations/50?maxCandidates=5&minMatchScore=0.6
```

**Response:**
```json
{
  "code": 0,
  "result": {
    "jobPostingId": 50,
    "jobTitle": "Senior Java Developer",
    "totalCandidatesFound": 5,
    "recommendations": [
      {
        "candidateId": 1,
        "candidateName": "Tuấn Khang",
        "email": "user01@gmail.com",
        "matchScore": 0.82,
        "matchedSkills": ["Java", "Spring Boot", "PostgreSQL"],
        "missingSkills": ["Docker"],
        "totalYearsExperience": 6,
        "profileSummary": "Full-stack developer specializing in Java..."
      }
    ],
    "processingTimeMs": 1245
  }
}
```

### 2. Sync Single Candidate
```
POST /api/admin/recommendations/sync/{candidateId}
```

**Response:**
```json
{
  "code": 0,
  "message": "Candidate synced successfully"
}
```

### 3. Sync All Candidates
```
POST /api/admin/recommendations/sync-all
```

**Response:**
```json
{
  "code": 0,
  "message": "All candidates synced successfully"
}
```

---

## Known Issues & Limitations

### 1. Weaviate Model Configuration Issue ❌
**Problem:** Cannot specify custom embedding model in Weaviate Cloud

**Error Message:**
```
vectorize target vector : update vector: Weaviate embed API error: 400 Model not found.
```

**Root Cause:**
- Weaviate Cloud API has inconsistent/undocumented model naming
- Attempting to specify model: `sentence-transformers/all-MiniLM-L6-v2` fails
- Must use default model (no model specification)

**Current Solution:**
- Removed model configuration from schema
- Using Weaviate's default model (all-MiniLM-L6-v2)
- System works but no control over model selection

**Code Location:**
```java
// CandidateRecommendationServiceImpl.java - ensureWeaviateSchema()
Map<String, Object> text2vecWeaviate = new HashMap<>();
text2vecWeaviate.put("vectorizeClassName", false);
// DON'T specify model - let Weaviate use default
// text2vecWeaviate.put("model", "sentence-transformers/all-MiniLM-L6-v2"); // FAILS!
```

### 2. Scoring Algorithm Needs Tuning ⚠️
**Problem:** Match scores may not accurately reflect candidate suitability

**Current Issues:**
- Semantic search returns candidates without exact skill matches
- A candidate with 0 matched skills but similar profile text can score 0.4+
- Experience factor has minimal impact (only 10%)

**Example Problem:**
```
Job: ["Java", "C#"]
Candidate: ["React", "JavaScript", "Go"]
Result: matchScore = 0.42 (42% match)
Issue: No actual skill overlap, but semantic vectors are similar
```

**Why This Happens:**
- Profile text ("Front-end developer") gets vectorized
- "developer" creates semantic similarity with any dev job
- Skill matching (0%) gets diluted by semantic score (40%)

### 3. No Data Persistence in Weaviate ⚠️
**Problem:** Candidate data must be manually synced to Weaviate

**Current Behavior:**
- Candidates not automatically synced when created/updated
- Admin must manually trigger sync via API
- No automatic sync on resume updates

**Impact:**
- New candidates not searchable until synced
- Updated skills not reflected in recommendations
- Risk of stale data

### 4. Limited Skill Vocabulary
**Problem:** SkillMatcher only knows ~130 skill synonyms

**Missing Mappings:**
- Many niche technologies (Kafka, RabbitMQ, GraphQL)
- Emerging frameworks (Svelte, Astro, Remix)
- Industry-specific tools
- Company-specific tech stacks

### 5. No Caching Strategy
**Problem:** Every recommendation query hits Weaviate

**Impact:**
- Slow response times (1-3 seconds)
- Unnecessary API calls
- No optimization for repeated queries

---

## Improvements Needed

### Priority 1: Fix Scoring Algorithm 🔥

**Current Problem:**
- Semantic similarity too heavily weighted (40%)
- Candidates without matching skills score too high
- Not enough emphasis on exact skill matches

**Proposed Changes:**

**Option A: Increase Skill Weight**
```
New Formula: (Skill Match × 0.7) + (Semantic × 0.2) + (Experience × 0.1)
```
- 70% skill matching (up from 50%)
- 20% semantic similarity (down from 40%)
- 10% experience factor (unchanged)

**Option B: Require Minimum Skill Match**
```java
// Filter candidates with <30% skill match before semantic scoring
if (skillMatchScore < 0.3) {
    continue; // Skip this candidate
}
```

**Option C: Adjust Semantic Certainty Threshold**
```java
// Increase initial certainty from 0.3 to 0.5
.withNearText(weaviateClient.graphQL().arguments().nearTextArgBuilder()
    .concepts(new String[]{searchQuery})
    .certainty(0.5f) // Higher threshold
    .build())
```

**Recommended: Combination Approach**
1. Increase skill weight to 60%
2. Reduce semantic weight to 30%
3. Require minimum 20% skill match
4. Keep experience at 10%

**Implementation:**
```java
// In parseSemanticSearchResults():

// Calculate scores
double skillMatchScore = skillMatcher.calculateEnhancedMatchScore(requiredSkills, candidateSkills);
double semanticScore = extractSemanticScore(candidate);

// Apply minimum skill threshold
if (skillMatchScore < 0.2) {
    continue; // Skip candidates with <20% skill match
}

// New weighted formula: 60% skill, 30% semantic, 10% experience
double baseScore = (skillMatchScore * 0.6) + (semanticScore * 0.3);
double combinedScore = baseScore * experienceFactor;
```

### Priority 2: Automatic Sync Strategy

**Options:**

**A. Event-Driven Sync (Recommended)**
```java
@Component
public class CandidateSyncEventListener {
    
    @EventListener
    public void onCandidateCreated(CandidateCreatedEvent event) {
        candidateRecommendationService.syncCandidateToWeaviate(event.getCandidateId());
    }
    
    @EventListener
    public void onResumeUpdated(ResumeUpdatedEvent event) {
        candidateRecommendationService.syncCandidateToWeaviate(event.getCandidateId());
    }
}
```

**B. Scheduled Batch Sync**
```java
@Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
public void scheduledSyncAllCandidates() {
    log.info("Starting scheduled candidate sync...");
    syncAllCandidatesToWeaviate();
}
```

**C. Async Queue-Based Sync**
```java
@Async
public CompletableFuture<Void> asyncSyncCandidate(int candidateId) {
    syncCandidateToWeaviate(candidateId);
    return CompletableFuture.completedFuture(null);
}
```

### Priority 3: Expand Skill Vocabulary

**Add Missing Technologies:**
```java
// Message Queues
addSynonyms("kafka", "kafka", "apache kafka");
addSynonyms("rabbitmq", "rabbitmq", "rabbit mq", "amqp");

// API Technologies
addSynonyms("graphql", "graphql", "graph ql");
addSynonyms("rest api", "rest", "restful", "rest api");
addSynonyms("grpc", "grpc", "protobuf");

// Modern Frameworks
addSynonyms("svelte", "svelte", "sveltekit");
addSynonyms("astro", "astro", "astro.build");
addSynonyms("remix", "remix", "remix.run");

// Mobile
addSynonyms("react native", "react native", "rn");
addSynonyms("flutter", "flutter", "dart");
addSynonyms("swift", "swift", "swiftui", "ios");
addSynonyms("kotlin", "kotlin", "android");
```

**Add More Hierarchies:**
```java
// Mobile Development
addHierarchy("mobile development",
    "react native", "flutter", "swift", "kotlin", "xamarin");

// Data Engineering
addHierarchy("data engineering",
    "spark", "hadoop", "kafka", "airflow", "databricks");

// AI/ML
addHierarchy("machine learning",
    "tensorflow", "pytorch", "scikit-learn", "pandas", "numpy");
```

### Priority 4: Implement Caching

**Redis Cache Strategy:**
```java
@Cacheable(value = "candidateRecommendations", 
           key = "#jobPostingId + '-' + #maxCandidates + '-' + #minMatchScore")
public RecommendationResponseDTO getRecommendedCandidatesForJob(
        int jobPostingId,
        Integer maxCandidates,
        Double minMatchScore) {
    // ... existing logic
}

@CacheEvict(value = "candidateRecommendations", allEntries = true)
public void syncCandidateToWeaviate(int candidateId) {
    // ... existing logic
}
```

**Configuration:**
```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000 # 1 hour
```

### Priority 5: Better Error Handling

**Current Issues:**
- Generic error messages
- No retry logic for Weaviate failures
- Sync failures not reported to user

**Improvements:**
```java
// Add retry mechanism
@Retryable(
    value = {WeaviateException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 2000)
)
public void syncCandidateToWeaviate(int candidateId) {
    // ... existing logic
}

// Better error responses
@ExceptionHandler(WeaviateException.class)
public ResponseEntity<ErrorResponse> handleWeaviateError(WeaviateException e) {
    return ResponseEntity.status(503)
        .body(new ErrorResponse(
            "WEAVIATE_UNAVAILABLE",
            "Recommendation service temporarily unavailable",
            e.getMessage()
        ));
}
```

### Priority 6: Performance Optimization

**1. Batch Operations:**
```java
// Instead of syncing one at a time:
public void syncCandidatesBatch(List<Integer> candidateIds) {
    List<Map<String, Object>> batchData = new ArrayList<>();
    
    for (int candidateId : candidateIds) {
        batchData.add(prepareCandidateData(candidateId));
    }
    
    // Use Weaviate batch API
    weaviateClient.batch().objectsBatcher()
        .withObjects(batchData.toArray(new Map[0]))
        .run();
}
```

**2. Parallel Processing:**
```java
// Parse candidates in parallel
List<CandidateRecommendationDTO> recommendations = candidates.parallelStream()
    .map(candidate -> parseCandidate(candidate, requiredSkills))
    .filter(dto -> dto.getMatchScore() >= threshold)
    .sorted(Comparator.comparing(CandidateRecommendationDTO::getMatchScore).reversed())
    .limit(limit)
    .collect(Collectors.toList());
```

**3. Index Optimization:**
```java
// Add database indices for faster candidate lookup
@Entity
@Table(name = "candidates", indexes = {
    @Index(name = "idx_candidate_resume", columnList = "resume_id"),
    @Index(name = "idx_candidate_skills", columnList = "skills")
})
```

### Priority 7: Analytics & Monitoring

**Add Metrics:**
```java
@Component
public class RecommendationMetrics {
    
    private final MeterRegistry registry;
    
    public void recordSearch(int jobId, int candidatesFound, long duration) {
        registry.counter("recommendations.searches.total").increment();
        registry.timer("recommendations.search.duration").record(duration, TimeUnit.MILLISECONDS);
        registry.gauge("recommendations.candidates.found", candidatesFound);
    }
    
    public void recordSyncSuccess(int candidateId) {
        registry.counter("recommendations.sync.success").increment();
    }
    
    public void recordSyncFailure(int candidateId, String error) {
        registry.counter("recommendations.sync.failure", "error", error).increment();
    }
}
```

**Add Logging:**
```java
// Detailed matching logs for debugging
log.info("🎯 Recommendation Search for Job {}: {} required skills", 
    jobId, requiredSkills.size());
log.info("⏱️ Weaviate search took {}ms", weaviateDuration);
log.info("📊 Found {} candidates above threshold {}", 
    filtered.size(), threshold);
log.info("🏆 Top candidate: {} (score: {})", 
    topCandidate.getName(), topCandidate.getScore());
```

---

## Troubleshooting

### Issue: No candidates found
**Symptoms:** API returns 0 recommendations

**Possible Causes:**
1. No candidates synced to Weaviate
2. Threshold too high (default 0.5)
3. Job has no skills defined
4. Weaviate connection issues

**Solutions:**
```bash
# 1. Check Weaviate connection
curl -H "Authorization: Bearer $WEAVIATE_API_KEY" \
  https://$WEAVIATE_URL/v1/schema/CandidateProfile

# 2. Sync candidates
POST /api/admin/recommendations/sync-all

# 3. Lower threshold
GET /api/recruiter/recommendations/50?minMatchScore=0.3

# 4. Check job skills
GET /api/jobs/50
```

### Issue: Low match scores
**Symptoms:** All candidates score below 0.5

**Causes:**
1. Job requires very specific/niche skills
2. Candidates don't have matching skills
3. Skill synonyms not defined
4. Semantic model doesn't understand domain

**Solutions:**
1. Add more skill synonyms to SkillMatcher
2. Lower minMatchScore threshold
3. Expand job description with related terms
4. Review candidate skill data quality

### Issue: Wrong candidates recommended
**Symptoms:** Recommended candidates lack required skills

**Diagnosis:**
```bash
# Enable debug logging
logging.level.com.fpt.careermate.services.recommendation=DEBUG

# Check logs for:
# - Semantic scores
# - Skill match scores
# - Experience factors
```

**Solutions:**
1. Adjust scoring weights (see Priority 1 improvements)
2. Increase minMatchScore threshold
3. Add minimum skill match requirement
4. Review and improve skill mappings

### Issue: Slow response times
**Symptoms:** Requests take 3+ seconds

**Causes:**
1. Weaviate network latency (Asia-Southeast1)
2. Large result set processing
3. No caching
4. Complex GraphQL queries

**Solutions:**
1. Implement Redis caching
2. Reduce initial fetch limit
3. Add database indices
4. Consider geo-distributed Weaviate deployment

### Issue: Sync failures
**Symptoms:** Candidates not appearing in search after sync

**Check Logs:**
```
ERROR Failed to sync candidate X to Weaviate: Model not found
```

**Solutions:**
1. Verify Weaviate API key is valid
2. Check schema configuration (don't specify model)
3. Ensure skills array is not empty
4. Check for special characters in text fields

---

## Development Guidelines

### Adding New Skill Synonyms
```java
// 1. Add to SkillMatcher.initializeSkillSynonyms()
addSynonyms("canonical-name", "variant1", "variant2", "variant3");

// 2. Test matching
String skill1 = "variant1";
String skill2 = "variant2";
assertTrue(skillMatcher.skillsMatch(skill1, skill2));

// 3. Redeploy and re-sync all candidates
POST /api/admin/recommendations/sync-all
```

### Testing Recommendations
```bash
# 1. Create test job with known skills
POST /api/jobs
{
  "title": "Test Job",
  "skills": ["Java", "Spring Boot"]
}

# 2. Create test candidate with matching skills
POST /api/candidates
{
  "skills": ["Java", "Spring"]
}

# 3. Sync candidate
POST /api/admin/recommendations/sync/{candidateId}

# 4. Get recommendations
GET /api/recruiter/recommendations/{jobId}

# 5. Verify match score and matched skills
```

### Monitoring Production
```bash
# Check Weaviate health
curl https://$WEAVIATE_URL/v1/.well-known/ready

# Check candidate count
curl -X POST https://$WEAVIATE_URL/v1/graphql \
  -H "Authorization: Bearer $WEAVIATE_API_KEY" \
  -d '{"query": "{ Aggregate { CandidateProfile { meta { count } } } }"}'

# View application logs
tail -f logs/application.log | grep "recommendation"
```

---

## Summary

### What Works Well ✅
- Semantic search finds candidates with related skills
- Synonym matching handles common variations
- Skill hierarchy gives credit for parent skills
- Clean API with configurable thresholds
- Fast sync (< 1 second per candidate)

### What Needs Work ⚠️
- Scoring algorithm needs rebalancing (too much weight on semantics)
- No automatic sync (manual trigger required)
- Limited skill vocabulary (only ~130 mappings)
- No caching (slow repeated queries)
- Model configuration locked to Weaviate default

### Development Priority
1. **Fix scoring algorithm** (highest impact on accuracy)
2. **Add automatic sync** (better UX)
3. **Expand skill vocabulary** (better coverage)
4. **Implement caching** (better performance)
5. **Add retry logic** (better reliability)

---

## References

- **Weaviate Documentation**: https://weaviate.io/developers/weaviate
- **Sentence Transformers**: https://www.sbert.net/docs/pretrained_models.html
- **GraphQL API**: https://weaviate.io/developers/weaviate/api/graphql
- **Vector Search Concepts**: https://weaviate.io/developers/weaviate/concepts/vector-index

**Last Updated**: November 5, 2025
**System Version**: 1.0
**Weaviate Version**: 1.33.4

