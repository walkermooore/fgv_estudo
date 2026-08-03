package com.fgv.studyhub;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:studyhub-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1")
class StudyHubApplicationTest {
 @Test void contextLoadsWithH2AndFlyway() {}
}
